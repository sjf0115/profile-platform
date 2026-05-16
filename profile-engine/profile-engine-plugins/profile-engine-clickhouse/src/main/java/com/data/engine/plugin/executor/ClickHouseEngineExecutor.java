package com.data.engine.plugin.executor;

import com.data.engine.api.EngineExecutor;
import com.data.engine.common.ExecutorRequest;
import com.data.profile.common.config.Configurations;
import com.data.profile.common.domain.engine.ProcessResult;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能：ClickHouseEngineExecutor
 * 描述：基于 JDBC 直连 ClickHouse 执行数据同步
 * 适用于小批量数据同步，大批量请使用 SeaTunnel
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
    
    // 同步配置
    private String sourceTable;
    private String targetTable;
    private List<String> columns;
    private int batchSize = 1000;

    @Override
    public void init(ExecutorRequest jobExecutionRequest, Logger logger, Configurations configurations) throws Exception {
        this.request = jobExecutionRequest;
        this.logger = logger;
        this.jobId = jobExecutionRequest.getJobId();
        this.processResult = new ProcessResult();
        
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
        
        // 同步配置
        this.sourceTable = getString(config, "sourceTable", "");
        this.targetTable = getString(config, "targetTable", "");
        this.columns = (List<String>) config.getOrDefault("columns", new ArrayList<>());
        this.batchSize = getInt(config, "batchSize", 1000);
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
        logger.info("Starting ClickHouse sync job: {}", jobId);
        
        String jdbcUrl = String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            // 1. 检查源表是否存在
            if (!checkTableExists(conn, sourceTable)) {
                throw new RuntimeException("Source table not found: " + sourceTable);
            }
            
            // 2. 创建目标表（如果不存在）
            createTargetTable(conn);
            
            // 3. 执行数据同步
            syncData(conn);
            
            processResult.setSuccess(true);
            logger.info("ClickHouse sync job completed: {}", jobId);
            
        } catch (Exception e) {
            processResult.setSuccess(false);
            processResult.setErrorMsg(e.getMessage());
            logger.error("ClickHouse sync job failed: {}", jobId, e);
            throw e;
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean checkTableExists(Connection conn, String tableName) throws Exception {
        String sql = String.format("SHOW TABLES LIKE '%s'", tableName);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }
    
    /**
     * 创建目标表
     */
    private void createTargetTable(Connection conn) throws Exception {
        // 获取源表结构
        String showCreateSql = String.format("SHOW CREATE TABLE %s", sourceTable);
        String createTableSql = null;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(showCreateSql)) {
            if (rs.next()) {
                createTableSql = rs.getString(1);
            }
        }
        
        if (createTableSql == null) {
            throw new RuntimeException("Cannot get create table SQL for: " + sourceTable);
        }
        
        // 替换表名
        createTableSql = createTableSql.replaceFirst(sourceTable, targetTable);
        
        // 创建目标表（如果不存在）
        createTableSql = createTableSql.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            logger.info("Target table created: {}", targetTable);
        }
    }
    
    /**
     * 同步数据
     */
    private void syncData(Connection conn) throws Exception {
        String selectColumns = columns.isEmpty() ? "*" : String.join(", ", columns);
        String selectSql = String.format("SELECT %s FROM %s", selectColumns, sourceTable);
        String insertSql = buildInsertSql();
        
        long totalCount = 0;
        long startTime = System.currentTimeMillis();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, Object>> batch = new ArrayList<>();
            
            while (rs.next()) {
                if (isCancelled) {
                    logger.warn("Job cancelled: {}", jobId);
                    throw new InterruptedException("Job cancelled");
                }
                
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                batch.add(row);
                
                if (batch.size() >= batchSize) {
                    executeBatchInsert(pstmt, batch, metaData);
                    totalCount += batch.size();
                    batch.clear();
                    logger.info("Synced {} records", totalCount);
                }
            }
            
            // 处理剩余数据
            if (!batch.isEmpty()) {
                executeBatchInsert(pstmt, batch, metaData);
                totalCount += batch.size();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        processResult.setRecordCount(totalCount);
        processResult.setDuration(duration);
        
        logger.info("Data sync completed. Total: {} records, Duration: {}ms", totalCount, duration);
    }
    
    /**
     * 构建 INSERT SQL
     */
    private String buildInsertSql() {
        String cols = columns.isEmpty() ? "" : "(" + String.join(", ", columns) + ")";
        String placeholders = columns.isEmpty() ? "" : 
            String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));
        return String.format("INSERT INTO %s %s VALUES (%s)", targetTable, cols, placeholders);
    }
    
    /**
     * 执行批量插入
     */
    private void executeBatchInsert(PreparedStatement pstmt, List<Map<String, Object>> batch, 
                                    ResultSetMetaData metaData) throws Exception {
        for (Map<String, Object> row : batch) {
            for (int i = 0; i < columns.size(); i++) {
                pstmt.setObject(i + 1, row.get(columns.get(i)));
            }
            pstmt.addBatch();
        }
        pstmt.executeBatch();
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
