package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface TableDataRow {

    @JsonProperty("date")
    String getDate();

    @JsonProperty("country")
    String getCountry();

    @JsonProperty("confirmed")
    Integer getConfirmed();

    @JsonProperty("deaths")
    Integer getDeaths();

    @JsonProperty("recovered")
    Integer getRecovered();

    @JsonProperty("active")
    Integer getActive();

    @JsonProperty("new_cases")
    Integer getNewCases();

    @JsonProperty("new_deaths")
    Integer getNewDeaths();

    @JsonProperty("new_recovered")
    Integer getNewRecovered();



}
