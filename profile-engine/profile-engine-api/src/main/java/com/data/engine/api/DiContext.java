package com.data.engine.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 同步任务上下文
 *
 * <p>作为业务层（profile-web）与同步引擎插件（DataX/SeaTunnel/...）之间的契约，
 * 不包含任何引擎私有概念，引擎插件通过 {@link DiRequestBuilder} 自行解析转换。</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiContext {

    /** 业务任务 ID（用于日志、追踪） */
    private String jobId;

    /** 源端 */
    private Endpoint source;

    /** 目标端 */
    private Endpoint target;

    /** 并发通道数（DataX speed.channel 等同概念） */
    private int channel;

    /** 允许失败记录数 */
    private int errorRecord;

    /**
     * 端点描述（源/目标通用）。
     *
     * <p>使用统一字段表达 JDBC/Hive/Hdfs 等数据源最常见的连接信息；
     * 引擎插件按需消费。</p>
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Endpoint {

        /** 数据源类别：mysql / postgresql / clickhouse / oracle / hive / hdfs / ... */
        private String category;

        /** 主机 */
        private String host;
        /** 端口（字符串以容纳 "host:port" 等扩展） */
        private String port;
        /** 数据库 / schema */
        private String database;
        /** 用户名 */
        private String username;
        /** 密码 */
        private String password;
        /** JDBC 扩展属性，形如 "useSSL=false&amp;serverTimezone=UTC" */
        private String properties;

        /** 表名 */
        private String tableName;
        /** 列名列表 */
        private List<String> columns;

        /** 写入模式（仅 target 使用）：insert / replace / update */
        private String writeMode;
        /** 单批写入条数（仅 target 使用） */
        private Integer batchSize;
    }
}
