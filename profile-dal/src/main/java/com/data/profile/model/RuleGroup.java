package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：规则组
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/30 16:30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleGroup {
    // AND OR
    private String logic;
    // 规则
    private List<Rule> rules;
}
