package org.tu.isn.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public interface PaginatedResponse<T> {

    @JsonProperty("page")
    Integer getCurrentPage();

    @JsonProperty("total_pages")
    Integer getTotalPages();

    @JsonProperty("resources")
    List<T> getResources();

}
