package org.tu.isn.server.service;

import org.springframework.stereotype.Service;
import org.tu.isn.server.model.DiagramResponseCovidData;
import org.tu.isn.server.model.HeatmapResponseCovidData;
import org.tu.isn.server.model.ImmutableTableResponseCovidData;
import org.tu.isn.server.model.TableResponseCovidData;

@Service
public class DataExtractionService {

    //TODO read file which is written by the python script and interpret it according to the desired request

    public TableResponseCovidData extractTableData() {
        return ImmutableTableResponseCovidData.builder()
                                              .addHeaders("header")
                                              .build();
    }

    public HeatmapResponseCovidData extractHeatmapData() {
        return null;
    }

    public DiagramResponseCovidData extractDiagramData() {
        return null;
    }

}
