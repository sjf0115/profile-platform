package com.data.profile.common.enums;

import java.util.HashMap;

public enum DataType {
    /**
     *
     */
    NULL_TYPE,
    BOOLEAN_TYPE,
    BYTE_TYPE,
    SHORT_TYPE,
    INT_TYPE,
    LONG_TYPE,
    FLOAT_TYPE,
    DOUBLE_TYPE,
    TIME_TYPE,
    DATE_TYPE,
    TIMESTAMP_TYPE,
    STRING_TYPE,
    BYTES_TYPE,
    BIG_DECIMAL_TYPE,
    OBJECT;

    private static final HashMap<String, DataType> DATA_TYPE_MAP = new HashMap<>();

    static {
        for (DataType dataType: DataType.values()){
            DATA_TYPE_MAP.put(dataType.name(), dataType);
        }
    }

    public static DataType of(String dataType){
        if(DATA_TYPE_MAP.containsKey(dataType)){
            return DATA_TYPE_MAP.get(dataType);
        }
        throw new IllegalArgumentException("invalid data type : " + dataType);
    }
}
