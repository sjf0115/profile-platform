package com.data.profile.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 功能：标签类目VO - API响应
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class LabelCategoryVO {
    // 类目ID
    private String categoryId;
    // 类目名称
    private String categoryName;
    // 排序序号
    private Integer sortOrder;
    // 该类目下的标签列表
    private List<UserLabelVO> labels;
}
