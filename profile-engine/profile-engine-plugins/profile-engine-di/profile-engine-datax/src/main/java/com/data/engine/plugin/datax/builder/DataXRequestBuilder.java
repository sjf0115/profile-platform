package com.data.engine.plugin.datax.builder;

import com.data.engine.api.DiRequestBuilder;
import com.data.engine.api.context.DiContext;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.datax.datasource.JdbcDataXDataSource;
import com.data.engine.plugin.datax.helper.DataXJobBuildRequest;
import com.data.engine.plugin.datax.helper.DataXJsonHelper;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DataX 引擎的 DiRequestBuilder 实现。
 *
 * <p>把中性 {@link DiContext} 转为 DataX 私有的 {@link DataXJobBuildRequest}，
 * 再序列化为 {@link ExecutorRequest#getConfig()} 供 {@code DataXDiEngineExecutor} 消费。</p>
 *
 * <p>引擎模块对 web 层模型（DataSource/Dataset/Engine）零依赖。</p>
 */
public class DataXRequestBuilder implements DiRequestBuilder {

    @Override
    public ExecutorRequest buildRequest(DiContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("SyncContext is null");
        }
        DiContext.Endpoint source = ctx.getSource();
        DiContext.Endpoint target = ctx.getTarget();
        if (source == null) {
            throw new IllegalArgumentException("SyncContext.source is null");
        }
        if (target == null) {
            throw new IllegalArgumentException("SyncContext.target is null");
        }

        // 1. Reader 侧（表名从中性参数集 config 消费）
        Map<String, Object> sourceCfg = safeConfig(source);
        JdbcDataXDataSource readerSource = buildJdbcSource(source);
        ReaderContext readerContext = ReaderContext.builder()
                .table(getString(sourceCfg, "tableName"))
                .columns(source.getColumns())
                .build();

        // 2. Writer 侧：写入参数（导入平台策略 / 导出投递参数）已并入中性参数集 config，统一消费；
        //    writeMode/targetColumn 等语义 → DataX 方言由本插件转换（如 append→insert、upsert 生成 preSql 先删后写）
        Map<String, Object> targetCfg = safeConfig(target);
        String table = getString(targetCfg, "tableName");
        String writeMode = StringUtils.defaultIfBlank(getString(targetCfg, "writeMode"), "insert");
        if ("append".equalsIgnoreCase(writeMode)) {
            // 投递语义 append → DataX 方言 insert（追加写）
            writeMode = "insert";
        }
        List<String> preSql = target.getPreSql();
        String targetColumn = getString(targetCfg, "targetColumn");
        if ("upsert".equalsIgnoreCase(writeMode) && StringUtils.isNotBlank(targetColumn) && StringUtils.isNotBlank(table)) {
            // DataX 方言：upsert = 先删后写（同库子查询，要求源表与目标表同实例可互访）
            preSql = Collections.singletonList("DELETE FROM " + table
                    + " WHERE " + targetColumn + " IN (SELECT " + targetColumn + " FROM " + getString(sourceCfg, "tableName") + ")");
            writeMode = "insert";
        }
        Integer batchSize = getInteger(targetCfg, "batchSize");
        JdbcDataXDataSource writerSource = buildJdbcSource(target);
        WriterContext writerContext = WriterContext.builder()
                .table(table)
                .columns(target.getColumns())
                .preSql(preSql)
                .writeMode(writeMode)
                .batchSize(batchSize == null ? 1000 : batchSize)
                .build();

        // 3. 组装 DataXJobBuildRequest
        DataXJobBuildRequest buildReq = DataXJobBuildRequest.builder()
                .readerSource(readerSource)
                .readerContext(readerContext)
                .writerSource(writerSource)
                .writerContext(writerContext)
                .settingSpeedChannel(ctx.getChannel() > 0 ? ctx.getChannel() : 3)
                .settingErrorRecord(ctx.getErrorRecord())
                .build();

        // 4. 序列化为 Map 放入 ExecutorRequest.config
        @SuppressWarnings("unchecked")
        Map<String, Object> configMap = DataXJsonHelper.sharedMapper()
                .convertValue(buildReq, Map.class);

        return ExecutorRequest.builder()
                .jobId(ctx.getJobId())
                .config(configMap)
                .build();
    }

    /** 把中性 Endpoint 转为 DataX 的 JdbcDataXDataSource（消费归一化后契约字段）。 */
    private JdbcDataXDataSource buildJdbcSource(DiContext.Endpoint ep) {
        String category = ep.getCategory();
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException("Endpoint.category is blank");
        }
        Map<String, Object> cfg = ep.getConfig() == null ? Collections.emptyMap() : ep.getConfig();
        String jdbcUrl = buildJdbcUrl(category, getString(cfg, "host"), getString(cfg, "port"),
                getString(cfg, "database"), getString(cfg, "properties"));
        String driverClass = resolveDriverClass(category);

        return JdbcDataXDataSource.builder()
                .category(category)
                .jdbcUrl(jdbcUrl)
                .driverClass(driverClass)
                .username(getString(cfg, "username"))
                .password(getString(cfg, "password"))
                .extraProps(Collections.emptyMap())
                .build();
    }

    private String getString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Integer getInteger(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, Object> safeConfig(DiContext.Endpoint ep) {
        return ep.getConfig() == null ? Collections.emptyMap() : ep.getConfig();
    }

    /** 按 category 拼接 JDBC URL（覆盖 mysql/oracle/postgresql/clickhouse）。 */
    private String buildJdbcUrl(String category, String host, String port,
                                String database, String properties) {
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("host is blank for category=" + category);
        }
        String dbPart = StringUtils.isNotBlank(database) ? "/" + database : "";
        String propsPart = StringUtils.isNotBlank(properties) ? "?" + properties : "";
        switch (category.toLowerCase()) {
            case "mysql":
                return "jdbc:mysql://" + host + ":"
                        + (StringUtils.isNotBlank(port) ? port : "3306") + dbPart + propsPart;
            case "postgresql":
                return "jdbc:postgresql://" + host + ":"
                        + (StringUtils.isNotBlank(port) ? port : "5432") + dbPart + propsPart;
            case "clickhouse":
                return "jdbc:clickhouse://" + host + ":"
                        + (StringUtils.isNotBlank(port) ? port : "8123") + dbPart + propsPart;
            case "oracle":
                return "jdbc:oracle:thin:@//" + host + ":"
                        + (StringUtils.isNotBlank(port) ? port : "1521") + dbPart;
            default:
                throw new IllegalArgumentException("DataX 不支持 [" + category + "] 作为同步端点");
        }
    }

    /** 按 category 推断 driverClass。 */
    private String resolveDriverClass(String category) {
        switch (category.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "clickhouse":
                return "ru.yandex.clickhouse.ClickHouseDriver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            default:
                throw new IllegalArgumentException("DataX 不支持 [" + category + "] 作为同步端点");
        }
    }
}
