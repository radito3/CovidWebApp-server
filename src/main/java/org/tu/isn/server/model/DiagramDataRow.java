package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize
public abstract class DiagramDataRow {

    @JsonProperty("day")
    abstract String getIdentifier();

    @JsonProperty("value")
    abstract int getValue();

    @Value.Check
    public void validateIdentifier() {
        if (!getIdentifier().matches("\\d\\d\\.\\d\\d")) { //e.g. 20.12 (December 20th)
            throw new IllegalStateException("Invalid day format");
        }
    }

}
