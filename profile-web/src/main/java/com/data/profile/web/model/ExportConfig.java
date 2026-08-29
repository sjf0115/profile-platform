package com.data.profile.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 功能：投递配置
 * <p>精简后仅保留投递特定参数，database/bucket/topic 等连接信息已在数据源 config 中。</p>
 * <p>使用 @JsonProperty 映射前端 snake_case 字段名到后端 camelCase。</p>
 *
 * 作者：@Smartsi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 12:57
 */
@Data
public class ExportConfig {

    // 1. 通用配置
    // 关联群组ID
    @JsonProperty("group_id")
    private String groupId;

    // 2. 数据源投递配置
    // 数据源ID
    @JsonProperty("datasource_id")
    private String datasourceId;
    // 目标表名（JDBC 投递）
    @JsonProperty("table_name")
    private String tableName;
    // 写入模式：append-追加, upsert-覆盖（JDBC 投递）
    @JsonProperty("write_mode")
    private String writeMode;
    // upsert key 列名（JDBC 投递，覆盖模式下使用）
    @JsonProperty("target_column")
    private String targetColumn;
    // 对象路径模板（MinIO 投递），执行时替换 {groupId}/{timestamp} 等变量
    @JsonProperty("object_path")
    private String objectPath;

    // 3. 应用投递配置
    // 应用ID
    @JsonProperty("application_id")
    private String applicationId;
}
