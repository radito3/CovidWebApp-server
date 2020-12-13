package org.tu.isn.server.service;

import org.springframework.stereotype.Service;
import org.tu.isn.server.model.*;
import org.tu.isn.server.util.FileContentConsumer;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataExtractionService {

    private static final String INPUT_DATA_FILE_NAME = System.getenv("INPUT_DATA_FILE_NAME");
    private static final String OUTPUT_DATA_FILE_NAME = System.getenv("OUTPUT_DATA_FILE_NAME");

    public TableResponseCovidData extractTableData(int page) {
        List<TableDataRow> rows = new ArrayList<>();
        try {
            //TODO pagination will be from batches of 10 days, not file lines
            // remove this
            long fileLines = processFileContent(OUTPUT_DATA_FILE_NAME, reader -> {
                long lines = 0;
                while (reader.readLine() != null) {
                    lines++;
                }
                return lines;
            });

            consumeFileContent(OUTPUT_DATA_FILE_NAME, reader -> {
                String line = reader.readLine();
                while (line != null) {
                    String[] parts = line.split(",");
                    String rowName = parts[0];
                    List<Integer> rowData = Arrays.asList(parts)
                                                  .subList(1, parts.length)
                                                  .stream()
                                                  .map(Integer::valueOf)
                                                  .collect(Collectors.toList());

                    rows.add(ImmutableTableDataRow.builder()
                                                  .header(rowName)
                                                  .data(rowData)
                                                  .build());

                    line = reader.readLine();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableTableResponseCovidData.builder()
                                              .headers(List.of("Date", "Country", "Deaths", "Recovered", "Active"))
                                              .resources(rows)
                                              .build();
    }

    public HeatmapResponseCovidData extractHeatmapData(int page, String aggregateBy) {
        AggregateType aggregateType = AggregateType.of(aggregateBy);

        return null;
    }

    public DiagramResponseCovidData extractDiagramData(int page, String country) {
        //TODO determine whether to extract from the input file (the present data) or the output file (ML predicted data)
        // depending on the page
        // so the end of present data and beginning of predicted data is the last day in the dataset
        List<DiagramDataRow> data = new ArrayList<>();
        try {
            extractDiagramData(data, INPUT_DATA_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableDiagramResponseCovidData.builder()
                                                .abscissaValueName("Time")
                                                .abscissaValueDivisions(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May"))
                                                .ordinateValeName("Num Casualties")
                                                .ordinateValueDivisions(Arrays.asList(5000, 10000, 15000, 20000, 25000))
                                                .resources(data)
                                                .build();
    }

    private void consumeFileContent(String fileName, FileContentConsumer consumer) throws IOException {
        processFileContent(fileName, reader -> {
            consumer.accept(reader);
            return null;
        });
    }

    private <T> T processFileContent(String fileName, FileContentProcessor<T> processor) throws IOException {
        Path outputDataFile = Paths.get(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            Files.newInputStream(outputDataFile), StandardCharsets.UTF_8))) {
            return processor.accept(reader);
        }
    }

    private void extractDiagramData(List<DiagramDataRow> dataRows, String fileName) throws IOException {
        consumeFileContent(fileName, reader -> {
            if (reader.readLine() == null) { //skip headers
                throw new IllegalStateException("Empty file");
            }
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(",");
                DiagramDataRow dataRow = ImmutableDiagramDataRow.builder()
                                                                .identifier(parts[0])
                                                                .value(Integer.parseInt(parts[1]))
                                                                .build();
                dataRows.add(dataRow);
                line = reader.readLine();
            }
        });
    }

}
