package com.data.conenctor.plugin;
import com.data.connector.api.ParameterConverter;

import java.util.HashMap;
import java.util.Map;

import static com.data.profile.common.domain.connector.ConfigConstants.*;

public abstract class JdbcParameterConverter implements ParameterConverter {

    @Override
    public Map<String, Object> converter(Map<String, Object> parameter) {
        Map<String,Object> config = new HashMap<>();
        config.put(SRC_CONNECTOR_TYPE, parameter.get(SRC_CONNECTOR_TYPE));
        config.put(TABLE,parameter.get(TABLE));
        config.put(USER,parameter.get(USER));
        config.put(PASSWORD, parameter.get(PASSWORD));
        config.put(DATABASE, parameter.get(DATABASE));
        config.put(CATALOG, parameter.get(CATALOG));
        config.put(SCHEMA, parameter.get(SCHEMA));
        config.put(URL, parameter.get(URL) == null ? getUrl(parameter) : parameter.get(URL));
        return config;
    }

    protected abstract String getUrl(Map<String, Object> parameter);
}
