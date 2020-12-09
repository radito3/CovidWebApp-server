package org.tu.isn.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tu.isn.server.model.RequestCovidData;
import org.tu.isn.server.service.AnalysisService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    @Autowired
    private AnalysisService service;

    @PostMapping(path = "/analyze", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Void> startAnalysis(HttpServletRequest request, @RequestBody(required = false) RequestCovidData body) {
        if (request.getContentType()
                   .equals(MediaType.APPLICATION_JSON_VALUE)) {
            service.startAnalysisFromJson(body);
        } else if (request.getContentType()
                          .equals(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            service.startAnalysisFromFile(request);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .build();
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                             .build();
    }

}
