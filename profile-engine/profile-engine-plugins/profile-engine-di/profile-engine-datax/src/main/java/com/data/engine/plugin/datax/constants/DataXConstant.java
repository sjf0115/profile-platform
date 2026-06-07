package com.data.engine.plugin.datax.constants;

/**
 * DataX 引擎常量。
 * <ul>
 *     <li>日志关键字：用于 {@code AnalysisStatistics} 解析任务统计</li>
 *     <li>JSON 字段名：用于构建 DataX 标准任务配置</li>
 * </ul>
 */
public final class DataXConstant {

    private DataXConstant() {
    }

    // ======== DataX 标准日志关键字 ========
    public static final String TASK_START_TIME = "任务启动时刻";
    public static final String TASK_END_TIME = "任务结束时刻";
    public static final String TASK_TOTAL_COSTS = "任务总计耗时";
    public static final String TASK_BYTE_SPEED = "任务平均流量";
    public static final String TASK_RECORD_SPEED = "记录写入速度";
    public static final String TASK_READ_RECORDS = "读出记录总数";
    public static final String TASK_FAIL_RECORDS = "读写失败总数";

    // ======== DataX JSON 字段名 ========
    public static final String JOB = "job";
    public static final String SETTING = "setting";
    public static final String CONTENT = "content";

    public static final String SPEED = "speed";
    public static final String CHANNEL = "channel";
    public static final String BYTE = "byte";
    public static final String RECORD = "record";
    public static final String ERROR_LIMIT = "errorLimit";
    public static final String PERCENTAGE = "percentage";

    public static final String READER = "reader";
    public static final String WRITER = "writer";
    public static final String NAME = "name";
    public static final String PARAMETER = "parameter";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CONNECTION = "connection";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String TABLE = "table";
    public static final String COLUMN = "column";
    public static final String WHERE = "where";
    public static final String SPLIT_PK = "splitPk";
    public static final String QUERY_SQL = "querySql";
    public static final String FETCH_SIZE = "fetchSize";

    public static final String PRE_SQL = "preSql";
    public static final String POST_SQL = "postSql";
    public static final String WRITE_MODE = "writeMode";
    public static final String BATCH_SIZE = "batchSize";
    public static final String BATCH_BYTE_SIZE = "batchByteSize";

    // ======== HDFS / Hive 字段（占位，给将来 Builder 复用） ========
    public static final String PATH = "path";
    public static final String DEFAULT_FS = "defaultFS";
    public static final String FILE_TYPE = "fileType";
    public static final String FIELD_DELIMITER = "fieldDelimiter";
    public static final String HADOOP_CONFIG = "hadoopConfig";
    public static final String ENCODING = "encoding";

    // ======== Engine.entry 命令行（已废弃：进程模式不再使用） ========
    // 已移除 ENGINE_ARGS_TEMPLATE

    // ======== 日志器名（已废弃：CapturingLogAppender 已删除） ========
    // 已移除 DATAX_LOGGER_NAME
}
