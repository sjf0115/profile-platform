package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能：标签操作符
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/21 13:40
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelOperator {
    // 展示名称
    private String name;
    // 操作码
    private String code;
    // 支持的标签数据类型 label_data_type
    private List<Integer> types;
}
