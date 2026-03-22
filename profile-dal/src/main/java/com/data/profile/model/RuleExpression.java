package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：规则表达式
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleExpression {
    private String logic;
    @SerializedName("rule_groups")
    private List<RuleGroup> ruleGroups;
}
