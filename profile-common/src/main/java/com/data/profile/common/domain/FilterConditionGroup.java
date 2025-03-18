package com.data.profile.common.domain;

import lombok.Data;

import java.util.List;

/**
 * 功能：圈选条件组
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/18 22:24
 */
@Data
public class FilterConditionGroup {
    private String logic;
    private List<FilterCondition> conditions;
}
