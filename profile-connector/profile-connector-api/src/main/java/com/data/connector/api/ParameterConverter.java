package com.data.connector.api;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;

import static com.data.profile.common.domain.connector.ConfigConstants.*;

public interface ParameterConverter {

    Map<String,Object> converter(Map<String,Object> parameter);

    default String getConnectorUUID(Map<String,Object> parameter) {
        Map<String, Object> convertResult = converter(parameter);
        return DigestUtils.md5Hex(
                String.valueOf(convertResult.get(URL)) +
                convertResult.get(TABLE) +
                convertResult.get(USER) +
                convertResult.get(PASSWORD));
    }
}
