package com.data.profile.web.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

// 数据集字段
@Data
public class DatasetField {
    private Long id;
    // 数据集ID
    @SerializedName("dataset_id")
    private String datasetId;
    // 废弃：字段状态:1-新增字段,2-修改字段,3-删除字段
    @Deprecated
    @SerializedName("field_status")
    private Integer fieldStatus;
    // 字段名称
    @SerializedName("field_name")
    private String fieldName;
    // 字段描述
    @SerializedName("field_desc")
    private String fieldDesc;
    // 字段类型
    @SerializedName("field_type")
    private String fieldType;
    // 导入状态:1-导入,2-不导入
    @SerializedName("import_status")
    private Integer importStatus;
    // 数据集关联对象ID
    @SerializedName("related_id")
    private String relatedId;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    @SerializedName("gmt_create")
    private Date gmtCreate;
    // 修改时间
    @SerializedName("gmt_modified")
    private Date gmtModified;
}
