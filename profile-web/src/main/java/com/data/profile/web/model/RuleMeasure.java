package com.data.profile.web.model;

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
public class RuleMeasure {
    // 指标ID
    private String id;
    // 指标名称
    private String name;
    // 指标运算符 count
    private String type;
    // 指标运算符 > < =等
    private String op;
    // 指标值
    private List<String> values;
}
