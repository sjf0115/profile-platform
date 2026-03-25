package com.data.profile.common.domain.sql;

import lombok.Data;

import java.io.Serializable;

@Data
public class QueryColumn implements Serializable {

    private static final long serialVersionUID = -2398995167525051291L;

    private String name;

    private String type;

    private String comment;

    public QueryColumn() {
    }

    public QueryColumn(String name, String type) {
        this(name, type, null);
    }

    public QueryColumn(String name, String type, String comment) {
        this.name = name;
        this.type = type;
        this.comment = comment;
    }
}
