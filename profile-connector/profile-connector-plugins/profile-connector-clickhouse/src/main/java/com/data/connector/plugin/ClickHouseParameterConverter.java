package com.data.connector.plugin;

import com.data.conenctor.plugin.JdbcParameterConverter;
import java.util.Map;
import static com.data.profile.common.domain.connector.ConfigConstants.*;

public class ClickHouseParameterConverter extends JdbcParameterConverter {

    @Override
    protected String getUrl(Map<String, Object> parameter) {
         return String.format("jdbc:clickhouse://%s:%s/%s",
                parameter.get(HOST),
                parameter.get(PORT),
                parameter.get(DATABASE));
    }
}
