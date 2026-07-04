package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 应用管理实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    private Long id;
    // 状态: 1-启用, 2-停用
    private Integer status;
    // 应用名称
    private String appName;
    // 应用描述
    private String appDesc;
    // 应用Key（唯一标识 + API凭证）
    private String appKey;
    // 应用Secret（API密钥）
    private String appSecret;
    // 投递目标配置(JSON)
    private String targetConfig;
    // Webhook地址
    private String webhookUrl;
    // API调用频率限制（次/分钟）
    private Integer rateLimit;
    // IP白名单
    private String ipWhitelist;
    // 创建方式: 1-系统内置, 2-自定义
    private Integer sourceType;
    // 负责人
    private String owner;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
