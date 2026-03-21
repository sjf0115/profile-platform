package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能：筛选器条件
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SelectorCondition {
    // 标签规则：是(label)/不是(not_label); 群组规则: 包含(group)/不包含(not_group)；事件规则：做过(event)/没有做过(not_event)；行为序列规则：依次做过(event_sequence)/没有依次做过(not_event_sequence)
    private String type;
    // 事件：事件规则时使用
    private Event event;
    // 指标：事件规则时使用
    private Measure measure;
    // 时间周期范围
    private TimePeriod period;
    // 筛选器表达式：群组/标签规则时使用
    private FilterExpression filters;
}
