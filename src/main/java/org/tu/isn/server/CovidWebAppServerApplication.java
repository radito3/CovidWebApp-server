package org.tu.isn.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@SpringBootApplication
public class CovidWebAppServerApplication {

    public static void main(String[] args) {
        Runtime.getRuntime()
               .addShutdownHook(new GracefulShutdown());
        SpringApplication.run(CovidWebAppServerApplication.class, args);
    }

    private static class GracefulShutdown extends Thread {

        private final HttpClient client = HttpClient.newBuilder()
                                                    .version(HttpClient.Version.HTTP_1_1)
                                                    .build();

        @Override
        public void run() {
            try {
                client.send(buildShutdownRequest(), HttpResponse.BodyHandlers.discarding());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private HttpRequest buildShutdownRequest() {
            return HttpRequest.newBuilder()
                              .POST(HttpRequest.BodyPublishers.noBody())
                              .uri(URI.create(System.getenv("PY_SCRIPT_URL") + "/shutdown"))
                              .timeout(Duration.ofSeconds(5))
                              .build();
        }
    }

}
