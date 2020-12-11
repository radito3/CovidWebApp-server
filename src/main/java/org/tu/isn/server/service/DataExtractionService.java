package org.tu.isn.server.service;

import org.springframework.stereotype.Service;
import org.tu.isn.server.model.*;

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

    public TableResponseCovidData extractTableData() {
        List<String> columnNames = new ArrayList<>();
        List<TableResponseCovidData.TableRow> rows = new ArrayList<>();
        try {
            consumeFileContent(OUTPUT_DATA_FILE_NAME, reader -> {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException("Empty file");
                }
                String[] headers = line.split(",");
                columnNames.addAll(Arrays.asList(headers)
                                         .subList(1, headers.length));

                line = reader.readLine();
                while (line != null) {
                    String[] parts = line.split(",");
                    if (parts.length != headers.length) {
                        throw new IllegalStateException("Invalid file format");
                    }
                    String rowName = parts[0];
                    List<Integer> rowData = Arrays.asList(parts)
                                                  .subList(1, headers.length)
                                                  .stream()
                                                  .map(Integer::valueOf)
                                                  .collect(Collectors.toList());

                    rows.add(ImmutableTableResponseCovidData.TableRow.builder()
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
                                              .headers(columnNames)
                                              .rows(rows)
                                              .build();
    }

    public HeatmapResponseCovidData extractHeatmapData() {
        return null;
    }

    public DiagramResponseCovidData extractDiagramData() {
        List<DiagramDataRow> presentData = new ArrayList<>();
        List<DiagramDataRow> predictedData = new ArrayList<>();
        try {
            extractDiagramData(presentData, INPUT_DATA_FILE_NAME);
            extractDiagramData(predictedData, OUTPUT_DATA_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ImmutableDiagramResponseCovidData.builder()
                                                .abscissaValueName("Time")
                                                .abscissaValueDivisions(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May"))
                                                .ordinateValeName("Num Casualties")
                                                .ordinateValueDivisions(Arrays.asList(5000, 10000, 15000, 20000, 25000))
                                                .presentData(presentData)
                                                .predictedData(predictedData)
                                                .build();
    }

    private void consumeFileContent(String fileName, FileContentConsumer readerConsumer) throws IOException {
        Path outputDataFile = Paths.get(fileName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            Files.newInputStream(outputDataFile), StandardCharsets.UTF_8))) {
            readerConsumer.accept(reader);
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
                                                                .identifier(parts[0]) //maybe some parsing of the date
                                                                .value(Integer.parseInt(parts[1]))
                                                                .build();
                dataRows.add(dataRow);
                line = reader.readLine();
            }
        });
    }

    @FunctionalInterface
    interface FileContentConsumer {
        void accept(BufferedReader reader) throws IOException;
    }

}
