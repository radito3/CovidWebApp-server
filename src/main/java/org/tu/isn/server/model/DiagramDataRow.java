package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize
public interface DiagramDataRow {

    @JsonProperty("date")
    String getIdentifier();

    @JsonProperty("value")
    Integer getValue();

}
