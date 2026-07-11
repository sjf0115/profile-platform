package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 权限申请明细
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthApplyItem {
    private Long id;
    private String itemId;
    private String applyId;
    private String resourceType;
    private String resourceId;
    private Integer action;
    private Date expireTime;
    private String creator;
    private Date gmtCreate;
}
