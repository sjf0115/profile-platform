package com.data.profile.common.domain.connector.param;

import java.util.HashMap;
import java.util.Map;

/**
 * frontend form type
 */
public enum FormType {
    /**
     * 0-input
     * 1-radio
     * 2-select
     * 3-checkbox
     * 4-cascader
     * 5-textarea
     */
    INPUT(0,"input"),
    RADIO(1,"radio"),
    SELECT(2,"select"),
    SWITCH(3,"checkbox"),
    CASCADER(4,"cascader"),
    TEXTAREA(5,"textarea"),
    GROUP(6,"group");

    FormType(int code, String description) {
        this.code = code;
        this.description = description;
    }


    private final int code;

    private final String description;

    public int getCode() {
        return code;
    }


    public String getDescription() {
        return description;
    }

    private static final Map<Integer, FormType> VALUES_MAP = new HashMap<>();

    static {
        for (FormType type : FormType.values()) {
            VALUES_MAP.put(type.code,type);
        }
    }

    public static FormType of(Integer status) {
        if (VALUES_MAP.containsKey(status)) {
            return VALUES_MAP.get(status);
        }
        throw new IllegalArgumentException("invalid code : " + status);
    }
}