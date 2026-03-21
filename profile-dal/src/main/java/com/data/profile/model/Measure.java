package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：指标
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Measure {
    // 指标ID
    @SerializedName("id")
    private String id;
    // 指标名称
    @SerializedName("name")
    private String name;
    // 指标运算符 DISTINCT/COUNT
    @SerializedName("type")
    private String type;
    // 指标运算符 > < =等
    @SerializedName("op")
    private String op;
    // 指标值
    @SerializedName("values")
    private List<String> values;
    // 参与指标计算的可以有事件属性和标签
    @SerializedName("property_type")
    private String propertyType;
    // 对应事件属性ID、标签ID
    @SerializedName("property_id")
    private String propertyId;
    // 对应事件属性名称、标签名称
    @SerializedName("property_name")
    private String propertyName;
}
