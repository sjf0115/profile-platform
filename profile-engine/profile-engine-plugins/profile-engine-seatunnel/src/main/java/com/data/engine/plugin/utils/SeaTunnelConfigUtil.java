package com.data.engine.plugin.utils;

import com.data.profile.common.utils.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.shade.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.seatunnel.shade.com.typesafe.config.*;

import java.util.*;
import java.util.stream.Collectors;

public class SeaTunnelConfigUtil {

    public String generateJobConfig() {
        /*PluginType pluginType = PluginType.valueOf(task.getType().toUpperCase(Locale.ROOT));
        String pluginId = task.getPluginId();
        // 根据插件类型(Source/Sink) 和 Connector 获取 OptionRule
        OptionRule optionRule = connectorCache.getOptionRule(pluginType.getType(), task.getConnectorType());
        Config config = filterEmptyValue(parseConfigWithOptionRule(
                pluginType,
                task.getConnectorType(),
                task.getConfig(),
                optionRule));*/
        return "";
    }

    // 生成 Env HOCON Config
    public String getEnvConfig(Map<String,String> envMap) {
        Config envConfig = filterEmptyValue(ConfigFactory.parseString(JSONUtils.toJsonString(envMap)));
        String env = envConfig.root().render(
                ConfigRenderOptions.defaults()
                        .setJson(false)
                        .setComments(false)
                        .setOriginComments(false)
        );
        return env;
    }

    // 生成 Connector HOCON Config
    public String getConnectorConfig(Map<String, List<Config>> connectorMap) {
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

    // 作业配置模板
    public static final String CONFIG_TEMPLATE =
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

    public static String generateConfig(String env, String sources, String transforms, String sinks) {
        return CONFIG_TEMPLATE
                .replace("env_placeholder", env)
                .replace("source_placeholder", sources)
                .replace("transform_placeholder", transforms)
                .replace("sink_placeholder", sinks);
    }


    //------------------------------------------------------------------------------------------------------------------

    private Config parseConfigWithOptionRule(
            PluginType pluginType, String connectorType, String config, OptionRule optionRule) {
        return parseConfigWithOptionRule(pluginType, connectorType, ConfigFactory.parseString(config), optionRule);
    }

    private Config parseConfigWithOptionRule(PluginType pluginType, String connectorType, Config config, OptionRule optionRule) {
        Map<String, TypeReference<?>> typeReferenceMap = new HashMap<>();
        optionRule.getOptionalOptions()
                .forEach(option -> typeReferenceMap.put(option.key(), option.typeReference()));
        optionRule.getRequiredOptions()
                .forEach(
                        options -> {
                            options.getOptions()
                                    .forEach(
                                            option -> {
                                                typeReferenceMap.put(
                                                        option.key(), option.typeReference());
                                            });
                        });

        Map<String, ConfigObject> needReplaceMap = new HashMap<>();
        Map<String, ConfigValue> needReplaceList = new HashMap<>();

        config.entrySet().forEach(
                entry -> {
                    String key = entry.getKey();
                    ConfigValue configValue = entry.getValue();
                    try {
                        if (typeReferenceMap.containsKey(key)
                                && isComplexType(typeReferenceMap.get(key))
                                && !isEmptyValue(configValue)) {
                            String valueStr = configValue.unwrapped().toString();
                            if (typeReferenceMap
                                    .get(key)
                                    .getType()
                                    .getTypeName()
                                    .startsWith("java.util.List")
                                    || typeReferenceMap
                                    .get(key)
                                    .getType()
                                    .getTypeName()
                                    .startsWith(
                                            "org.apache.seatunnel.api.configuration.Options")) {
                                String valueWrapper = "{key=" + valueStr + "}";
                                ConfigValue configList =
                                        ConfigFactory.parseString(valueWrapper)
                                                .getList("key");
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

    private boolean isComplexType(TypeReference<?> typeReference) {
        return typeReference.getType().getTypeName().startsWith("java.util.List")
                || typeReference.getType().getTypeName().startsWith("java.util.Map")
                || typeReference
                .getType()
                .getTypeName()
                .startsWith("org.apache.seatunnel.api.configuration.Options");
    }

    private Config filterEmptyValue(Config config) {
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

    private boolean isEmptyValue(ConfigValue value) {
        return value.unwrapped().toString().isEmpty() || value.valueType().equals(ConfigValueType.NULL);
    }
}
