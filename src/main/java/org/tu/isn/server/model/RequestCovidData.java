package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface RequestCovidData {

    @JsonProperty("test")
    String test();

    //TODO figure out the fields that the server will consume from the Web UI
}
