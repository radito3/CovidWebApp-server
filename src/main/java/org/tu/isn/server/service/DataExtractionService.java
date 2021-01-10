package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tu.isn.server.datasets.DatasetParser;
import org.tu.isn.server.model.*;
import org.tu.isn.server.util.DataPaginator;
import org.tu.isn.server.util.FileContentProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class DataExtractionService {

    @Autowired
    private DatasetParser datasetParser;

    private static final String INPUT_DATA_FILE_NAME = System.getenv("INPUT_DATA_FILE_NAME");
    private static final String OUTPUT_DATA_FILE_NAME = System.getenv("OUTPUT_DATA_FILE_NAME");

    public TableResponseCovidData extractTableData(String operationId, int page) {
        String inputFileName = INPUT_DATA_FILE_NAME + "_" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "_" + operationId;
        List<TableDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = countLines(outputFileName);
            long inputFileLines = countLines(inputFileName);

            int batchLen = (int) (10 * countries);
            long totalLines = outputFileLines + inputFileLines;
            totalBatches = (int) (totalLines / batchLen);
            if (batchLen >= totalLines) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .build();

            data = dataPaginator.getPageOfResources(this::createTableDataRow);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableTableResponseCovidData.builder()
                                              .currentPage(page)
                                              .totalPages(totalBatches)
                                              .headers(List.of("Date", "Country", "Deaths", "Recovered", "Active"))
                                              .resources(data)
                                              .build();
    }

    private TableDataRow createTableDataRow(String line) {
        String[] parts = line.split(",");
        return ImmutableTableDataRow.builder()
                                    .date(parts[datasetParser.getDateIndex()])
                                    .country(parts[datasetParser.getCountryNameIndex()])
                                    .deaths(Integer.valueOf(parts[datasetParser.getDeathsIndex()]))
                                    .recovered(Integer.valueOf(parts[datasetParser.getRecoveredIndex()]))
                                    .active(Integer.valueOf(parts[datasetParser.getActiveIndex()]))
                                    .build();
    }

    public HeatmapResponseCovidData extractHeatmapData(String operationId, int page, String aggregateBy) {
        AggregateType aggregateType = AggregateType.of(aggregateBy);
        String inputFileName = INPUT_DATA_FILE_NAME + "_" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "_" + operationId;
        List<HeatmapDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            List<String> countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                                       .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                       .distinct()
                                                                                       .collect(Collectors.toList()));
            long outputFileLines = countLines(outputFileName);
            long inputFileLines = countLines(inputFileName);
            
            int batchLen = 3 * aggregateType.getDaysMapped() * countries.size();
            long totalLines = outputFileLines + inputFileLines;
            totalBatches = (int) (totalLines / batchLen);
            if (totalBatches == 0) {
                totalBatches = 1;
            }

            if (aggregateType == AggregateType.DAY) {
                DataPaginator dataPaginator = DataPaginator.builder()
                                                           .setPage(page)
                                                           .setBatchLen(batchLen)
                                                           .setInputFileName(inputFileName)
                                                           .setOutputFileName(outputFileName)
                                                           .build();

                data = dataPaginator.getPageOfResources(this::createHeatmapDataRow);
            } else {
                long offsetFrom = page == 0 ? 0 : (long) page * batchLen;

                //FIXME too much indentation -> extract into methods OR refactor data paginator
                for (String country : countries) {
                    for (long i = 0; i < 3; i++) {
                        Predicate<String> lineFilter = line -> {
                            String[] parts = line.split(",");
                            return country.replaceAll("\\*", "")
                                          .equals(parts[datasetParser.getCountryNameIndex()].replaceAll("\\*", ""));
                        };

                        long start = i * aggregateType.getDaysMapped();
                        long limit = aggregateType.getDaysMapped();

                        List<HeatmapDataRow> innerResult = consumeFileLines(inputFileName, outputFileName,
                                                                            offsetFrom, start, limit,
                                                                            lineFilter, this::createHeatmapDataRow);
                        if (innerResult.isEmpty()) {
                            continue;
                        }
                        HeatmapDataRow first = innerResult.get(0);
                        int sum = first.getValue();
                        for (int j = 1; j < innerResult.size(); j++) {
                            sum += innerResult.get(j).getValue();
                        }
                        data.add(ImmutableHeatmapDataRow.copyOf(first)
                                                        .withValue(sum));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableHeatmapResponseCovidData.builder()
                                                .currentPage(page)
                                                .totalPages(totalBatches)
                                                .resources(data)
                                                .build();
    }

    private HeatmapDataRow createHeatmapDataRow(String line) {
        String[] parts = line.split(",");
        int deaths = Integer.parseInt(parts[datasetParser.getDeathsIndex()]);
        int recovered = Integer.parseInt(parts[datasetParser.getRecoveredIndex()]);
        int active = Integer.parseInt(parts[datasetParser.getActiveIndex()]);
        double lat = Double.parseDouble(parts[datasetParser.getCountryCoordinateLatIndex()]);
        double lon = Double.parseDouble(parts[datasetParser.getCountryCoordinateLonIndex()]);
        return ImmutableHeatmapDataRow.builder()
                                      .coordinates(ImmutableCoordinate.of(lat, lon))
                                      .countryName(parts[datasetParser.getCountryNameIndex()])
                                      .value(deaths + recovered + active)
                                      .build();
    }

    //FIXME hideous amount of parameters...
    private <T> List<T> consumeFileLines(String fileNameIn, String fileNameOut,
                                  long offsetFrom, long start, long limit,
                                  Predicate<String> filter, Function<String, ? extends T> entryProducer) throws IOException {
        Path inputFile = Paths.get(fileNameIn);
        Path outputFile = Paths.get(fileNameOut);

        try (BufferedReader inputReader = Files.newBufferedReader(inputFile);
            BufferedReader outputReader = Files.newBufferedReader(outputFile)) {

            return Stream.concat(inputReader.lines().skip(1), outputReader.lines().skip(1))
                         .skip(offsetFrom)
                         .filter(filter)
                         .skip(start)
                         .limit(limit)
                         .map(entryProducer)
                         .collect(Collectors.toList());
        }
    }

    public DiagramResponseCovidData extractDiagramData(String operationId, int page, String country) {
        String countryName = URLDecoder.decode(country, StandardCharsets.UTF_8);
        String inputFileName = INPUT_DATA_FILE_NAME + "_" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "_" + operationId;
        List<DiagramDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long presentDaysForCountry = processFileContent(inputFileName, reader -> reader.lines()
                                                                                           .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                           .map(countryStr -> countryStr.replaceAll("\\*", ""))
                                                                                           .filter(countryName::equals)
                                                                                           .count());
            long predictedDaysForCountry = processFileContent(outputFileName, reader -> reader.lines()
                                                                                              .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                              .map(countryStr -> countryStr.replaceAll("\\*", ""))
                                                                                              .filter(countryName::equals)
                                                                                              .count());

            int batchLen = 6 * 30;
            long totalDaysForCountry = presentDaysForCountry + predictedDaysForCountry;
            totalBatches = (int) (totalDaysForCountry / batchLen);
            if (batchLen >= totalDaysForCountry) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .setFilter(line -> {
                                                           String[] parts = line.split(",");
                                                           String countryStr = parts[datasetParser.getCountryNameIndex()];
                                                           return countryName.equals(countryStr.replaceAll("\\*", ""));
                                                       })
                                                       .build();

            data = dataPaginator.getPageOfResources(this::createDiagramDataRowForCountry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableDiagramResponseCovidData.builder()
                                                .abscissaValueName("Time")
                                                .abscissaValueDivisions(generateAbscissaValueDivisions(data.get(0)))
                                                .ordinateValeName("Number Affected")
                                                .ordinateValueDivisions(generateOrdinateValueDivisions())
                                                .currentPage(page)
                                                .totalPages(totalBatches)
                                                .resources(data)
                                                .build();
    }

    private DiagramDataRow createDiagramDataRowForCountry(String line) {
        String[] parts = line.split(",");
        int deaths = Integer.parseInt(parts[datasetParser.getDeathsIndex()]);
        int recovered = Integer.parseInt(parts[datasetParser.getRecoveredIndex()]);
        int active = Integer.parseInt(parts[datasetParser.getActiveIndex()]);
        return ImmutableDiagramDataRow.builder()
                                      .identifier(parts[datasetParser.getDateIndex()])
                                      .value(deaths + recovered + active)
                                      .build();
    }

    private List<Integer> generateOrdinateValueDivisions() {
        String valueDivisionsStep = System.getenv("DIAGRAM_VALUES_STEP");
        String valuesLimit = System.getenv("DIAGRAM_VALUES_LIMIT");
        return IntStream.iterate(0, i -> i + Integer.parseInt(valueDivisionsStep))
                        .skip(1)
                        .takeWhile(i -> i <= Integer.parseInt(valuesLimit))
                        .boxed()
                        .collect(Collectors.toList());
    }

    private List<String> generateAbscissaValueDivisions(DiagramDataRow firstEntry) {
        int startingMonth = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                             .parse(firstEntry.getIdentifier())
                                             .get(ChronoField.MONTH_OF_YEAR);

        return Stream.iterate(Month.of(startingMonth), month -> month.plus(1))
                     .limit(6) //6 is DIAGRAM_VALUES_LIMIT / STEP
                     .map(month -> month.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                     .collect(Collectors.toList());
    }

    private <T> T processFileContent(String fileName, FileContentProcessor<T> processor) throws IOException {
        Path file = Paths.get(fileName);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); //skip csv headers
            return processor.accept(reader);
        }
    }

    private long countLines(String fileName) throws IOException {
        return Files.lines(Paths.get(fileName))
                    .skip(1)
                    .count();
    }

}
