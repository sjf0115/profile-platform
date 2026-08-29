package com.data.engine.plugin.datax.builder;

import com.data.engine.api.DiRequestBuilder;
import com.data.engine.api.DiContext;
import com.data.engine.common.ExecutorRequest;
import com.data.engine.plugin.datax.datasource.JdbcDataXDataSource;
import com.data.engine.plugin.datax.helper.DataXJobBuildRequest;
import com.data.engine.plugin.datax.helper.DataXJsonHelper;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
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

        // 1. Reader 侧
        JdbcDataXDataSource readerSource = buildJdbcSource(source);
        ReaderContext readerContext = ReaderContext.builder()
                .table(source.getTableName())
                .columns(source.getColumns())
                .build();

        // 2. Writer 侧
        JdbcDataXDataSource writerSource = buildJdbcSource(target);
        WriterContext writerContext = WriterContext.builder()
                .table(target.getTableName())
                .columns(target.getColumns())
                .writeMode(StringUtils.defaultIfBlank(target.getWriteMode(), "insert"))
                .batchSize(target.getBatchSize() == null ? 1000 : target.getBatchSize())
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

    /** 把 SyncContext.Endpoint 转为 DataX 的 JdbcDataXDataSource。 */
    private JdbcDataXDataSource buildJdbcSource(DiContext.Endpoint ep) {
        String category = ep.getCategory();
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException("Endpoint.category is blank");
        }
        String jdbcUrl = buildJdbcUrl(category, ep.getHost(), ep.getPort(),
                ep.getDatabase(), ep.getProperties());
        String driverClass = resolveDriverClass(category);

        return JdbcDataXDataSource.builder()
                .category(category)
                .jdbcUrl(jdbcUrl)
                .driverClass(driverClass)
                .username(ep.getUsername())
                .password(ep.getPassword())
                .extraProps(Collections.emptyMap())
                .build();
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
                throw new IllegalArgumentException("Unsupported category for DataX sync: " + category);
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
                throw new IllegalArgumentException("Unsupported category for DataX sync: " + category);
        }
    }
}
