package com.data.engine.plugin.datax.plugin.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Reader 侧任务上下文。涵盖 JDBC/HDFS/Hive 等多形态字段；具体 Builder 取需要的字段。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReaderContext {

    // ========== JDBC 通用 ==========
    /** 表名（与 querySql 二选一） */
    private String table;
    /** 列名列表（推荐显式列，少用 ["*"]） */
    private List<String> columns;
    /** where 条件，可选 */
    private String where;
    /** 切分主键，可选 */
    private String splitPk;
    /** 自定义查询 SQL（与 table 二选一） */
    private List<String> querySql;
    /** Reader 抓取批次大小（如 Oracle fetchSize） */
    private Integer fetchSize;

    // ========== HDFS / Hive 通用（占位字段，本期 Builder 未实装） ==========
    /** HDFS 文件路径（含 wildcard） */
    private String path;
    /** 文件类型：text / orc / parquet / csv */
    private String fileType;
    /** 字段分隔符 */
    private String fieldDelimiter;
    /** 编码 */
    private String encoding;
    /** Hadoop 自定义配置覆盖 */
    private Map<String, String> hadoopConfig;

    /** 兜底扩展参数，由 Builder 选择性消费 */
    private Map<String, Object> extraParams;
}
