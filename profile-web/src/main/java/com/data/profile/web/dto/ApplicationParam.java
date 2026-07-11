package com.data.profile.web.dto;

import lombok.Data;

/**
 * 应用查询参数
 */
@Data
public class ApplicationParam {
    // 应用名称（模糊搜索）
    private String appName;
    // 状态: 1-启用, 2-停用
    private Integer status;
    // 创建方式: 1-系统内置, 2-自定义
    private Integer sourceType;
}
