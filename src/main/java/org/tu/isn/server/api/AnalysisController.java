package org.tu.isn.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tu.isn.server.model.RequestCovidData;
import org.tu.isn.server.service.AnalysisService;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    @Autowired
    private AnalysisService service;

    @PostMapping(path = "/analyze_existing_data", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> startAnalysisFromJson(@RequestBody(required = false) RequestCovidData body) {
        service.startAnalysisFromJson(body);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                             .build();
    }

    @PostMapping(path = "/analyze_user_data", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> startAnalysisFromFile(MultipartFile file) {
        service.startAnalysisFromFile(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                             .build();
    }

}
