package com.data.profile.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：规则
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rule {
    // 规则类型：1-标签规则,2-群组规则,3-事件规则,4-行为序列规则
    private String type;

    // 标签规则/群组规则/事件规则
    @SerializedName("filter_expression")
    private RuleFilterExpression filterExpression;

    // 事件规则使用
    @SerializedName("event")
    private RuleEvent event;

    // 行为序列规则使用
    @JsonProperty("events")
    private List<RuleSequence> events;
}
