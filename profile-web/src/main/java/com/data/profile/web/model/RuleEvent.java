package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：事件规则
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/22 20:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleEvent {
    // 事件
    private Event event;
    // 指标
    private RuleMeasure measure;
    // 时间周期范围
    private RuleTimePeriod period;
}
