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
import java.util.Objects;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().count());

            int batchLen = 10 * Math.toIntExact(countries);
            long daysTotalPerCountry = (outputFileLines + inputFileLines) / countries;
            totalBatches = (int) (daysTotalPerCountry / 10);
            if (totalBatches == 0) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputLimit(inputFileLines)
                                                       .setOutputLimit(outputFileLines)
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
        String inputFileName = INPUT_DATA_FILE_NAME + "-" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "-" + operationId;
        List<HeatmapDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long countries = processFileContent(inputFileName, reader -> reader.lines()
                                                                               .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                               .distinct()
                                                                               .count());
            long outputFileLines = processFileContent(outputFileName, reader -> reader.lines().count());
            long inputFileLines = processFileContent(inputFileName, reader -> reader.lines().count());
            
            int batchLen = (int) (10 * aggregateType.getDaysMapped() * countries);
            long daysTotal = outputFileLines + inputFileLines;
            totalBatches = (int) (daysTotal / batchLen);
            if (totalBatches == 0) {
                totalBatches = 1;
            }

            DataPaginator dataPaginator = DataPaginator.builder()
                                                       .setPage(page)
                                                       .setBatchLen(batchLen)
                                                       .setInputLimit(inputFileLines)
                                                       .setOutputLimit(outputFileLines)
                                                       .setInputFileName(inputFileName)
                                                       .setOutputFileName(outputFileName)
                                                       .build();
            if (aggregateType == AggregateType.DAY) {
                data = dataPaginator.getPageOfResources(this::createHeatmapDataRow);
            } else {
                List<String> lines = getAggregatedLines(countries, aggregateType.getDaysMapped(), dataPaginator);
                data = lines.stream()
                            .filter(Objects::nonNull)
                            .map(this::createHeatmapDataRow)
                            .collect(Collectors.toList());
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

    private List<String> getAggregatedLines(long countries, int daysToAggregate, DataPaginator dataPaginator) throws IOException {
        Map<String, AtomicInteger> countryDataAggregationCounters = new HashMap<>((int) countries);
        Map<String, StringBuilder> countryAggregatedLines = new HashMap<>((int) countries);

        return dataPaginator.getPageOfResources(line -> {
            String[] parts = line.split(",");
            String country = parts[datasetParser.getCountryNameIndex()];

            int countryLinesCounter = countryDataAggregationCounters.computeIfAbsent(country, k -> new AtomicInteger(0))
                                                                    .incrementAndGet();
            StringBuilder aggregatedLines = countryAggregatedLines.computeIfAbsent(country, k -> new StringBuilder())
                                                                  .append(line);

            if (countryLinesCounter != daysToAggregate) {
                aggregatedLines.append('+');
                return null;
            }

            StringBuilder result = new StringBuilder();
            int firstNumber = findIndexOfNthOccurance(line, ',', datasetParser.getDeathsIndex()) + 1;
            String commonDataForCountry = line.substring(0, firstNumber);
            result.append(commonDataForCountry);

            int sum = Arrays.stream(aggregatedLines.toString().split("\\+"))
                            .flatMap(innerLine -> Arrays.stream(extractDataToAggregateFromCsvLine(innerLine)))
                            .mapToInt(Integer::parseInt)
                            .sum();

            result.append(sum)
                  .append(',')
                  .append(0)
                  .append(',')
                  .append(0);

            countryDataAggregationCounters.get(country).set(0);
            aggregatedLines.delete(0, aggregatedLines.length());
            return result.toString();
        });
    }

    private String[] extractDataToAggregateFromCsvLine(String line) {
        int firstNumber = findIndexOfNthOccurance(line, ',', datasetParser.getDeathsIndex()) + 1;
        return line.substring(firstNumber)
                   .split(",");
    }

    private int findIndexOfNthOccurance(String str, char ch, int n) {
        int occur = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                occur++;
            }
            if (occur == n) {
                return i;
            }
        }
        return -1;
    }

    public DiagramResponseCovidData extractDiagramData(String operationId, int page, String country) {
        String inputFileName = INPUT_DATA_FILE_NAME + "-" + operationId;
        String outputFileName = OUTPUT_DATA_FILE_NAME + "-" + operationId;
        List<DiagramDataRow> data = new ArrayList<>();
        int totalBatches = -1;
        try {
            long presentDaysForCountry = processFileContent(inputFileName, reader -> reader.lines()
                                                                                           .map(line -> line.split(",")[datasetParser.getCountryNameIndex()])
                                                                                           .filter(country::equals)
                                                                                           .count());
            long predictedDaysForCountry = processFileContent(outputFileName, reader -> reader.lines()
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

            data = dataPaginator.getPageOfResources(this::createDiagramDataRow);
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

    private DiagramDataRow createDiagramDataRow(String line) {
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
                        .limit(Long.parseLong(valuesLimit))
                        .boxed()
                        .collect(Collectors.toList());
    }

    private <T> T processFileContent(String fileName, FileContentProcessor<T> processor) throws IOException {
        Path file = Paths.get(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            reader.readLine(); //skip csv headers
            return processor.accept(reader);
        }
    }

}
