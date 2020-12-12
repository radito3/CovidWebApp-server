package org.tu.isn.server.service;

import org.springframework.stereotype.Component;
import org.tu.isn.server.model.CsvCovidData;
import org.tu.isn.server.model.CsvDataRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class DataPersister {

    public void createCsvFromDataset() {
        createCsvFromDataset(Collections.emptyList());
    }

    public void createCsvFromDataset(List<String> excludedCountries) {
        Path inputDataFile = Paths.get(System.getenv("INPUT_DATA_FILE_NAME") + "-" + UUID.randomUUID().toString());
    }

    public void createCsvFromFile(InputStream content) {
        Path inputDataFile = Paths.get(System.getenv("INPUT_DATA_FILE_NAME") + "-" + UUID.randomUUID().toString());
    }

    private void persistCsv(CsvCovidData data) {
        Path inputDataFile = Paths.get(System.getenv("INPUT_DATA_FILE_NAME") + "-" + UUID.randomUUID().toString());
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        Files.newOutputStream(inputDataFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                        StandardCharsets.UTF_8))) {
            writeHeaders(data.getHeaders(), writer);
            writeData(data.getData(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeaders(List<String> headers, BufferedWriter writer) throws IOException {
        for (int i = 0; i < headers.size(); i++) {
            writer.write(headers.get(i));
            if (i != headers.size() - 1) {
                writer.write(',');
            }
        }
        writer.newLine();
    }

    private void writeData(List<CsvDataRow> dataRows, BufferedWriter writer) throws IOException {
        for (CsvDataRow dataRow : dataRows) {
            writeDataRow(dataRow, writer);
            writer.newLine();
        }
    }

    private void writeDataRow(CsvDataRow dataRow, BufferedWriter writer) throws IOException {
        List<Integer> data = dataRow.getData();
        for (int i = 0; i < data.size(); i++) {
            writer.write(data.get(i));
            if (i != data.size() - 1) {
                writer.write(',');
            }
        }
    }

}
