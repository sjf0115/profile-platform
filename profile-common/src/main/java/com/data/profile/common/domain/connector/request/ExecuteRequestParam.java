package com.data.profile.common.domain.connector.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecuteRequestParam extends ConnectorRequestParam {

    private String script;

    private String variables;

    private int limit = 1000;

    private int pageNumber = 0;

    private int pageSize = 0;

    /** 数据写入目标表名（insertData 场景） */
    private String tableName;

    /** 数据写入行（insertData 场景），每行为 列名 -> 值，各行 key 集合一致 */
    private List<Map<String, Object>> rows;
}
