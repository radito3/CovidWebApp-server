package org.tu.isn.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tu.isn.server.model.DiagramResponseCovidData;
import org.tu.isn.server.model.HeatmapResponseCovidData;
import org.tu.isn.server.model.TableResponseCovidData;
import org.tu.isn.server.service.DataExtractionService;

@RestController
@RequestMapping("/api/v1/data/{id}")
public class DataController {

    @Autowired
    private DataExtractionService dataExtractor;

    @GetMapping(path = "/heatmap", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HeatmapResponseCovidData> getHeatmapStatus(@PathVariable(name = "id") String operationId,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "0") String page,
                                                                     @RequestParam(name = "aggregate_by", required = false, defaultValue = "day") String aggregateBy) {
        return ResponseEntity.ok(dataExtractor.extractHeatmapData(operationId, Integer.parseInt(page), aggregateBy));
    }

    @GetMapping(path = "/diagram", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DiagramResponseCovidData> getDiagramStatus(@PathVariable(name = "id") String operationId,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "0") String page,
                                                                     @RequestParam(name = "country") String country) {
        return ResponseEntity.ok(dataExtractor.extractDiagramData(operationId, Integer.parseInt(page), country));
    }

    @GetMapping(path = "/table", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TableResponseCovidData> getTableViewStatus(@PathVariable(name = "id") String operationId,
                                                                     @RequestParam(name = "page", required = false, defaultValue = "0") String page) {
        return ResponseEntity.ok(dataExtractor.extractTableData(operationId, Integer.parseInt(page)));
    }

}
