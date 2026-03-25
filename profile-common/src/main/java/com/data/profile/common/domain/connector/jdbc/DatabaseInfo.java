package com.data.profile.common.domain.connector.jdbc;

import lombok.Data;

@Data
public class DatabaseInfo {

    private String name;

    private String type;

    public DatabaseInfo() {
    }

    public DatabaseInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
