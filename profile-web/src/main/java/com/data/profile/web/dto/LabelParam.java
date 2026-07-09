package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：标签查询参数
 * 日期：2026/7/8
 */
@Data
public class LabelParam {
    // 标签名称
    private String labelName;
    // 标签状态
    private Integer labelStatus;
    // 标签类型: 1-属性标签, 2-行为标签
    private String labelType;
    // 标签类目ID
    private String labelCategoryId;
    // 标签实体标识ID
    private String entityIdentifierId;
    // 是否有效
    private Integer isValid;
    // 创建方式
    private Integer sourceType;
}
