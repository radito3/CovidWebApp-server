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

    public String callScriptContainer(CallType callType, String arg) {
        try {
            if (callType == CallType.START_ANALYSIS) {
                return startAnalysis(arg);
            }
            return pollStatus(arg);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String startAnalysis(String fileName) throws IOException, InterruptedException {
        HttpResponse<Void> response = client.send(buildPostRequest(fileName), HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() / 100 != 2) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('_') + 1, fileName.indexOf('.'));
    }

    private String pollStatus(String id) throws IOException, InterruptedException {
        int statusCode = client.send(buildGetRequest(id), HttpResponse.BodyHandlers.discarding())
                               .statusCode();
        return Boolean.toString(statusCode == 200);
    }

    private HttpRequest buildPostRequest(String fileName) {
        String requestUrl = System.getenv("PY_SCRIPT_URL") + "/analyze";
        return HttpRequest.newBuilder()
                          .POST(HttpRequest.BodyPublishers.ofString("{\"input_file_name\":\"" + fileName + "\"}"))
                          .uri(URI.create(requestUrl))
                          .build();
    }

    private HttpRequest buildGetRequest(String id) {
        String requestUrl = System.getenv("PY_SCRIPT_URL") + "/status/" + id;
        return HttpRequest.newBuilder()
                          .GET()
                          .uri(URI.create(requestUrl))
                          .build();
    }

}
