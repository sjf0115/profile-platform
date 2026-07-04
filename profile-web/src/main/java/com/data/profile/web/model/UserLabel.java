package com.data.profile.web.model;

import lombok.Data;

import java.util.Date;

/**
 * 功能：用户标签关联DO - 对应 profile_user_label 表
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserLabel {
    private Long id;
    // 用户ID
    private String userId;
    // 标签ID（关联 profile_label.label_id）
    private String labelId;
    // 标签值
    private String labelValue;
    // 类目排序序号
    private Integer sortOrder;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
