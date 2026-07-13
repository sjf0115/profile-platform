package com.data.profile.web.dto;

import lombok.Data;

import java.util.Date;

/**
 * 实体标识传输对象
 */
@Data
public class EntityIdentifierDTO {
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
