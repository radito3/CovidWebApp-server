package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCovidData.class)
@JsonDeserialize(as = ImmutableCovidData.class)
public interface CovidData {

    @JsonProperty("_data_")
    String data();

}
