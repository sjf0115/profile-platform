package com.data.profile.web.service;

import com.data.notification.api.spi.NotificationHandlerPlugin;
import com.data.profile.web.dao.SystemConfigMapper;
import com.data.profile.web.model.SystemConfig;
import com.data.profile.web.security.UserContextHolder;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    /**
     * 按组获取所有配置，返回 key-value Map
     */
    public Map<String, String> getConfigByGroup(String group) {
        List<SystemConfig> configs = systemConfigMapper.selectByGroup(group);
        return configs.stream().collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                c -> StringUtils.defaultString(c.getConfigValue()),
                (v1, v2) -> v2
        ));
    }

    /**
     * 获取全部配置，按组嵌套
     */
    public Map<String, Map<String, String>> getAllConfig() {
        List<SystemConfig> configs = systemConfigMapper.selectAll();
        Map<String, Map<String, String>> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.computeIfAbsent(config.getConfigGroup(), k -> new HashMap<>())
                    .put(config.getConfigKey(), StringUtils.defaultString(config.getConfigValue()));
        }
        return result;
    }

    /**
     * 按组批量保存配置
     */
    public void saveConfig(String group, Map<String, String> configMap) {
        String userId = UserContextHolder.currentUserId();
        List<SystemConfig> configs = configMap.entrySet().stream()
                .map(entry -> SystemConfig.builder()
                        .configGroup(group)
                        .configKey(entry.getKey())
                        .configValue(entry.getValue())
                        .creator(userId)
                        .modifier(userId)
                        .build())
                .collect(Collectors.toList());
        if (!configs.isEmpty()) {
            systemConfigMapper.batchUpsert(configs);
        }
    }

    /**
     * 获取 SMTP 发送端表单 Schema（来自邮件插件的 getConfigSenderJson）
     */
    public String getSmtpConfigForm() {
        NotificationHandlerPlugin emailPlugin = PluginLoader
                .getPluginLoader(NotificationHandlerPlugin.class)
                .getOrCreatePlugin("email");
        return emailPlugin.getConfigSenderJson();
    }

}
