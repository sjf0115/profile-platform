package com.data.profile.common.domain.connector.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetColumnsRequestParam extends ConnectorRequestParam {

    private String dataBase;

    private String table;
}
