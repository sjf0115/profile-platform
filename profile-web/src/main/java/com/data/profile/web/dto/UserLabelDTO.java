package com.data.profile.web.dto;

import lombok.Data;

/**
 * 功能：用户标签DTO - Service层业务传输
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserLabelDTO {
    // 用户ID
    private String userId;
    // 标签ID
    private String labelId;
    // 标签名称（从 Label 关联获取）
    private String labelName;
    // 标签值
    private String labelValue;
    // 类目ID（从 Label.labelCategoryId 关联获取）
    private String categoryId;
    // 类目名称（从 LabelCategory 关联获取）
    private String categoryName;
    // 排序序号
    private Integer sortOrder;
}
