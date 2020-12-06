package org.tu.isn.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tu.isn.server.model.DiagramResponseCovidData;
import org.tu.isn.server.model.HeatmapResponseCovidData;
import org.tu.isn.server.model.TableResponseCovidData;
import org.tu.isn.server.service.DataExtractionService;

@RestController
@RequestMapping("/api/v1")
public class DataController {

    @Autowired
    private DataExtractionService dataExtractor;

    @GetMapping(path = "/heatmap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HeatmapResponseCovidData> getHeatmapStatus() {
        //TODO json will probably need to contain latitude/longitude coordinates along with the data of infected/recovered/deaths
        return ResponseEntity.ok()
            .build();
    }

    @GetMapping(path = "/diagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiagramResponseCovidData> getDiagramStatus() {
        //TODO json will depend on the diagram
        return ResponseEntity.ok()
            .build();
    }

    @GetMapping(path = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TableResponseCovidData> getTableViewStatus() {
        //TODO json will probably only need to contain the data of infected/recovered/deaths per time slice (past/present/future)
        return ResponseEntity.ok()
            .build();
    }

}
