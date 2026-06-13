package com.data.engine.plugin.clickhouse.executor;

import com.data.engine.api.AnalysisEngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse 分析引擎执行器
 * 负责在 ClickHouse 中创建表、修改表、同步数据、圈选群组等
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/28 13:46
 */
@Slf4j
public class ClickHouseAnalysisEngineExecutor implements AnalysisEngineExecutor {

    private ExecutorRequest request;
    private Logger logger;
    private Configurations configurations;
    private ProcessResult processResult;
    private Map<String, Object> engineConfig;

    @Override
    public void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {
        this.request = jobExecutionRequest;
        this.logger = logger;
        this.configurations = configurations;

        // 解析引擎配置
        if (jobExecutionRequest.getConfig() != null) {
            this.engineConfig = (Map<String, Object>) jobExecutionRequest.getConfig().get("engineConfig");
        }
    }

    @Override
    public void createTable(String tableName, List<Map<String, Object>> fields, String entityField, String partitionField) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // 构建 DDL
            String ddl = buildCreateTableDDL(tableName, fields, entityField, partitionField);

            logger.info("创建 ClickHouse 表: {}", ddl);
            stmt.execute(ddl);

            processResult = new ProcessResult();
            processResult.setSuccess(true);
            processResult.setErrorMsg("表创建成功: " + tableName);
            logger.info("ClickHouse 表创建成功: {}", tableName);

        } catch (Exception e) {
            logger.error("创建 ClickHouse 表失败: {}", tableName, e);
            processResult = new ProcessResult();
            processResult.setSuccess(false);
            processResult.setErrorMsg("表创建失败: " + e.getMessage());
            throw new RuntimeException("创建 ClickHouse 表失败: " + e.getMessage(), e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    @Override
    public void alterTable(String tableName, List<Map<String, Object>> addFields, List<String> dropFields, List<Map<String, Object>> modifyFields) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // 添加字段
            if (addFields != null && !addFields.isEmpty()) {
                for (Map<String, Object> field : addFields) {
                    String addSql = String.format("ALTER TABLE %s ADD COLUMN `%s` %s",
                            tableName,
                            field.get("name"),
                            field.get("type"));

                    if (field.containsKey("comment") && field.get("comment") != null) {
                        addSql += String.format(" COMMENT '%s'", field.get("comment"));
                    }

                    logger.info("添加字段: {}", addSql);
                    stmt.execute(addSql);
                }
            }

            // 删除字段
            if (dropFields != null && !dropFields.isEmpty()) {
                for (String field : dropFields) {
                    String dropSql = String.format("ALTER TABLE %s DROP COLUMN `%s`", tableName, field);
                    logger.info("删除字段: {}", dropSql);
                    stmt.execute(dropSql);
                }
            }

            // 修改字段
            if (modifyFields != null && !modifyFields.isEmpty()) {
                for (Map<String, Object> field : modifyFields) {
                    String modifySql = String.format("ALTER TABLE %s MODIFY COLUMN `%s` %s",
                            tableName,
                            field.get("name"),
                            field.get("type"));

                    if (field.containsKey("comment") && field.get("comment") != null) {
                        modifySql += String.format(" COMMENT '%s'", field.get("comment"));
                    }

                    logger.info("修改字段: {}", modifySql);
                    stmt.execute(modifySql);
                }
            }

            processResult = new ProcessResult();
            processResult.setSuccess(true);
            processResult.setErrorMsg("表结构修改成功: " + tableName);
            logger.info("ClickHouse 表结构修改成功: {}", tableName);

        } catch (Exception e) {
            logger.error("修改 ClickHouse 表失败: {}", tableName, e);
            processResult = new ProcessResult();
            processResult.setSuccess(false);
            processResult.setErrorMsg("表结构修改失败: " + e.getMessage());
            throw new RuntimeException("修改 ClickHouse 表失败: " + e.getMessage(), e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    @Override
    public void dropTable(String tableName) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            String dropSql = String.format("DROP TABLE IF EXISTS %s", tableName);
            logger.info("删除 ClickHouse 表: {}", dropSql);
            stmt.execute(dropSql);

            processResult = new ProcessResult();
            processResult.setSuccess(true);
            processResult.setErrorMsg("表删除成功: " + tableName);
            logger.info("ClickHouse 表删除成功: {}", tableName);

        } catch (Exception e) {
            logger.error("删除 ClickHouse 表失败: {}", tableName, e);
            processResult = new ProcessResult();
            processResult.setSuccess(false);
            processResult.setErrorMsg("表删除失败: " + e.getMessage());
            throw new RuntimeException("删除 ClickHouse 表失败: " + e.getMessage(), e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    /**
     * @deprecated 数据同步已由 DI 引擎（DataX）承担。请通过 {@code DatasetSyncService.submitSyncTask}
     * 路径调用 DataX DI 引擎执行同步，分析引擎仅负责 DDL/查询职责。
     */
    @Deprecated
    @Override
    public void syncData(ExecutorRequest syncRequest) throws Exception {
        throw new UnsupportedOperationException(
                "数据同步已由 DI 引擎（DataX）承担，请通过 DatasetSyncService 调用 DataX DI 引擎。"
                        + "分析引擎仅负责 DDL/查询职责。");
    }

    @Override
    public void selectedGroup() throws Exception {
        // 圈选群组功能
        logger.info("执行圈选群组操作");
        processResult = new ProcessResult();
        processResult.setSuccess(true);
        processResult.setErrorMsg("圈选群组执行成功");
    }

    @Override
    public ProcessResult getProcessResult() {
        return processResult;
    }

    @Override
    public ExecutorRequest getTaskRequest() {
        return request;
    }

    // -----------------------------------------------------------------------------------------------------------------
    /**
     * 构建创建表 DDL
     */
    private String buildCreateTableDDL(String tableName, List<Map<String, Object>> fields, String entityField, String partitionField) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

        // 业务字段
        for (int i = 0; i < fields.size(); i++) {
            Map<String, Object> field = fields.get(i);
            ddl.append("  `").append(field.get("name")).append("` ")
                    .append(field.get("type"));

            if (field.containsKey("comment") && field.get("comment") != null) {
                ddl.append(" COMMENT '").append(field.get("comment")).append("'");
            }

            if (i < fields.size() - 1) {
                ddl.append(",\n");
            }
        }
        // 系统字段
        ddl.append(",\n  `_sync_time` DateTime DEFAULT now() COMMENT '同步时间'");
        ddl.append(",\n  `_version` UInt64 DEFAULT 1 COMMENT '版本号'");
        ddl.append("\n) ENGINE = MergeTree() ");

        // ORDER BY
        if (entityField != null && !entityField.isEmpty()) {
            ddl.append("ORDER BY (").append(entityField).append(") ");
        } else {
            ddl.append("ORDER BY tuple() ");
        }

        // PARTITION BY (可选)
        if (partitionField != null && !partitionField.isEmpty()) {
            ddl.append("PARTITION BY ").append(partitionField).append(" ");
        }

        ddl.append("SETTINGS index_granularity = 8192");

        return ddl.toString();
    }

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        if (engineConfig == null) {
            throw new SQLException("引擎配置未初始化");
        }

        String host = (String) engineConfig.get("host");
        int port = ((Number) engineConfig.get("port")).intValue();
        String database = (String) engineConfig.get("database");
        String username = (String) engineConfig.get("username");
        String password = (String) engineConfig.get("password");

        String url = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);

        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭资源
     */
    private void closeResources(Statement stmt, Connection conn) {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            logger.error("关闭资源失败", e);
        }
    }
}