package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：过滤器条件组
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterConditionGroup {
    private String logic;
    private List<FilterCondition> conditions;
}
