package com.data.profile.web.vo;

import lombok.Data;

/**
 * 功能：用户标签VO - API响应
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserLabelVO {
    // 标签ID
    private String labelId;
    // 标签名称
    private String labelName;
    // 标签值
    private String labelValue;
}
