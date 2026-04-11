package com.data.profile.web.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：过滤条件 支持标签和群组筛选
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:35
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleFilter {
    // 1-标签,2-群组,3-事件属性
    private int type;
    // 标签/群组ID
    private String id;
    // 标签/群组名称
    private String name;
    // 标签/群组操作符
    private String op;
    // 标签/群组支持的操作符
    @SerializedName("support_ops")
    private List<String> supportOps;
    // 标签/群组操作值
    private List<String> values;
}