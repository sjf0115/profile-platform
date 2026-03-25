package com.data.profile.common.domain.connector.jdbc;

import lombok.Data;

@Data
public class TableInfo {

    private String database;

    private String name;

    private String type;

    private String comment;

    private String owner;

    private String createTime;

    public TableInfo() {}

    public TableInfo(String database, String name, String type, String comment) {
        this.database = database;
        this.name = name;
        this.type = type;
        this.comment = comment;
    }
}
