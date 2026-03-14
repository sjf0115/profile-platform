package com.data.profile.model;

import lombok.Data;

// 数据源分类
@Data
public class DataSourceCategory {
    // 数据源分类ID
    private Integer id;
    // 数据源分类名称
    private String name;
    // 数据源类型个数
    private Integer count;
}