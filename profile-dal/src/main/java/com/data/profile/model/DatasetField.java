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
    // 1-新增字段:数据集字段没有但原始表列有(标记新增的标识)、2-修改字段:数据集字段和原始表列均有、3-删除字段:数据集字段有但原始表列已经删除(标记删除标识)
    private int status = 1;
    // 导入状态: 1-导入,2-不导入
    private int importStatus = 1;
    private String name;
    private String alias;
    // 1-实体ID、2-标签、3-行为类型、4-行为时间、5-行为属性、6-行为指标、7-自定义
    private int category;
    // 1-单值,2-多值,3-KV,4-KKV,5-JSON,6-MAP
    private int organizeType = 1;
    // 1-文本型、2-数值型、3-时间型
    private int dataType = 1;
    // 1-枚举,2-非枚举
    private int distType = 1;
    // 绑定的对象ID
    private String relationId;
}
