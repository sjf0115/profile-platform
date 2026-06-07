package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Engine {
    private Long id;
    // 引擎状态:1-启用,2-停用
    private Integer status;
    // 引擎ID
    private String engineId;
    // 引擎名称
    private String engineName;
    // 引擎类别:analysis-分析引擎,di-数据集成引擎
    private String engineCategory;
    // 引擎类型:clickhouse,doris,spark,flink
    private String engineType;
    // 引擎描述
    private String engineDesc;
    // 是否默认引擎:0-否,1-是
    private Integer isDefault;
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 引擎配置(JSON格式)
    private String config;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
