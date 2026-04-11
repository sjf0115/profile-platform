package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

// 实体标识
@Data
public class EntityIdentifier {
    private Integer id;
    // 实体标识状态
    private Integer status;
    // 实体标识ID
    private String entityIdentifierId;
    // 实体标识名称
    private String entityIdentifierName;
    // 实体ID
    private String entityId;
    // 实体名称
    private String entityName;
    // 创建方式
    private Integer sourceType;
    // 创建者
    private String creator;
    // 修改人
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}