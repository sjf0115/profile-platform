package com.data.profile.web.dto;

import lombok.Data;

/**
 * 应用创建/编辑请求
 */
@Data
public class ApplicationRequest {
    // 应用名称
    private String appName;
    // 应用描述
    private String appDesc;
    // 负责人(userId)
    private String owner;
    // 投递目标配置(JSON)
    private String targetConfig;
    // Webhook地址
    private String webhookUrl;
    // API调用频率限制（次/分钟）
    private Integer rateLimit;
    // IP白名单
    private String ipWhitelist;
}
