package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 权限申请单
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthApply {
    private Long id;
    private String applyId;
    private String applicant;
    private String applyReason;
    private Integer status;
    private String approver;
    private Date approveTime;
    private String approveRemark;
    private String creator;
    private Date gmtCreate;
    private Date gmtModified;
}
