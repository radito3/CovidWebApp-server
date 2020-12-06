package org.tu.isn.server.service;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tu.isn.server.docker.DockerService;
import org.tu.isn.server.model.CsvCovidData;
import org.tu.isn.server.model.RequestCovidData;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AnalysisService {

    @Autowired
    private DockerService dockerService;
    @Autowired
    private DataTransformer dataTransformer;
    @Autowired
    private DataPersister dataPersister;

    public void startAnalysisFromJson(RequestCovidData data) {
        CsvCovidData csv = dataTransformer.transformJsonToCsv(data);
        dataPersister.persistCsv(csv);
        dockerService.runScriptImage();
    }

    public void startAnalysisFromFile(HttpServletRequest request) {
        try {
            Path tempFile = writeFileFromRequest(request);
            dataTransformer.transformFileFromJsonToCsv(tempFile);
            dockerService.runScriptImage();
            Files.delete(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Path writeFileFromRequest(HttpServletRequest request) throws IOException {
        FileItemIterator fileIterator = new ServletFileUpload().getItemIterator(request);
        Path tempFile = Files.createTempFile("temp-data", null);
        try (OutputStream fileWriter = Files.newOutputStream(tempFile)) {
            while (fileIterator.hasNext()) {
                FileItemStream item = fileIterator.next();
                if (item.isFormField()) {
                    continue; // ignore simple (non-file) form fields
                }
                try (InputStream fileContent = item.openStream()) {
                    fileContent.transferTo(fileWriter);
                }
            }
        }
        return tempFile;
    }

}
