package com.data.profile.web.vo;

import lombok.Data;

import java.util.Date;

/**
 * 应用视图对象
 */
@Data
public class ApplicationVO {
    private Long id;
    private Integer status;
    private String appName;
    private String appDesc;
    private String appKey;
    private String appSecret;
    private String targetConfig;
    private String webhookUrl;
    private Integer rateLimit;
    private String ipWhitelist;
    private Integer sourceType;
    private String owner;
    private String ownerName;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
}
