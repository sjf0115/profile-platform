package com.data.profile.common.domain.connector.jdbc;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class TableColumnInfo {

    private String database;

    private String table;

    private List<String> primaryKeys;

    private List<ColumnInfo> columns;

    public TableColumnInfo(String table, List<String> primaryKeys, List<ColumnInfo> columns) {
        this(null, table, primaryKeys, columns);
    }

    public TableColumnInfo(String database, String table, List<String> primaryKeys, List<ColumnInfo> columns) {
        this.database = database;
        this.table = table;
        this.primaryKeys = primaryKeys;
        this.columns = columns;

        if (CollectionUtils.isNotEmpty(columns) && CollectionUtils.isNotEmpty(primaryKeys)) {
            columns.forEach(columnInfo -> {
                if (primaryKeys.contains(columnInfo.getName())) {
                    columnInfo.setPrimaryKey(true);
                }
            });
        }
    }
}
