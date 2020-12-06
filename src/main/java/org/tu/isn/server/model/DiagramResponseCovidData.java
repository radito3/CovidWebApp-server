package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize
public interface DiagramResponseCovidData {

    @JsonProperty("x_name")
    String getXName();

    @JsonProperty("y_name")
    String getYName();

    @JsonProperty("x_value")
    double getXValue();

    @JsonProperty("y_value")
    double getYValue();

    //rest of the data...

}
