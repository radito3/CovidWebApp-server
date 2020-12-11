package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize
public interface DiagramResponseCovidData {

    @JsonProperty("x_axis_name")
    String getAbscissaValueName();

    @JsonProperty("x_axis_div")
    List<String> getAbscissaValueDivisions();

    @JsonProperty("y_axis_name")
    String getOrdinateValeName();

    @JsonProperty("y_axis_div")
    List<Integer> getOrdinateValueDivisions();

    @JsonProperty("present_data")
    List<DiagramDataRow> getPresentData();

    @JsonProperty("predicted_data")
    List<DiagramDataRow> getPredictedData();

}
