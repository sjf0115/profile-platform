package com.data.profile.model;

import lombok.Data;

/**
 * 功能：群组规则
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/18 00:08
 */
@Data
public class GroupRule {
    // 标签筛选
    private RuleExpression expression;
}
