package com.data.engine.plugin.executor;

import com.data.engine.api.EngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：ClickHouseEngineExecutor
 * 描述：直连 ClickHouse 执行 SQL 语句（圈选人群、数据查询等）
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/28 13:46
 */
@Slf4j
public class ClickHouseEngineExecutor implements EngineExecutor {
    
    private String jobId;
    private Logger logger;
    private ExecutorRequest request;
    private volatile boolean isCancelled = false;
    private ProcessResult processResult;
    
    // ClickHouse 连接配置
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    
    // SQL 语句
    private String sql;
    
    // 查询结果
    private List<Map<String, Object>> queryResult;

    @Override
    public void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {
        this.request = jobExecutionRequest;
        this.logger = logger;
        this.jobId = jobExecutionRequest.getJobId();
        this.processResult = new ProcessResult();
        this.queryResult = new ArrayList<>();
        
        // 解析配置
        parseConfig(jobExecutionRequest);
        
        logger.info("ClickHouseEngineExecutor initialized, jobId: {}", jobId);
    }
    
    /**
     * 解析执行请求配置
     */
    private void parseConfig(ExecutorRequest request) {
        Map<String, Object> config = request.getConfig();
        
        // ClickHouse 连接信息
        this.host = getString(config, "host", "localhost");
        this.port = getInt(config, "port", 8123);
        this.database = getString(config, "database", "default");
        this.username = getString(config, "username", "default");
        this.password = getString(config, "password", "");
        
        // SQL 语句
        this.sql = getString(config, "sql", "");
    }
    
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value != null ? Integer.parseInt(value.toString()) : defaultValue;
    }

    @Override
    public void execute() throws Exception {
        logger.info("Starting ClickHouse SQL execution, jobId: {}, sql: {}", jobId, sql);
        
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL statement cannot be empty");
        }
        
        String jdbcUrl = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            long startTime = System.currentTimeMillis();
            boolean hasResultSet = stmt.execute(sql);
            long duration = System.currentTimeMillis() - startTime;
            
            if (hasResultSet) {
                // SELECT 查询，获取结果集
                try (ResultSet rs = stmt.getResultSet()) {
                    processResultSet(rs);
                }
                processResult.setSuccess(true);
                processResult.setRecordCount(queryResult.size());
                logger.info("SQL executed successfully, jobId: {}, rows: {}, duration: {}ms", jobId, queryResult.size(), duration);
            } else {
                // INSERT/UPDATE/DELETE 等，获取影响行数
                int affectedRows = stmt.getUpdateCount();
                processResult.setSuccess(true);
                processResult.setRecordCount(affectedRows);
                logger.info("SQL executed successfully, jobId: {}, affected rows: {}, duration: {}ms", jobId, affectedRows, duration);
            }
            
            processResult.setDuration(duration);
            
        } catch (Exception e) {
            processResult.setSuccess(false);
            processResult.setErrorMsg(e.getMessage());
            logger.error("SQL execution failed, jobId: {}, sql: {}", jobId, sql, e);
            throw e;
        }
    }
    
    /**
     * 处理查询结果集
     */
    private void processResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while (rs.next()) {
            if (isCancelled) {
                logger.warn("Job cancelled: {}", jobId);
                throw new SQLException("Job cancelled");
            }
            
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            queryResult.add(row);
        }
    }

    @Override
    public void pause() throws Exception {
        logger.info("Pause not supported in ClickHouseEngineExecutor");
    }

    @Override
    public void restore() throws Exception {
        logger.info("Restore not supported in ClickHouseEngineExecutor");
    }

    @Override
    public void after() throws Exception {
        // 清理资源
        logger.info("ClickHouseEngineExecutor after hook, jobId: {}", jobId);
    }

    @Override
    public void cancel() throws Exception {
        this.isCancelled = true;
        logger.info("Job cancelled: {}", jobId);
    }

    @Override
    public boolean isCancel() throws Exception {
        return isCancelled;
    }

    @Override
    public ProcessResult getProcessResult() {
        return processResult;
    }

    @Override
    public ExecutorRequest getTaskRequest() {
        return request;
    }
}
