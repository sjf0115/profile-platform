package com.data.profile.web.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：过滤器表达式
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleFilterExpression {
    private String logic;
    @SerializedName("filter_groups")
    private List<RuleFilterGroup> filterGroups;
}
