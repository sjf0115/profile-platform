package com.data.profile.web.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

// 数据集字段
@Data
public class DatasetFieldVO {
    private Long id;
    // 数据集ID
    private String datasetId;
    // 废弃：字段状态:1-新增字段,2-修改字段,3-删除字段
    @Deprecated
    private Integer fieldStatus;
    // 字段名称
    private String fieldName;
    // 字段描述
    private String fieldDesc;
    // 字段类型
    private String fieldType;
    // 导入状态:1-导入,2-不导入
    private Integer importStatus;
    // 数据集关联对象ID
    private String relatedId;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
    // 是否是实体ID字段
    private Boolean entityField;
}
