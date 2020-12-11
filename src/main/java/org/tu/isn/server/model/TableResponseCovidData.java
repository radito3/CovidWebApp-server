package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Value.Enclosing
@JsonDeserialize
public interface TableResponseCovidData {

    @JsonProperty("table_headers")
    List<String> getHeaders();

    @JsonProperty("table_rows")
    List<TableRow> getRows();

    @Value.Immutable
    @JsonDeserialize
    interface TableRow {

        @JsonProperty("table_row_data")
        List<Integer> getData();
    }

}
