package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize
public abstract class DiagramDataRow {

    @JsonProperty("date")
    abstract String getIdentifier();

    @JsonProperty("value")
    abstract int getValue();

    @Value.Check
    public void validateIdentifier() {
        if (!getIdentifier().matches("\\d{2}\\.\1\\.\\d{4}")) {
            throw new IllegalStateException("Invalid day format");
        }
    }

}
