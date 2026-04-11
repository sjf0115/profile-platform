package com.data.notification.core;


import com.data.notification.api.entity.SlaConfigMessage;
import com.data.notification.api.entity.SlaNotificationMessage;
import com.data.notification.api.entity.SlaNotificationResult;
import com.data.notification.api.entity.SlaSenderMessage;
import com.data.notification.api.spi.SlasHandlerPlugin;
import com.data.profile.common.exception.ProfileException;
import com.data.spi.PluginLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class NotificationManager {

    private final Set<String> supportedPlugins;

    public NotificationManager(){
        supportedPlugins = PluginLoader.getPluginLoader(SlasHandlerPlugin.class)
                .getSupportedPlugins();
    }

    public SlaNotificationResult notify(SlaNotificationMessage slaNotificationMessage, Map<SlaSenderMessage, Set<SlaConfigMessage>> config){
        if (config == null || config.isEmpty()){
            throw new ProfileException("message cannot be send without sender and receiver");
        }
        SlaNotificationResult result = new SlaNotificationResult();
        result.setStatus(true);

        for (Map.Entry<SlaSenderMessage, Set<SlaConfigMessage>> entry: config.entrySet()) {
            String type = entry.getKey().getType();
            if (!supportedPlugins.contains(type)) {
                throw new ProfileException("sender type not support of "+ type);
            }
            SlasHandlerPlugin handlerPlugin = PluginLoader
                    .getPluginLoader(SlasHandlerPlugin.class)
                    .getOrCreatePlugin(type);
            Map<SlaSenderMessage, Set<SlaConfigMessage>> senderEntity = new HashMap(){
                {
                    put(entry.getKey(), entry.getValue());
                }
            };
            SlaNotificationResult entryResult = handlerPlugin.notify(slaNotificationMessage, senderEntity);
            result.merge(entryResult);
        }
        return result;
    }

}
