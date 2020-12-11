package org.tu.isn.server.docker;

import org.springframework.stereotype.Service;

import java.net.http.HttpClient;

@Service
public class DockerService {

    private HttpClient client = HttpClient.newBuilder()
                                          .version(HttpClient.Version.HTTP_1_1)
                                          .build();

    public void callScriptContainer() {
//        client.send(POST python:5000/analyze {input_file=<file_name>}, discard_body)
    }

}
