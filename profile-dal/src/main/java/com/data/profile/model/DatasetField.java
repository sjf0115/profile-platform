package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 功能：数据集字段
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/5 22:50
 */
@Data
public class DatasetField {
    // 1-新增字段:数据集字段没有但原始表列有(标记新增的标识)、2-修改字段:数据集字段和原始表列均有、3-删除字段:数据集字段有但原始表列已经删除(标记删除标识)
    private int status = 1;
    // 字段名称
    @SerializedName("field_name")
    private String fieldName;
    // 字段描述
    @SerializedName("field_desc")
    private String fieldDesc;
    // 字段类型
    @SerializedName("field_type")
    private String fieldType;
    // 导入状态: 1-导入,2-不导入
    @SerializedName("import_status")
    private int importStatus = 1;
    // 标签数据集关联标签ID
    @SerializedName("related_id")
    private String relatedId;
    // 标签数据集关联标签名称
    @SerializedName("related_name")
    private String relatedName;
}
