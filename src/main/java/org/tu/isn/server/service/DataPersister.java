package org.tu.isn.server.service;

import org.springframework.stereotype.Component;
import org.tu.isn.server.model.CsvCovidData;

@Component
public class DataPersister {

    public void persistCsv(CsvCovidData data) {
        //TODO write to filesystem (which docker will forward to the attached volume)
    }

}
