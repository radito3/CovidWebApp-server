package org.tu.isn.server.docker;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class DockerService {

    private final HttpClient client = HttpClient.newBuilder()
                                                .version(HttpClient.Version.HTTP_1_1)
                                                .build();

    public void callScriptContainer() {
        try {
            int statusCode = client.send(buildRequest(), HttpResponse.BodyHandlers.discarding())
                                   .statusCode();
            if (statusCode / 100 != 2) {
                System.err.println("Analysis request wasn't processed correctly");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private HttpRequest buildRequest() {
        String requestUrl = System.getenv("PY_SCRIPT_URL") + "/analyze";
        String inputFileName = System.getenv("INPUT_DATA_FILE_NAME");
        return HttpRequest.newBuilder()
                          .POST(HttpRequest.BodyPublishers.ofString("{\"input_file_name\":\"" + inputFileName + "\"}"))
                          .uri(URI.create(requestUrl))
                          .build();
    }

}
