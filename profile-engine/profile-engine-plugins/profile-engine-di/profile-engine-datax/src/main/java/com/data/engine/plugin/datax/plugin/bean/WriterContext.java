package com.data.engine.plugin.datax.plugin.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Writer 侧任务上下文。涵盖 JDBC/HDFS/Hive 等多形态字段；具体 Builder 取需要的字段。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriterContext {

    // ========== JDBC 通用 ==========
    /** 目标表 */
    private String table;
    /** 列名列表 */
    private List<String> columns;
    /** 写入前 SQL */
    private List<String> preSql;
    /** 写入后 SQL */
    private List<String> postSql;
    /** 写入模式：insert / replace / update */
    private String writeMode;
    /** 单批写入条数 */
    private Integer batchSize;
    /** 单批写入字节数（部分插件支持） */
    private Long batchByteSize;

    // ========== HDFS / Hive 通用（占位字段，本期 Builder 未实装） ==========
    private String path;
    private String fileType;
    private String fieldDelimiter;
    private String encoding;
    private Map<String, String> hadoopConfig;

    /** 兜底扩展参数，由 Builder 选择性消费 */
    private Map<String, Object> extraParams;
}
