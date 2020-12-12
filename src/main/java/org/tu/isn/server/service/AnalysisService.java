package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tu.isn.server.docker.CallType;
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

    public String startAnalysisFromJson(RequestCovidData data) {
        if (data == null) {
            dataPersister.createCsvFromDataset();
            return dockerService.callScriptContainer(CallType.START_ANALYSIS, null);
        }
        dataPersister.createCsvFromDataset(data.getExcludedCountries());
        return dockerService.callScriptContainer(CallType.START_ANALYSIS, null);
    }

    public String startAnalysisFromFile(MultipartFile file) {
        try (InputStream content = file.getInputStream()) {
            dataPersister.createCsvFromFile(content);
            return dockerService.callScriptContainer(CallType.START_ANALYSIS, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkStatus(String id) {
        return dockerService.callScriptContainer(CallType.POLL_STATUS, id);
    }

}
