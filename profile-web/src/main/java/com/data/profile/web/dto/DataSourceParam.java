package com.data.profile.web.dto;

import lombok.Data;

/**
 * 数据源查询参数
 */
@Data
public class DataSourceParam {
    // 数据源名称（模糊搜索）
    private String datasourceName;
    // 数据源类型
    private String datasourceType;
    // 状态: 1-启用, 2-停用
    private Integer status;
}
