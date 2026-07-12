package com.data.profile.web.dto;

import lombok.Data;

/**
 * 投递查询参数
 */
@Data
public class ExportParam {
    // 投递名称（模糊搜索）
    private String exportName;
    // 状态: 1-启用, 2-停用
    private Integer status;
    // 投递方式: 1-数据源, 2-应用
    private Integer exportMode;
}
