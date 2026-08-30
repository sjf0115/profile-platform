package com.data.profile.web.engine;

import com.data.engine.api.DiContext;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.config.ProfileEngineConfig;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Engine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 同步端点翻译器
 * <p>职责：将平台配置（业务数据源 / 分析引擎）翻译为中性的 {@link DiContext.Endpoint}。
 * 这里是"平台 config schema → 同步端点"的唯一翻译点：
 * config 字段名（host/port/user/username/database/schema）的认知收敛在此处，
 * 引擎插件只消费中性端点，业务层不感知字段细节。</p>
 */
@Slf4j
@Component
public class SyncEndpointResolver {

    @Resource
    private ProfileEngineConfig engineProperties;

    /** 从业务 DataSource 构建 DIContext source 端点。 */
    public DiContext.Endpoint resolveSource(DataSource dataSource, String tableName, List<String> columns) {
        Map<String, Object> cfg = parseConfig(dataSource.getConfig());
        return DiContext.Endpoint.builder()
                .category(dataSource.getDatasourceType())
                .host(getString(cfg, "host"))
                .port(getString(cfg, "port"))
                .database(firstNonBlank(getString(cfg, "database"), getString(cfg, "schema")))
                .username(firstNonBlank(getString(cfg, "user"), getString(cfg, "username")))
                .password(getString(cfg, "password"))
                .properties(getString(cfg, "properties"))
                .tableName(tableName)
                .columns(columns)
                .build();
    }

    /** 从分析 Engine 构建 DIContext target 端点。 */
    public DiContext.Endpoint resolveTarget(Engine analysisEngine, String tableName, List<String> columns) {
        Map<String, Object> cfg = parseConfig(analysisEngine.getConfig());
        return DiContext.Endpoint.builder()
                .category(analysisEngine.getEngineType())
                .host(getString(cfg, "host"))
                .port(getString(cfg, "port"))
                .database(firstNonBlank(getString(cfg, "database"), getString(cfg, "schema")))
                .username(firstNonBlank(getString(cfg, "user"), getString(cfg, "username")))
                .password(getString(cfg, "password"))
                .properties(getString(cfg, "properties"))
                .tableName(tableName)
                .columns(columns)
                .writeMode(engineProperties.getSyncWriteMode())
                .batchSize(engineProperties.getSyncBatchSize())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        return JSONUtils.parseObject(json, Map.class);
    }

    private String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                return v;
            }
        }
        return null;
    }
}
