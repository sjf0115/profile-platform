package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 实体标识视图对象
 */
@Data
public class EntityIdentifierVO {
    private Integer id;
    private Integer status;
    private String entityIdentifierId;
    private String entityIdentifierName;
    private String entityId;
    private String entityName;
    private Integer sourceType;
    private String creator;
    private String creatorName;
    private String modifier;
    private String modifierName;
    private Date gmtCreate;
    private Date gmtModified;
}
