package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tu.isn.server.datasets.DatasetParser;
import org.tu.isn.server.model.*;
import org.tu.isn.server.util.DataPaginator;
import org.tu.isn.server.util.FileContentProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tu.isn.server.util.DataPaginator.consumeFileLines;

@Service
public class DataExtractionService {

    @Autowired
    private DatasetParser datasetParser;

    private static final String INPUT_DATA_FILE_NAME = System.getenv("INPUT_DATA_FILE_NAME");
    private static final String OUTPUT_DATA_FILE_NAME = System.getenv("OUTPUT_DATA_FILE_NAME");

    public TableResponseCovidData extractTableData(String operationId, int page) {
        String inputFileName = INPUT_DATA_FILE_NAME + "-" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "-" + operationId;
        List<TableDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                               .skip(1)
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().skip(1).count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().skip(1).count());

            int batchLen = 10 * Math.toIntExact(countries);
            long daysTotalPerCountry = (outputFileLines + inputFileLines) / countries;
            totalBatches = (int) (daysTotalPerCountry / 10);

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputLimit(inputFileLines)
                                                       .setOutputLimit(outputFileLines)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .build();

            data = dataPaginator.getPageOfResources(line -> {
                String[] parts = line.split(",");
                return ImmutableTableDataRow.builder()
                                            .date(parts[datasetParser.getDateIndex()])
                                            .country(parts[datasetParser.getCountryNameIndex()])
                                            .deaths(Integer.valueOf(parts[datasetParser.getDeathsIndex()]))
                                            .recovered(Integer.valueOf(parts[datasetParser.getRecoveredIndex()]))
                                            .active(Integer.valueOf(parts[datasetParser.getActiveIndex()]))
                                            .build();
            });
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

    public HeatmapResponseCovidData extractHeatmapData(String operationId, int page, String aggregateBy) {
        AggregateType aggregateType = AggregateType.of(aggregateBy);
        String inputFileName = INPUT_DATA_FILE_NAME + "-" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "-" + operationId;
        List<HeatmapDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                               .skip(1)
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().skip(1).count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().skip(1).count());
            
            int batchLen = (int) (10 * aggregateType.getDaysMapped() * countries);
            long daysTotal = outputFileLines + inputFileLines;
            totalBatches = (int) (daysTotal / batchLen);

            if (aggregateType == AggregateType.DAY) {
                DataPaginator dataPaginator = DataPaginator.builder()
                                                           .setPage(page)
                                                           .setBatchLen(batchLen)
                                                           .setInputLimit(inputFileLines)
                                                           .setOutputLimit(outputFileLines)
                                                           .setInputFileName(inputFileName)
                                                           .setOutputFileName(outputFileName)
                                                           .build();
                data = dataPaginator.getPageOfResources(line -> {
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
                });
            } else {
                //TODO finish
                Map<String, Integer> countryNumDataRows = new HashMap<>();

                long offsetFrom = page == 0 ? 0 : (long) page * batchLen;
                long offsetTo = page == 0 ? batchLen : (long) (page + 1) * batchLen;
                if (batchLen > inputFileLines + outputFileLines) {
                    offsetTo = inputFileLines + outputFileLines;
                }

                if (offsetFrom < inputFileLines) { //start is within existing data
                    if (offsetTo > inputFileLines) { //time slice is within both files' data
                        long additional = offsetTo - inputFileLines;
//                        consumeFileLines(inputFileName, offsetFrom, inputFileLines, line -> result.add(entryProducer.apply(line)));
//                        consumeFileLines(outputFileName, 0, additional, line -> result.add(entryProducer.apply(line)));
                    } else { //time slice is within existing data only
//                        consumeFileLines(inputFileName, offsetFrom, offsetTo, line -> result.add(entryProducer.apply(line)));
                    }
                } else { //time slice is within predicted data only
//                    consumeFileLines(outputFileName, offsetFrom, offsetTo, line -> result.add(entryProducer.apply(line)));
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

    public DiagramResponseCovidData extractDiagramData(String operationId, int page, String country) {
        String inputFileName = INPUT_DATA_FILE_NAME + "-" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "-" + operationId;
        List<DiagramDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long presentDaysForCountry = processFileContent(inputFileName, reader -> reader.lines()
                                                                                          .skip(1)
                                                                                          .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                          .filter(country::equals)
                                                                                          .count());
            long predictedDaysForCountry = processFileContent(outputFileName, reader -> reader.lines()
                                                                                              .skip(1)
                                                                                              .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                              .filter(country::equals)
                                                                                              .count());

            int batchLen = 6 * 30;
            long totalDays = presentDaysForCountry + predictedDaysForCountry;
            totalBatches = (int) (totalDays / batchLen);
            if (batchLen > totalDays) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputLimit(presentDaysForCountry)
                                                       .setOutputLimit(predictedDaysForCountry)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .build();

            data = dataPaginator.getPageOfResources(line -> {
                String[] parts = line.split(",");
                int deaths = Integer.parseInt(parts[datasetParser.getDeathsIndex()]);
                int recovered = Integer.parseInt(parts[datasetParser.getRecoveredIndex()]);
                int active = Integer.parseInt(parts[datasetParser.getActiveIndex()]);
                return ImmutableDiagramDataRow.builder()
                                              .identifier(parts[datasetParser.getDateIndex()])
                                              .value(deaths + recovered + active)
                                              .build();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableDiagramResponseCovidData.builder()
                                                .abscissaValueName("Time")
                                                .abscissaValueDivisions(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May"))
                                                .ordinateValeName("Number of Casualties")
                                                .ordinateValueDivisions(generateOrdinateValueDivisions())
                                                .currentPage(page)
                                                .totalPages(totalBatches)
                                                .resources(data)
                                                .build();
    }

    private List<Integer> generateOrdinateValueDivisions() {
        String valueDivisionsStep = System.getenv("DIAGRAM_VALUES_STEP");
        String valuesLimit = System.getenv("DIAGRAM_VALUES_LIMIT");
        return IntStream.iterate(0, i -> i + Integer.parseInt(valueDivisionsStep))
                        .skip(1)
                        .limit(Long.parseLong(valuesLimit))
                        .boxed()
                        .collect(Collectors.toList());
    }

    private <T> T processFileContent(String fileName, FileContentProcessor<T> processor) throws IOException {
        Path outputDataFile = Paths.get(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(outputDataFile), StandardCharsets.UTF_8))) {
            return processor.accept(reader);
        }
    }

}
