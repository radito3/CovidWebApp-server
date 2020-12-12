package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize
public interface TableDataRow {

    @JsonProperty("row_name")
    String getHeader();

    @JsonProperty("row_data")
    List<Integer> getData();

}
