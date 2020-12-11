package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize
public interface HeatmapResponseCovidData {

    @JsonProperty("coordinates")
    List<Coordinate> getCoordinates();

    @JsonProperty("countries")
    List<String> getCountries();

    //TODO either casualties per country or {infected, deaths, recovered} per country

}
