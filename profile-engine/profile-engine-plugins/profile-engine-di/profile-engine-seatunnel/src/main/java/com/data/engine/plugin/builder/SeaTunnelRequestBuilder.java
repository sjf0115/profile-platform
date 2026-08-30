package com.data.engine.plugin.builder;

import com.data.engine.api.DiContext;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.common.ExecutorRequest;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SeaTunnel 引擎的 DiRequestBuilder 实现。
 *
 * <p>把中性 {@link DiContext} 翻译为 SeaTunnel 私有的 HOCON 作业配置（翻译②，引擎方言）：
 * 消费归一化后的端点契约字段（host/port/database/username/password/properties），
 * 按 category 映射 SeaTunnel 连接器（Jdbc/Clickhouse），写入临时 conf 文件，
 * 通过 {@link ExecutorRequest#getConfigPath()} 交给 {@code SeaTunnelDiEngineExecutor} 提交。</p>
 *
 * <p>引擎模块对 web 层模型（DataSource/Dataset/Engine）零依赖。</p>
 *
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class SeaTunnelRequestBuilder implements DiRequestBuilder {

    @Override
    public ExecutorRequest buildRequest(DiContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("DiContext is null");
        }
        DiContext.Endpoint source = ctx.getSource();
        DiContext.Endpoint target = ctx.getTarget();
        if (source == null || target == null) {
            throw new IllegalArgumentException("DiContext.source/target is null");
        }

        String conf = buildJobConf(ctx);
        Path confPath;
        try {
            confPath = Files.createTempFile("seatunnel_job_" + ctx.getJobId() + "_", ".conf");
            Files.write(confPath, conf.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("SeaTunnel 作业配置文件写入失败: " + e.getMessage(), e);
        }

        return ExecutorRequest.builder()
                .jobId(ctx.getJobId())
                .configPath(confPath.toAbsolutePath().toString())
                .build();
    }

    /** 组装 SeaTunnel HOCON 作业配置：env + source + sink。 */
    private String buildJobConf(DiContext ctx) {
        StringBuilder sb = new StringBuilder();
        // env 段
        sb.append("env {\n");
        sb.append("  job.name = \"").append(ctx.getJobId()).append("\"\n");
        if (ctx.getChannel() > 0) {
            sb.append("  parallelism = ").append(ctx.getChannel()).append("\n");
        }
        sb.append("}\n\n");
        // source 段
        sb.append("source {\n");
        sb.append(buildSourceBlock(ctx.getSource()));
        sb.append("}\n\n");
        // sink 段
        sb.append("sink {\n");
        sb.append(buildSinkBlock(ctx.getTarget()));
        sb.append("}\n");
        return sb.toString();
    }

    /** source 段：按 category 映射 SeaTunnel 连接器。 */
    private String buildSourceBlock(DiContext.Endpoint ep) {
        String category = requireCategory(ep);
        Map<String, Object> cfg = safeConfig(ep);
        if (isJdbcCategory(category)) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Jdbc {\n");
            sb.append("    url = \"").append(buildJdbcUrl(category, cfg)).append("\"\n");
            sb.append("    driver = \"").append(resolveDriverClass(category)).append("\"\n");
            appendIfNotBlank(sb, "user", getString(cfg, "username"));
            appendIfNotBlank(sb, "password", getString(cfg, "password"));
            sb.append("    query = \"select ").append(joinColumns(ep.getColumns()))
                    .append(" from ").append(getString(cfg, "tableName")).append("\"\n");
            sb.append("  }\n");
            return sb.toString();
        }
        throw new IllegalArgumentException("SeaTunnel 同步暂不支持 [" + category + "] 作为源端点");
    }

    /** sink 段：按 category 映射 SeaTunnel 连接器。 */
    private String buildSinkBlock(DiContext.Endpoint ep) {
        // 写入参数（导入平台策略 / 导出投递参数）已并入中性参数集 config，统一消费；
        // SeaTunnel Jdbc sink 无先删后写能力，upsert 场景需切换默认 DI 引擎（能力声明归引擎插件）
        String category = requireCategory(ep);
        Map<String, Object> cfg = safeConfig(ep);
        if ("upsert".equalsIgnoreCase(getString(cfg, "writeMode"))
                || (ep.getPreSql() != null && !ep.getPreSql().isEmpty())) {
            throw new IllegalArgumentException("该引擎暂不支持 upsert 先删后写，请切换默认 DI 引擎（如 DataX）");
        }
        String table = getString(cfg, "tableName");
        if ("clickhouse".equalsIgnoreCase(category)) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Clickhouse {\n");
            sb.append("    host = \"").append(getString(cfg, "host"));
            String port = getString(cfg, "port");
            if (StringUtils.isNotBlank(port)) {
                sb.append(":").append(port);
            }
            sb.append("\"\n");
            appendIfNotBlank(sb, "database", getString(cfg, "database"));
            sb.append("    table = \"").append(table).append("\"\n");
            appendIfNotBlank(sb, "username", getString(cfg, "username"));
            appendIfNotBlank(sb, "password", getString(cfg, "password"));
            Integer batchSize = getInteger(cfg, "batchSize");
            if (batchSize != null) {
                sb.append("    bulk_size = ").append(batchSize).append("\n");
            }
            sb.append("  }\n");
            return sb.toString();
        }
        if (isJdbcCategory(category)) {
            StringBuilder sb = new StringBuilder();
            sb.append("  Jdbc {\n");
            sb.append("    url = \"").append(buildJdbcUrl(category, cfg)).append("\"\n");
            sb.append("    driver = \"").append(resolveDriverClass(category)).append("\"\n");
            appendIfNotBlank(sb, "user", getString(cfg, "username"));
            appendIfNotBlank(sb, "password", getString(cfg, "password"));
            appendIfNotBlank(sb, "database", getString(cfg, "database"));
            sb.append("    table = \"").append(table).append("\"\n");
            sb.append("    generate_sink_sql = true\n");
            Integer batchSize = getInteger(cfg, "batchSize");
            if (batchSize != null) {
                sb.append("    batch_size = ").append(batchSize).append("\n");
            }
            sb.append("  }\n");
            return sb.toString();
        }
        throw new IllegalArgumentException("SeaTunnel 同步暂不支持 [" + category + "] 作为目标端点");
    }

    // -----------------------------------------------------------------
    // 方言辅助：category → SeaTunnel 参数（JDBC URL / driver 拼法属 SeaTunnel 方言）
    // -----------------------------------------------------------------

    private boolean isJdbcCategory(String category) {
        switch (category.toLowerCase()) {
            case "mysql":
            case "postgresql":
            case "clickhouse":
            case "oracle":
                return true;
            default:
                return false;
        }
    }

    /** 按 category 拼接 JDBC URL（SeaTunnel Jdbc 连接器消费）。 */
    private String buildJdbcUrl(String category, Map<String, Object> cfg) {
        String host = getString(cfg, "host");
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("host is blank for category=" + category);
        }
        String port = getString(cfg, "port");
        String database = getString(cfg, "database");
        String properties = getString(cfg, "properties");
        String dbPart = StringUtils.isNotBlank(database) ? "/" + database : "";
        String propsPart = StringUtils.isNotBlank(properties) ? "?" + properties : "";
        switch (category.toLowerCase()) {
            case "mysql":
                return "jdbc:mysql://" + host + ":" + (StringUtils.isNotBlank(port) ? port : "3306") + dbPart + propsPart;
            case "postgresql":
                return "jdbc:postgresql://" + host + ":" + (StringUtils.isNotBlank(port) ? port : "5432") + dbPart + propsPart;
            case "clickhouse":
                return "jdbc:clickhouse://" + host + ":" + (StringUtils.isNotBlank(port) ? port : "8123") + dbPart + propsPart;
            case "oracle":
                return "jdbc:oracle:thin:@//" + host + ":" + (StringUtils.isNotBlank(port) ? port : "1521") + dbPart;
            default:
                throw new IllegalArgumentException("SeaTunnel 同步暂不支持 [" + category + "] 端点");
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
                throw new IllegalArgumentException("SeaTunnel 同步暂不支持 [" + category + "] 端点");
        }
    }

    private String requireCategory(DiContext.Endpoint ep) {
        String category = ep.getCategory();
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException("Endpoint.category is blank");
        }
        return category;
    }

    private Map<String, Object> safeConfig(DiContext.Endpoint ep) {
        return ep.getConfig() == null ? Collections.emptyMap() : ep.getConfig();
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

    private void appendIfNotBlank(StringBuilder sb, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            sb.append("    ").append(key).append(" = \"").append(value).append("\"\n");
        }
    }

    private String joinColumns(List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return "*";
        }
        return String.join(", ", columns);
    }
}
