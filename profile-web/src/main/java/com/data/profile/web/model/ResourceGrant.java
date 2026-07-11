package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 资源授权
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceGrant {
    private Long id;
    private String grantId;
    private String resourceType;
    private String resourceId;
    private Integer granteeType;
    private String granteeId;
    private Integer action;
    private Date expireTime;
    private String creator;
    private Date gmtCreate;
    private Date gmtModified;
}
