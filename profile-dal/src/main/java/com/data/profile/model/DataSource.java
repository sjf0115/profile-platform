package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSource {
    private Long id;
    // 数据源状态
    private Integer status;
    // 数据源ID
    private String datasourceId;
    // 数据源名称
    private String datasourceName;
    // 数据源描述信息
    private String datasourceDesc;
    // 数据源类型ID
    private String schemaId;
    // 数据源类型名称
    private String schemaName;
    // 数据源类型分类
    private Integer schemaType;
    // 数据源创建方式：内置/自定义
    private Integer sourceType;
    // 数据源负责人
    private String owner;
    // 数据源创建人
    private String creator;
    // 数据源修改者
    private String modifier;
    // 数据源创建时间
    private Date gmtCreate;
    // 数据源修改时间
    private Date gmtModified;
    // 数据源配置信息
    private DataSourceConfig config;
    // 数据源样式信息
    private List<SchemaConfig> configTemplate;
}