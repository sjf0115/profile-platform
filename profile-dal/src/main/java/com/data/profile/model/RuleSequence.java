package com.data.profile.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：规则-行为序列
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/22 22:54
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleSequence {
    @SerializedName("event")
    private RuleEvent ruleEvent;
    @SerializedName("filter_expression")
    private RuleFilterExpression ruleFilterExpression;
}
