package com.data.engine.plugin.utils;

import com.data.engine.plugin.bean.ConnectorCache;
import com.data.engine.plugin.bean.JobTask;
import com.data.profile.common.utils.JSONUtils;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.shade.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.seatunnel.shade.com.typesafe.config.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SeaTunnelConfigUtil {

    public static void main(String[] args) {
        JobTask task = JobTask.builder()
                .type("source")
                .connectorType("jdbc")
                .name("test-source-job")
                .config("{\"schema_save_mode\":\"CREATE_SCHEMA_WHEN_NOT_EXIST\",\"data_save_mode\":\"APPEND_DATA\",\"create_index\":\"true\",\"connection_check_timeout_sec\":\"30\",\"batch_size\":\"1000\",\"is_exactly_once\":\"false\",\"xa_data_source_class_name\":\"\",\"max_commit_attempts\":\"3\",\"transaction_timeout_sec\":\"-1\",\"max_retries\":\"0\",\"auto_commit\":\"true\",\"support_upsert_by_query_primary_key_exist\":\"false\",\"primary_keys\":\"\",\"compatible_mode\":\"\",\"multi_table_sink_replica\":\"1\"}")
                .selectTableFields("{\"tableFields\":[\"id\",\"name\",\"age\",\"email\"],\"all\":true}")
                .dataSourceOption("")
                .outputSchema("[{\"fields\":[{\"type\":\"BIGINT\",\"name\":\"id\",\"comment\":\"主键ID\",\"primaryKey\":true,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"BIGINT\"},{\"type\":\"VARCHAR\",\"name\":\"name\",\"comment\":\"姓名\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"STRING\"},{\"type\":\"INT\",\"name\":\"age\",\"comment\":\"年龄\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"INT\"},{\"type\":\"VARCHAR\",\"name\":\"email\",\"comment\":\"邮箱\",\"primaryKey\":false,\"defaultValue\":null,\"nullable\":false,\"properties\":null,\"unSupport\":false,\"outputDataType\":\"STRING\"}],\"tableName\":\"tb_user\",\"database\":\"test\"}]")
                .dataSourceId(11212L)
                .build();
        try {
            generateJobConfig(task);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateJobConfig(JobTask task) throws IOException {
        // 第一部分: 环境配置 Env
        Map<String, Object> jobEnv = Maps.newHashMap();
        jobEnv.put("env.job.mode", "BATCH");
        jobEnv.put("env.parallelism", 2);
        jobEnv.put("env.job.name", StringUtils.isBlank(task.getName()) ? "SeaTunnelJob" : task.getName());
        String env = getEnvConfig(jobEnv);

        // 第二部分：Source
        PluginType pluginType = PluginType.valueOf(task.getType().toUpperCase(Locale.ROOT));
        String pluginId = task.getPluginId();
        // 根据插件类型(Source/Sink) 和 Connector 获取 OptionRule
        ConnectorCache connectorCache = new ConnectorCache();
        // Connector 配置参数
        OptionRule optionRule = connectorCache.getOptionRule(pluginType.getType(), task.getConnectorType());
        Config config = filterEmptyValue(
                    parseConfigWithOptionRule(
                        pluginType,
                        task.getConnectorType(),
                        task.getConfig(),
                        optionRule
                    )
            );

        Map<String, List<Config>> sourceMap = new LinkedHashMap<>();
        String sources = "";

        // 第三部分：Transforms
        String transforms = "transform {}";

        // 第四部分：Sink
        String sinks = "";

        // 合并生成作业配置
        String jobConfig = generateConfig(env, sources, transforms, sinks);
        log.info("作业配置：{}", jobConfig);
        return jobConfig;
    }

    // 生成 Connector HOCON Config
    public static String getConnectorConfig(Map<String, List<Config>> connectorMap) {
        List<String> configs = new ArrayList<>();
        ConfigRenderOptions configRenderOptions = ConfigRenderOptions.defaults()
                .setJson(false)
                .setComments(false)
                .setOriginComments(false);
        for (Map.Entry<String, List<Config>> entry : connectorMap.entrySet()) {
            for (Config c : entry.getValue()) {
                configs.add(ConfigFactory.empty()
                                .withValue(entry.getKey(), c.root())
                                .root()
                                .render(configRenderOptions));
            }
        }
        return StringUtils.join(configs, "\n");
    }

    public static String generateConfig(String env, String sources, String transforms, String sinks) {
        return CONFIG_TEMPLATE
                .replace("env_placeholder", env)
                .replace("source_placeholder", sources)
                .replace("transform_placeholder", transforms)
                .replace("sink_placeholder", sinks);
    }

    //------------------------------------------------------------------------------------------------------------------

    // 生成 Env HOCON Config
    private static String getEnvConfig(Map<String,Object> envMap) {
        Config envConfig = filterEmptyValue(ConfigFactory.parseMap(envMap));
        String env = envConfig.root().render(
                ConfigRenderOptions.defaults()
                        .setJson(false)
                        .setComments(false)
                        .setFormatted(true)
                        .setOriginComments(false)
        );
        log.info("Env 配置：{}", env);
        return env;
    }

    // 作业配置模板
    private static final String CONFIG_TEMPLATE =
            "env {\n"
                    + "env_placeholder"
                    + "}\n"
                    + "source {\n"
                    + "source_placeholder"
                    + "}\n"
                    + "transform {\n"
                    + "transform_placeholder"
                    + "}\n"
                    + "sink {\n"
                    + "sink_placeholder"
                    + "}\n";

    private static Config parseConfigWithOptionRule(PluginType pluginType, String connectorType, String config, OptionRule optionRule) {
        return parseConfigWithOptionRule(pluginType, connectorType, ConfigFactory.parseString(config), optionRule);
    }

    private static Config parseConfigWithOptionRule(PluginType pluginType, String connectorType, Config config, OptionRule optionRule) {
        Map<String, TypeReference<?>> typeReferenceMap = new HashMap<>();
        // 可选选项
        optionRule.getOptionalOptions().forEach(option -> typeReferenceMap.put(option.key(), option.typeReference()));
        // 必选选型
        optionRule.getRequiredOptions().forEach(
                        options -> {
                            options.getOptions().forEach(option -> {typeReferenceMap.put(option.key(), option.typeReference());});
                        });

        Map<String, ConfigObject> needReplaceMap = new HashMap<>();
        Map<String, ConfigValue> needReplaceList = new HashMap<>();

        config.entrySet().forEach(
                entry -> {
                    String key = entry.getKey();
                    ConfigValue configValue = entry.getValue();
                    try {
                        // 复杂类型
                        if (typeReferenceMap.containsKey(key) && isComplexType(typeReferenceMap.get(key)) && !isEmptyValue(configValue)) {
                            String valueStr = configValue.unwrapped().toString();
                            boolean isList = typeReferenceMap.get(key)
                                    .getType()
                                    .getTypeName()
                                    .startsWith("java.util.List");
                            boolean isOption = typeReferenceMap.get(key)
                                    .getType()
                                    .getTypeName()
                                    .startsWith("org.apache.seatunnel.api.configuration.Options");
                            if (isList || isOption) {
                                String valueWrapper = "{key=" + valueStr + "}";
                                ConfigValue configList = ConfigFactory.parseString(valueWrapper).getList("key");
                                needReplaceList.put(key, configList);
                            } else {
                                Config configObject = ConfigFactory.parseString(valueStr);
                                needReplaceMap.put(key, configObject.root());
                            }
                        }
                    } catch (Exception e) {
                        /*throw new SeatunnelException(SeatunnelErrorEnum.ERROR_CONFIG,
                                String.format(
                                        "Plugin Type: %s, Connector Type: %s, Key: %s, Error Info: %s",
                                        pluginType, connectorType, key, e.getMessage()));*/
                    }
                });
        for (Map.Entry<String, ConfigObject> entry : needReplaceMap.entrySet()) {
            config = config.withValue(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, ConfigValue> entry : needReplaceList.entrySet()) {
            config = config.withValue(entry.getKey(), entry.getValue());
        }
        return config;
    }

    private static boolean isComplexType(TypeReference<?> typeReference) {
        return typeReference.getType().getTypeName().startsWith("java.util.List")
                || typeReference.getType().getTypeName().startsWith("java.util.Map")
                || typeReference
                .getType()
                .getTypeName()
                .startsWith("org.apache.seatunnel.api.configuration.Options");
    }

    private static Config filterEmptyValue(Config config) {
        List<String> removeKeys =
                config.entrySet().stream()
                        .filter(entry -> isEmptyValue(entry.getValue()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
        for (String removeKey : removeKeys) {
            config = config.withoutPath(removeKey);
        }
        return config;
    }

    private static boolean isEmptyValue(ConfigValue value) {
        return value.unwrapped().toString().isEmpty() || value.valueType().equals(ConfigValueType.NULL);
    }
}
