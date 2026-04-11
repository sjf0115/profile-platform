package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户概览统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOverview {
    private int totalCount;
    private int adminCount;
    private int memberCount;
    private int noPermissionCount;
}
