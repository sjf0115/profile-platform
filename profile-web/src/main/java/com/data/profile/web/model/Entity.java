package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

// 实体
@Data
public class Entity {
    private Long id;
    // 实体状态
    private Integer status;
    // 实体ID
    private String entityId;
    // 实体名称
    private String entityName;
    // 创建方式
    private Integer sourceType;
    // 创建人
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}