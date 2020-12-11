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
        return ResponseEntity.ok(dataExtractor.extractHeatmapData());
    }

    @GetMapping(path = "/diagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiagramResponseCovidData> getDiagramStatus() {
        return ResponseEntity.ok(dataExtractor.extractDiagramData());
    }

    @GetMapping(path = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TableResponseCovidData> getTableViewStatus() {
        return ResponseEntity.ok(dataExtractor.extractTableData());
    }

}
