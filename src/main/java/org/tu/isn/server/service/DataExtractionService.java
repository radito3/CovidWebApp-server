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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().count());

            int batchLen = (int) (10 * countries);
            long daysTotalPerCountry = (outputFileLines + inputFileLines) / countries;
            totalBatches = (int) (daysTotalPerCountry / 10);
            if (totalBatches == 0) {
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
            long countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().count());
            
            int batchLen = (int) (10 * countries);
            long daysTotal = outputFileLines + inputFileLines;
            totalBatches = (int) (daysTotal / batchLen) / aggregateType.getDaysMapped();
            if (totalBatches == 0) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .build();
            if (aggregateType == AggregateType.DAY) {
                data = dataPaginator.getPageOfResources(this::createHeatmapDataRow);
            } else {
                //FIXME need to loop through batchLen * aggregateType.getDaysMapped() days of content
                // for i := 0; i < 10; i++ ->
                // for country in countries {
                //      read 10 * days_mapped entries for country
                //      aggregate those entries
                //      add to result
                // }
                Map<String, List<String>> aggregate = new HashMap<>((int) countries);
                dataPaginator.getPageOfResources(line -> {
                                                    String[] parts = line.split(",");
                                                    aggregate.computeIfAbsent(parts[datasetParser.getCountryNameIndex()],
                                                                              k -> new ArrayList<>())
                                                             .add(line);
                                                    return null;
                                                 });

                for (List<String> aggregatedLines : aggregate.values()) {
                    HeatmapDataRow dataRow = createHeatmapDataRow(aggregatedLines.get(0));
                    int sum = aggregatedLines.stream()
                                             .skip(1)
                                             .mapToInt(line -> {
                                                 String[] parts = line.split(",");
                                                 int deaths = Integer.parseInt(parts[datasetParser.getDeathsIndex()]);
                                                 int recovered = Integer.parseInt(parts[datasetParser.getRecoveredIndex()]);
                                                 int active = Integer.parseInt(parts[datasetParser.getActiveIndex()]);
                                                 return deaths + recovered + active;
                                             })
                                             .sum();
                    data.add(ImmutableHeatmapDataRow.copyOf(dataRow)
                                                    .withValue(dataRow.getValue() + sum));
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

    public DiagramResponseCovidData extractDiagramData(String operationId, int page, String country) {
        String countryName = URLDecoder.decode(country, StandardCharsets.UTF_8);
        String inputFileName = INPUT_DATA_FILE_NAME + "_" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "_" + operationId;
        List<DiagramDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long presentDaysForCountry = processFileContent(inputFileName, reader -> reader.lines()
                                                                                           .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                           .filter(countryName::equals)
                                                                                           .count());
            long predictedDaysForCountry = processFileContent(outputFileName, reader -> reader.lines()
                                                                                              .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                              .filter(countryName::equals)
                                                                                              .count());

            int batchLen = 6 * 30;
            long totalDays = presentDaysForCountry + predictedDaysForCountry;
            totalBatches = (int) (totalDays / batchLen);
            if (batchLen >= totalDays) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .setFilter(line -> {
                                                           String[] parts = line.split(",");
                                                           return countryName.replaceAll("\\*", "")
                                                                             .equals(parts[datasetParser.getCountryNameIndex()]);
                                                       })
                                                       .build();
            data = dataPaginator.getPageOfResources(this::createDiagramDataRowForCountry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableDiagramResponseCovidData.builder()
                                                .abscissaValueName("Time")
                                                .abscissaValueDivisions(List.of("Jan", "Feb", "Mar", "Apr", "May", "June"))
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

    private <T> T processFileContent(String fileName, FileContentProcessor<T> processor) throws IOException {
        Path file = Paths.get(fileName);
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            reader.readLine(); //skip csv headers
            return processor.accept(reader);
        }
    }

}
