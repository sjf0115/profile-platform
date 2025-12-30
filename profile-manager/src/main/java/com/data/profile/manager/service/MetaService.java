package com.data.profile.manager.service;

import com.data.profile.manager.domain.Column;
import com.data.profile.manager.domain.ConnectionParam;
import com.data.profile.manager.domain.Table;

import java.sql.SQLException;
import java.util.List;

public interface MetaService {
    List<Table> getTables(ConnectionParam param) throws SQLException;
    List<Column> getColumns(ConnectionParam param, String tableName) throws SQLException;
}
