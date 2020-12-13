package org.tu.isn.server.service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum AggregateType {
    DAY(1),
    WEEK(7),
    MONTH(30);

    private final int daysMapped;

    AggregateType(int daysMapped) {
        this.daysMapped = daysMapped;
    }

    private static final Map<String, AggregateType> NAMES_TO_VALUES = Arrays.stream(AggregateType.values())
        .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));

    public static AggregateType of(String aggregateType) {
        AggregateType type = NAMES_TO_VALUES.get(aggregateType);
        if (type == null) {
            throw new IllegalArgumentException("Invalid aggregate type");
        }
        return type;
    }

    public int getDaysMapped() {
        return daysMapped;
    }
}
