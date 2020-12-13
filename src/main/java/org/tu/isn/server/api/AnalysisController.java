package org.tu.isn.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tu.isn.server.model.RequestData;
import org.tu.isn.server.service.AnalysisService;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    @Autowired
    private AnalysisService service;

    @PostMapping(path = "/analyze_existing_data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startAnalysisFromJson(@RequestBody(required = false) RequestData body) {
        String id = service.startAnalysisFromJson(body);
        if (id != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                                 .body(id);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .build();
    }

    @PostMapping(path = "/analyze_user_data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startAnalysisFromFile(MultipartFile file) {
        String id = service.startAnalysisFromFile(file);
        if (id != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                                 .body(id);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .build();
    }

    @GetMapping(path = "/status/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getStatus(@PathVariable("id") String id) {
        boolean done = service.checkStatus(id);
        return ResponseEntity.ok(done ? "done" : "processing");
    }

}
