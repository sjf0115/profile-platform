package com.data.profile.common.domain.connector;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetTablesRequestParam extends ConnectorRequestParam {
    private String database;
}
