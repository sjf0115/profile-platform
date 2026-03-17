package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：标签配置
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/17 23:21
 */
@Data
public class LabelConfig {
    // 数据集导入-数据集ID
    @SerializedName("dataset_id")
    private String datasetId;
    // 数据集导入-数据集字段
    @SerializedName("dataset_field")
    private String datasetField;
    // 文件上传-原始上传路径
    @SerializedName("origin_path")
    private String originPath;
    // 文件上传-实际存储路径
    @SerializedName("physical_path")
    private String physicalPath;
}
