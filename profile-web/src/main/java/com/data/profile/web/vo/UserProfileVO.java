package com.data.profile.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 功能：用户画像VO - Controller返回
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserProfileVO {
    // 用户基础信息
    private UserVO user;
    // 标签按类目分组
    private List<LabelCategoryVO> labelCategories;
    // 所属人群
    private List<GroupVO> groups;
}
