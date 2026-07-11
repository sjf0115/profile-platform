package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 权限点
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    private Long id;
    private Integer status;
    private String permissionId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private String parentId;
    private String menuPath;
    private String apiPattern;
    private Integer sort;
    private Integer sourceType;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
}
