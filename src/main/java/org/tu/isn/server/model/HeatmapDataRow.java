package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface HeatmapDataRow {

    @JsonProperty("coordinates")
    Coordinate getCoordinates();

    @JsonProperty("country")
    String getCountryName();

    @JsonProperty("deaths")
    Integer getDeaths();

    @JsonProperty("recovered")
    Integer getRecovered();

    @JsonProperty("active")
    Integer getActive();

}
