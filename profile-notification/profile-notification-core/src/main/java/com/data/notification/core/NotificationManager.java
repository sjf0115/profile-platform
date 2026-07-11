package com.data.notification.core;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.notification.api.enums.NotificationTemplate;
import com.data.notification.api.spi.NotificationHandlerPlugin;
import com.data.notification.core.engine.NotificationTemplateEngine;
import com.data.profile.common.exception.ProfileException;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class NotificationManager {

    private final Set<String> supportedPlugins;

    @Autowired
    private NotificationTemplateEngine templateEngine;

    public NotificationManager(){
        supportedPlugins = PluginLoader.getPluginLoader(NotificationHandlerPlugin.class)
                .getSupportedPlugins();
    }

    public NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config){
        if (config == null || config.isEmpty()){
            throw new ProfileException("message cannot be send without sender and receiver");
        }
        NotificationResult result = new NotificationResult();
        result.setStatus(true);

        for (Map.Entry<NotificationSenderConfig, Set<NotificationReceiverConfig>> entry: config.entrySet()) {
            String type = entry.getKey().getType();
            if (!supportedPlugins.contains(type)) {
                throw new ProfileException("sender type not support of "+ type);
            }
            NotificationHandlerPlugin handlerPlugin = PluginLoader
                    .getPluginLoader(NotificationHandlerPlugin.class)
                    .getOrCreatePlugin(type);
            Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> senderEntity = new HashMap<>();
            senderEntity.put(entry.getKey(), entry.getValue());
            NotificationResult entryResult = handlerPlugin.notify(notificationMessage, senderEntity);
            result.merge(entryResult);
        }
        return result;
    }

    /**
     * 简单单发：指定渠道 + 收件人配置 JSON + 主题 + 内容
     */
    public NotificationResult send(String channel, String receiverConfigJson, String subject, String content) {
        NotificationSenderConfig sender = new NotificationSenderConfig();
        sender.setType(channel);
        sender.setConfig("{}");

        NotificationReceiverConfig receiver = new NotificationReceiverConfig();
        receiver.setType(channel);
        receiver.setConfig(receiverConfigJson);

        NotificationMessage message = new NotificationMessage();
        message.setSubject(subject);
        message.setMessage(content);

        Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config = new HashMap<>();
        config.put(sender, new HashSet<>(Collections.singletonList(receiver)));

        return notify(message, config);
    }

    /**
     * 基于 FreeMarker 模板发送（内部调用 NotificationTemplateEngine.render 后转发给插件）
     */
    public NotificationResult sendByTemplate(String channel, String receiverConfigJson, String subject,
                                             NotificationTemplate template, Map<String, Object> params) {
        String renderedContent = templateEngine.render(template, params);
        return send(channel, receiverConfigJson, subject, renderedContent);
    }

}
