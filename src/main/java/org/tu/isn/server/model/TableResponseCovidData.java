package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize
public interface TableResponseCovidData {

    @JsonProperty("table_headers")
    List<String> getHeaders();

    //rest of the data...

}
