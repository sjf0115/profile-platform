package com.data.profile.common.domain.connector.request;

import lombok.Data;

@Data
public class ConnectorRequestParam {

    private String type;

    protected String dataSourceParam;
}
