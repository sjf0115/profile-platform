package com.data.connector.plugin;

import com.data.profile.common.domain.connector.param.type.InputParam;

public class ClickHouseConfigBuilder extends JdbcConfigBuilder {

    @Override
    protected InputParam getPasswordInput(boolean isEn) {
        return getInputParam("password",
                isEn ? "password" : "密码",
                isEn ? "please enter password" : "请填入密码", 1, null,
                null);
    }
}
