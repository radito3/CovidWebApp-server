package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tu.isn.server.docker.DockerService;
import org.tu.isn.server.model.RequestCovidData;

import java.io.IOException;
import java.io.InputStream;

@Service
public class AnalysisService {

    @Autowired
    private DockerService dockerService;
    @Autowired
    private DataPersister dataPersister;

    public void startAnalysisFromJson(RequestCovidData data) {
        if (data == null) {
            dataPersister.createCsvFromDataset();
            dockerService.callScriptContainer();
            return;
        }
        dataPersister.createCsvFromDataset(data.getExcludedCountries());
        dockerService.callScriptContainer();
    }

    public void startAnalysisFromFile(MultipartFile file) {
        try (InputStream content = file.getInputStream()) {
            dataPersister.createCsvFromFile(content);
            dockerService.callScriptContainer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
