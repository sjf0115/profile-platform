package com.data.profile.common.domain.connector;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecuteRequestParam extends ConnectorRequestParam {

    private String script;

    private String variables;

    private int limit = 1000;

    private int pageNumber = 0;

    private int pageSize = 0;
}
