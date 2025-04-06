package com.data.profile.manager.core;

import com.data.profile.manager.domain.Column;
import com.data.profile.manager.domain.ConnectionParam;
import com.data.profile.manager.domain.Table;

import java.sql.SQLException;
import java.util.List;

public interface MetaService {
    List<Table> getTables(ConnectionParam params) throws SQLException;
    List<Column> getColumns(ConnectionParam params, String dbName, String tableName) throws SQLException;
}
