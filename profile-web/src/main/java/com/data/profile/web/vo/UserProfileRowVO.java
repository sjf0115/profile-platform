package com.data.profile.web.vo;

import lombok.Data;

/**
 * 功能：用户画像表格行 VO
 * 用于用户画像主页随机用户列表展示
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserProfileRowVO {
    /** 用户ID */
    private String userId;
    /** 用户名称 */
    private String userName;
    /** 应用 */
    private String app;
    /** 最近访问时间 */
    private String lastAccessTime;
    /** 设备型号 */
    private String deviceModel;
    /** 操作系统 */
    private String os;
    /** 软件版本 */
    private String softwareVersion;
    /** 渠道 */
    private String channel;
    /** 设备品牌 */
    private String deviceBrand;
    /** 地域 */
    private String region;
}
