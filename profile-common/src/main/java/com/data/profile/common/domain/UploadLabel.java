package com.data.profile.common.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 功能：上传标签
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/19 12:26
 */
@Data
public class UploadLabel {
    // 实体ID
    @SerializedName("entity_id")
    private String entityId;
    // 标签值
    private String value;
}
