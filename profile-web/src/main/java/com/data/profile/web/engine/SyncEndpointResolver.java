package com.data.profile.web.engine;

import com.data.connector.api.ConnectorFactory;
import com.data.engine.api.DiContext;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.config.ProfileEngineConfig;
import com.data.profile.web.model.DataSource;
import com.data.spi.DiConfigTranslator;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步端点分派器（导入/导出对称）
 * <p>职责：按 category 分派到 connector 插件翻译配置，构建中性 {@link DiContext.Endpoint}。
 * 平台不感知任何配置字段名：字段名知识收敛在归属插件（翻译①），
 * DI 引擎插件消费统一契约后自行转换为引擎方言（翻译②）。</p>
 */
@Slf4j
@Component
public class SyncEndpointResolver {

    @Resource
    private ProfileEngineConfig engineProperties;

    /** 便捷入口：三方数据源 → 源端点（导入场景，表参数并入中性参数集）。 */
    public DiContext.Endpoint resolveSource(DataSource dataSource, String tableName, List<String> columns) {
        return resolveSource(dataSource.getDatasourceType(), dataSource.getConfig(), tableName, columns);
    }

    /** 源端点（字符串版：分析引擎作源与三方数据源作源共用同一翻译路径）。 */
    public DiContext.Endpoint resolveSource(String category, String configJson, String tableName, List<String> columns) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        return DiContext.Endpoint.builder()
                .category(category)
                .config(merge(translate(category, parseConfig(configJson)), params))
                .columns(columns)
                .build();
    }

    /** 目标端点：连接配置翻译（翻译①）+ 平台级写入策略并入中性参数集（分析引擎作目标 / 三方数据源作目标共用）。 */
    public DiContext.Endpoint resolveTarget(String category, String configJson, String tableName, List<String> columns) {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("writeMode", engineProperties.getSyncWriteMode());
        params.put("batchSize", engineProperties.getSyncBatchSize());
        return DiContext.Endpoint.builder()
                .category(category)
                .config(merge(translate(category, parseConfig(configJson)), params))
                .columns(columns)
                .build();
    }

    /** 仅翻译配置（供引擎侧端点组装：分析引擎作源 / 作目标时配置翻译入口）。 */
    public Map<String, Object> translateConfig(String category, String configJson) {
        return translate(category, parseConfig(configJson));
    }

    /**
     * 导出目标端点（投递即同步）：连接配置翻译（翻译①）+ 投递参数并入中性参数集。
     * <p>平台不做类型分派、不解析投递字段含义：投递参数是用户按 connector 表单填写的中性配置（仅含数据源配置之外的字段），
     * 语义由引擎插件按方言消费。</p>
     */
    public DiContext.Endpoint resolveExportTarget(String category, String configJson, Map<String, Object> delivery, List<String> columns) {
        return DiContext.Endpoint.builder()
                .category(category)
                .config(merge(translate(category, parseConfig(configJson)), delivery))
                .columns(columns)
                .build();
    }

    /** 核心分派：按 category 取 connector 插件翻译；无翻译器则原样直通。 */
    private Map<String, Object> translate(String category, Map<String, Object> rawConfig) {
        if (rawConfig == null || rawConfig.isEmpty()) {
            return Collections.emptyMap();
        }
        if (StringUtils.isBlank(category)) {
            throw new IllegalArgumentException("端点 category 不能为空");
        }
        ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class)
                .getOrCreatePlugin(StringUtils.lowerCase(StringUtils.trimToEmpty(category)));
        DiConfigTranslator translator = factory.getDiConfigTranslator();
        if (translator == null) {
            return rawConfig;
        }
        return translator.translate(rawConfig);
    }

    /** 组装侧合并：连接参数（翻译①产出）+ 写入参数（平台策略/投递表单），同键以后者为准。 */
    private Map<String, Object> merge(Map<String, Object> base, Map<String, Object> extra) {
        Map<String, Object> merged = new HashMap<>(base == null ? Collections.emptyMap() : base);
        if (extra != null) {
            extra.forEach((key, value) -> {
                if (value != null) {
                    merged.put(key, value);
                }
            });
        }
        return merged;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }
        return JSONUtils.parseObject(json, Map.class);
    }
}
