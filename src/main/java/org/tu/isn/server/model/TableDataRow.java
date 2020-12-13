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

    @JsonProperty("deaths")
    Integer getDeaths();

    @JsonProperty("recovered")
    Integer getRecovered();

    @JsonProperty("active")
    Integer getActive();

}
