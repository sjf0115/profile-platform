package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 数据源视图对象
 */
@Data
public class DataSourceVO {
    private Long id;
    private Integer status;
    private String datasourceId;
    private String datasourceName;
    private String datasourceDesc;
    private String datasourceType;
    private String config;
    private Integer sourceType;
    private String owner;
    private String ownerName;
    private String creator;
    private String creatorName;
    private String modifier;
    private String modifierName;
    private Date gmtCreate;
    private Date gmtModified;
}
