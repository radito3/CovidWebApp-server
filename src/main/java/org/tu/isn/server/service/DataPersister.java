package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tu.isn.server.datasets.DatasetParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Component
public class DataPersister {

    @Autowired
    private DatasetParser datasetParser;

    public String createCsvFromDataset() {
        return createCsvFromDataset(Collections.emptySet());
    }

    public String createCsvFromDataset(Set<String> excludedCountries) {
        String inputDataFileName = System.getenv("INPUT_DATA_FILE_NAME") + "-" + UUID.randomUUID().toString();
        Path inputDataFile = Paths.get(inputDataFileName);
        try {
            transferContentToCsv(datasetParser.getContent(), inputDataFile,
                                 line -> !excludedCountries.contains(line.split(",")[datasetParser.getCountryNameIndex()]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputDataFileName;
    }

    public String createCsvFromFile(InputStream content) {
        String inputDataFileName = System.getenv("INPUT_DATA_FILE_NAME") + "-" + UUID.randomUUID().toString();
        Path inputDataFile = Paths.get(inputDataFileName);
        transferContentToCsv(content, inputDataFile, line -> true);
        return inputDataFileName;
    }

    private void transferContentToCsv(InputStream content, Path inputFile, Predicate<String> countryFilter) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        Files.newOutputStream(inputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                        StandardCharsets.UTF_8))) {
            reader.readLine(); //skip headers
            writeHeaders(writer);
            writeData(reader, writer, countryFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHeaders(BufferedWriter writer) throws IOException {
        List<String> headers = List.of("Date", "Country", "Latitude", "Longitude", "Deaths", "Recovered", "Active");
        for (int i = 0; i < headers.size(); i++) {
            writer.write(headers.get(i));
            if (i != headers.size() - 1) {
                writer.write(',');
            }
        }
        writer.newLine();
    }

    private void writeData(BufferedReader reader, BufferedWriter writer, Predicate<String> countryFilter) throws IOException {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String countryWithoutRegion = stripRegionFromCountry(line);
            if (countryFilter.test(countryWithoutRegion)) {
                writeDataRow(countryWithoutRegion, writer);
                writer.newLine();
            }
        }
    }

    private void writeDataRow(String line, BufferedWriter writer) throws IOException {
        String[] parts = line.split(",");
        int partsLimit = datasetParser.getNumberOfFields();
        for (int i = 0; i < partsLimit; i++) {
            writer.write(parts[i]);
            if (i != partsLimit - 1) {
                writer.write(',');
            }
        }
    }

    private String stripRegionFromCountry(String line) {
        if (line.contains("(")) {
            return line.substring(0, line.indexOf('(')).stripTrailing();
        }
        return line;
    }

}
