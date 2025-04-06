package com.data.profile.model;

import lombok.Data;

/**
 * 功能：数据集字段
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/5 22:50
 */
@Data
public class DatasetField {
    private int status;
    private String name;
    private String alias;
    // 1-实体ID、2-标签、3-行为类型、4-行为时间、5-行为属性、6-行为指标、7-自定义
    private String category;
    // 1-单值,2-多值,3-KV,4-KKV,5-JSON,6-MAP
    private int organizeType;
    // 1-文本型、2-数值型、3-时间型
    private int dataType;
    // 1-枚举,2-非枚举
    private int distType;
    // 绑定的对象ID
    private String relationId;
}
