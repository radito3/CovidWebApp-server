package org.tu.isn.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootApplication
public class CovidWebAppServerApplication {

    public static void main(String[] args) {
        Runtime.getRuntime()
               .addShutdownHook(new GracefulShutdown());
        SpringApplication.run(CovidWebAppServerApplication.class, args);
    }

    private static class GracefulShutdown extends Thread {
        @Override
        public void run() {
            try {
                int statusCode = HttpClient.newBuilder()
                                           .version(HttpClient.Version.HTTP_1_1)
                                           .build()
                                           .send(buildRequest(), HttpResponse.BodyHandlers.discarding())
                                           .statusCode();
                if (statusCode / 100 != 2) {
                    System.err.println("Python script didn't exit correctly");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private HttpRequest buildRequest() {
            return HttpRequest.newBuilder()
                              .POST(HttpRequest.BodyPublishers.noBody())
                              .uri(URI.create(System.getenv("PY_SCRIPT_URL") + "/shutdown"))
                              .build();
        }
    }

}
