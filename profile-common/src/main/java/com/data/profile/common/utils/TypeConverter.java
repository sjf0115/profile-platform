package com.data.profile.common.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 功能：数据类型转换
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/13 10:16
 */
public class TypeConverter {
    private static final Map<String, String> TYPE_MAPPING = Maps.newHashMap();

    static {
        TYPE_MAPPING.put("tinyint", "Int8");
        TYPE_MAPPING.put("smallint", "Int16");
        TYPE_MAPPING.put("int", "Int32");
        TYPE_MAPPING.put("bigint", "Int64");
        TYPE_MAPPING.put("float", "Float32");
        TYPE_MAPPING.put("double", "Float64");
        TYPE_MAPPING.put("string", "String");
        TYPE_MAPPING.put("timestamp", "DateTime");
        TYPE_MAPPING.put("date", "Date");
    }

    public static String convert(String type) {
        String cleanType = type.replaceAll("<.*?>", "").toLowerCase();
        return TYPE_MAPPING.getOrDefault(cleanType, "String");
    }

    public static String handleComplexType(String type) {
        if (type.startsWith("array<")) {
            String innerType = type.substring(6, type.length()-1);
            return "Array(" + convert(innerType) + ")";
        }
        if (type.startsWith("map<")) {
            return "Nested(keys String, values String)";
        }
        return convert(type);
    }
}
