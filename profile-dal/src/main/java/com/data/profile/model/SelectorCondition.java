package com.data.profile.model;

import lombok.Data;

/**
 * 功能：筛选器条件
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:31
 */
@Data
public class SelectorCondition {
    // 用户是(profile)/用户不是(not_profile)/用户做过(event)/用户没有做过(not_event)/用户依次做过(event_sequence)/用户没有依次做过(not_event_sequence)
    private String type;
    // 事件
    private Event event;
    // 时间周期范围
    private TimePeriod period;
    // 指标
    private Measure measure;
    // 筛选器表达式
    private FilterExpression filters;
}
