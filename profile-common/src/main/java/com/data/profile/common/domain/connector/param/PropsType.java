package com.data.profile.common.domain.connector.param;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * form pros type
 */
public enum PropsType {
    /**
     * 0-text
     * 1-password
     * 2-textarea
     */
    TEXT(0,"text"),
    PASSWORD(1,"password"),
    TEXTAREA(2,"textarea");

    PropsType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    private final int code;

    private final String description;

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    private static final Map<Integer, PropsType> VALUES_MAP = new HashMap<>();

    static {
        for (PropsType type : PropsType.values()) {
            VALUES_MAP.put(type.code,type);
        }
    }

    public static PropsType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}
