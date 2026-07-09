package com.data.profile.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户概览统计（Service 层出参）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOverviewDTO {
    // 总用户数
    private int totalCount;
    // 管理员用户数
    private int adminCount;
    // 普通用户数
    private int memberCount;
    // 无权限用户数
    private int noPermissionCount;
}
