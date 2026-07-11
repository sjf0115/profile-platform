package com.data.notification.plugin.wecom.utils;

import java.util.HashMap;
import java.util.Map;

public class ContentUtil {

    private ContentUtil() {
        throw new IllegalStateException(ContentUtil.class.getName());
    }

    public static Map<String, Object> createParamMap(Object... elements) {
        Map<String, Object> paramMap = new HashMap<>(32);
        if (elements.length % 2 == 1) {
            throw new IllegalArgumentException("params length must be even!");
        }
        for (int i = 0; i < elements.length / 2; i++) {
            Object key = elements[2 * i];
            Object value = elements[2 * i + 1];
            if (key == null) {
                continue;
            }
            paramMap.put(key.toString(), value == null ? "" : value);
        }
        return paramMap;
    }
}
