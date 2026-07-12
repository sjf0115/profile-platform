package com.data.profile.web.dto;

import lombok.Data;

/**
 * 数据源创建/编辑请求
 */
@Data
public class DataSourceRequest {
    // 数据源名称
    private String datasourceName;
    // 数据源描述
    private String datasourceDesc;
    // 数据源类型
    private String datasourceType;
    // 负责人
    private String owner;
    // 数据源配置(JSON)
    private String config;
}
