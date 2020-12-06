package org.tu.isn.server.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface DataRow {

    List<Double> getDataRow();
}
