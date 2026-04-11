package com.data.profile.web.model;

import lombok.Data;

/**
 * 功能：文件导入标签配置
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/19 22:21
 */
@Data
public class FileImportLabelConfig {
    // 上传文件路径
    private String filePath;
    // 上传文件名称
    private String fileName;
}
