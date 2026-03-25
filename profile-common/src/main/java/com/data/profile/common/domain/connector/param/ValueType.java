package com.data.profile.common.domain.connector.param;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * rule input entry value type
 */
public enum ValueType {
    /**
     * 0-string
     * 1-list
     * 2-number
     * 3-sql
     */
    STRING(0,"string"),
    LIST(1,"list"),
    NUMBER(2,"number"),
    LIKE_SQL(3,"sql");

    ValueType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;
    private final String description;

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    private static final Map<Integer, ValueType> VALUES_MAP = new HashMap<>();

    static {
        for (ValueType type : ValueType.values()) {
            VALUES_MAP.put(type.code,type);
        }
    }

    public static ValueType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}