package com.data.profile.web.dto;

import lombok.Data;

import java.util.Date;

/**
 * 实体传输对象
 */
@Data
public class EntityDTO {
    private Long id;
    private Integer status;
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
