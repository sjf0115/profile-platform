package com.data.profile.web.dto;

import com.data.profile.web.model.LabelConfig;
import lombok.Data;

import java.util.Date;

/**
 * 标签数据传输对象
 */
@Data
public class LabelDTO {
    private Long id;
    private Integer isValid;
    private String labelId;
    private String labelName;
    private Integer labelStatus;
    private String labelType;
    private String labelDesc;
    private String labelCategoryId;
    private Integer labelDataType;
    private Integer labelDistType;
    private Integer labelOrganizeType;
    private Integer labelProduceType;
    private Integer labelTimeType;
    private Integer sourceType;
    private LabelConfig config;
    private Integer isOffice;
    private String owner;
    private String ownerName;
    private String creator;
    private String creatorName;
    private String modifier;
    private String modifierName;
    private Date gmtCreate;
    private Date gmtModified;
    private String entityIdentifierId;
    // 关联查询字段
    private String entityIdentifierName;
    private String entityId;
    private String entityName;
    private String datasetId;
    private String datasetName;
    private String datasetFieldName;
}
