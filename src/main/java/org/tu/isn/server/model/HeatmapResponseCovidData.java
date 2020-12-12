package org.tu.isn.server.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize
public interface HeatmapResponseCovidData extends PaginatedResponse<HeatmapDataRow> {

}
