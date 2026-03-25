package com.data.connector.api;

import com.data.profile.common.domain.connector.request.*;

import java.sql.SQLException;
import java.util.List;

public interface Connector {

    /**
     * get databases
     * @param param GetDatabasesRequestParam
     */
    default ConnectorResponse getDatabases(GetDatabasesRequestParam param) throws SQLException {
        return null;
    }

    /**
     * get tables
     * @param param GetTablesRequestParam
     */
    default ConnectorResponse getTables(GetTablesRequestParam param) throws SQLException {
        return null;
    }

    /**
     * get columns
     * @param param GetColumnsRequestParam
     */
    default ConnectorResponse getColumns(GetColumnsRequestParam param) throws SQLException {
        return null;
    }

    /**
     * get partitions
     * @param param ConnectorRequestParam
     */
    default ConnectorResponse getPartitions(ConnectorRequestParam param) {
        return null;
    }

    /**
     * test connect
     * @param param TestConnectionRequestParam
     * @return
     */
    default ConnectorResponse testConnect(TestConnectionRequestParam param) {
        return null;
    }

    List<String> keyProperties();
}
