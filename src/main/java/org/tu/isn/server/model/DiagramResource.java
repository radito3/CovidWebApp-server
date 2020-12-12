package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize
public interface DiagramResource {

    @JsonProperty("present_data")
    List<DiagramDataRow> getPresentData();

    @JsonProperty("predicted_data")
    List<DiagramDataRow> getPredictedData();

}
