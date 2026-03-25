package com.data.profile.common.domain.connector.jdbc;

import java.util.Map;

public interface IJdbcDataSourceInfo {

    BaseJdbcDataSourceInfo getDatasourceInfo(Map<String,String> param);
}
