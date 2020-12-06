package org.tu.isn.server.service;

import org.springframework.stereotype.Component;
import org.tu.isn.server.model.CsvCovidData;
import org.tu.isn.server.model.RequestCovidData;

import java.nio.file.Path;

@Component
public class DataTransformer {

    public CsvCovidData transformJsonToCsv(RequestCovidData request) {

        return null;
    }

    public void transformFileFromJsonToCsv(Path file) {

    }

}
