package org.tu.isn.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.tu.isn.server.service.AnalysisService;

import javax.servlet.http.HttpServletRequest;

@RestController
public class Controller {
    //TODO - what endpoints is it going to have? - could be 1 POST /analyze & rest GETs for the different type of data, or like now
    // - what jsons is it going to receive/send - sync with Illyan

    @Autowired
    private AnalysisService service;

    @GetMapping(path = "/heatmap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getHeatmapStatus() {
        return ResponseEntity.ok("test");
    }

    @PostMapping(path = "/heatmap", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> startHeatmapAnalysis(HttpServletRequest request, @RequestBody String body) {
        //call .py file with appropriate arguments
        return ResponseEntity.ok()
                             .build();
    }

    @GetMapping(path = "/diagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDiagramStatus() {
        return ResponseEntity.ok("test");
    }

    @PostMapping(path = "/diagram", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> startDiagramAnalysis(HttpServletRequest request, @RequestBody String body) {
        //call .py file with appropriate arguments
        return ResponseEntity.ok()
                             .build();
    }

    @GetMapping(path = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTableViewStatus() {
        return ResponseEntity.ok("test");
    }

    @PostMapping(path = "/table", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> startTableViewAnalysis(HttpServletRequest request, @RequestBody String body) {
        //call .py file with appropriate arguments
        return ResponseEntity.ok()
                             .build();
    }
}
