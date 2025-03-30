package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 功能：指标
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:32
 */
@Data
public class Measure {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    // 指标计算方式 DISTINCT/COUNT
    @SerializedName("type")
    private String type;
    // 指标运算符 > < =等
    @SerializedName("op")
    private String op;
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
