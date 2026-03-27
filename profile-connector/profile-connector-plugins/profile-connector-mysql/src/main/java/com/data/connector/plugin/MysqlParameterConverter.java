package com.data.connector.plugin;

import com.data.profile.common.utils.StringUtils;

import java.util.Map;

import static com.data.profile.common.domain.connector.ConfigConstants.*;

public class MysqlParameterConverter extends JdbcParameterConverter {

    @Override
    protected String getUrl(Map<String, Object> parameter) {
        String url = String.format("jdbc:mysql://%s:%s/%s",
                parameter.get(HOST),
                parameter.get(PORT),
                parameter.get(DATABASE));
        String properties = (String)parameter.get(PROPERTIES);
        if (StringUtils.isNotEmpty(properties)) {
            url += "?" + properties;
        }

        return url;
    }

}
