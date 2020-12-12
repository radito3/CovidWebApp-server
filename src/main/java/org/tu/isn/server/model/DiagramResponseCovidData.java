package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize
public interface DiagramResponseCovidData extends PaginatedResponse<DiagramDataRow> {

    @JsonProperty("x_axis_name")
    String getAbscissaValueName();

    @JsonProperty("x_axis_div")
    List<String> getAbscissaValueDivisions();

    @JsonProperty("y_axis_name")
    String getOrdinateValeName();

    @JsonProperty("y_axis_div")
    List<Integer> getOrdinateValueDivisions();

}
