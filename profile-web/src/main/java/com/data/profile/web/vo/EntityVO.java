package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 实体视图对象
 */
@Data
public class EntityVO {
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
