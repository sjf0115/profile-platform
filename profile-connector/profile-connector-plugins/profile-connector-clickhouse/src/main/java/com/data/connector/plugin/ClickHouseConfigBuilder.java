package com.data.connector.plugin;

import com.data.profile.common.domain.connector.param.type.InputParam;

public class ClickHouseConfigBuilder extends JdbcConfigBuilder {

    @Override
    protected InputParam getPasswordInput() {
        return getInputParam("password",
                "密码",
                "请填入密码", 1, null,
                null);
    }
}
