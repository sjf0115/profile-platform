package com.data.connector.plugin;

import com.data.profile.common.domain.connector.jdbc.BaseJdbcDataSourceInfo;

import java.util.Map;

public class MysqlDataSourceInfo extends BaseJdbcDataSourceInfo {

    public MysqlDataSourceInfo(Map<String,String> param) {
        super(param);
    }

    @Override
    public String getAddress() {
        return "jdbc:mysql://"+getHost()+":"+getPort();
    }

    @Override
    public String getDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public String getType() {
        return "mysql";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

}
