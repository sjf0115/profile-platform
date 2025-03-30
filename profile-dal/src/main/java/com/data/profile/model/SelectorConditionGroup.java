package com.data.profile.model;

import lombok.Data;

import java.util.List;

/**
 * 功能：筛选器条件组
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:30
 */
@Data
public class SelectorConditionGroup {
    private String logic;
    private List<SelectorCondition> conditions;
}
