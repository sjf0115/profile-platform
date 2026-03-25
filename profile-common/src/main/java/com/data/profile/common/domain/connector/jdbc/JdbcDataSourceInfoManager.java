package com.data.profile.common.domain.connector.jdbc;

import com.data.profile.common.utils.Md5Utils;

import java.util.concurrent.ConcurrentHashMap;

public class JdbcDataSourceInfoManager {

    private static final ConcurrentHashMap<String, BaseJdbcDataSourceInfo> DATA_SOURCE_INFO_MAP =
            new ConcurrentHashMap<>();

    public static BaseJdbcDataSourceInfo getDatasourceInfo(String param) {
        BaseJdbcDataSourceInfo dataSourceInfo = null;

        String key = Md5Utils.getMd5(param, false);
        dataSourceInfo = DATA_SOURCE_INFO_MAP.get(key);

        return dataSourceInfo;
    }

    public static BaseJdbcDataSourceInfo getDatasourceInfo(String param, BaseJdbcDataSourceInfo defaultSourceInfo) {
        BaseJdbcDataSourceInfo dataSourceInfo = null;

        String key = Md5Utils.getMd5(param, false);
        dataSourceInfo = DATA_SOURCE_INFO_MAP.get(key);
        if (dataSourceInfo == null) {
            putDataSourceInfo(key, defaultSourceInfo);
            dataSourceInfo = defaultSourceInfo;
        }

        return dataSourceInfo;
    }

    public static void putDataSourceInfo(String key, BaseJdbcDataSourceInfo dataSourceInfo) {
        DATA_SOURCE_INFO_MAP.put(key,dataSourceInfo);
    }
}
