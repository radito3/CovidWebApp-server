package org.tu.isn.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.tu.isn.server.docker.CallType;
import org.tu.isn.server.docker.DockerService;
import org.tu.isn.server.model.RequestCovidData;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

@Service
public class AnalysisService {

    @Autowired
    private DockerService dockerService;
    @Autowired
    private DataPersister dataPersister;

    public String startAnalysisFromJson(RequestCovidData data) {
        if (data == null) {
            String fileName = dataPersister.createCsvFromDataset();
            return dockerService.callScriptContainer(CallType.START_ANALYSIS, fileName);
        }
        String fileName = dataPersister.createCsvFromDataset(new HashSet<>(data.getExcludedCountries()));
        return dockerService.callScriptContainer(CallType.START_ANALYSIS, fileName);
    }

    public String startAnalysisFromFile(MultipartFile file) {
        try (InputStream content = file.getInputStream()) {
            String fileName = dataPersister.createCsvFromFile(content);
            return dockerService.callScriptContainer(CallType.START_ANALYSIS, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkStatus(String id) {
        return dockerService.callScriptContainer(CallType.POLL_STATUS, id);
    }

}
