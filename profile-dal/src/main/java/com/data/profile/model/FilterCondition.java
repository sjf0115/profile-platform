package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：过滤器条件
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:35
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterCondition {
    // 1-标签,2-群组,3-事件
    private int type;
    private String id;
    private String name;
    private String op;
    private List<String> values;
}