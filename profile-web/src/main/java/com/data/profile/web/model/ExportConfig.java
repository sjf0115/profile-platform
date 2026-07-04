package com.data.profile.web.model;

import lombok.Data;

import java.util.List;

/**
 * 功能：投递配置
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 12:57
 */
@Data
public class ExportConfig {
    // ========== 数据源投递配置 ==========

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 写入模式：append-追加, upsert-覆盖
     */
    private String writeMode;

    /**
     * 字段映射配置
     */
    private List<FieldMapping> fieldMapping;

    // ========== 应用投递配置 ==========

    /**
     * 应用ID（应用投递时使用，export_mode=2 时有效）
     */
    private String applicationId;

    // ========== 文件存储配置（targetType=file）==========

    /**
     * 存储桶名称
     */
    private String bucket;

    /**
     * 对象路径
     */
    private String objectPath;

    /**
     * 文件格式：csv, parquet, json
     */
    private String fileFormat;

    // ========== 消息队列配置（targetType=topic）==========

    /**
     * 主题名称
     */
    private String topic;

    /**
     * 消息格式：json, avro
     */
    private String messageFormat;

    // ========== ES 索引配置（targetType=index）==========

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 字段映射配置
     */
    @Data
    public static class FieldMapping {
        /**
         * 源字段名
         */
        private String sourceField;

        /**
         * 目标字段名
         */
        private String targetField;
    }
}
