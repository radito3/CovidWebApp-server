package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface DiagramDataRow {

    @JsonProperty("date")
    String getIdentifier();

    @JsonProperty("value")
    Integer getValue();

}
