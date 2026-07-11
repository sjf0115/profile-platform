package com.data.notification.plugin.wecom;

import com.data.notification.api.entity.*;
import com.data.notification.api.spi.NotificationHandlerPlugin;
import com.data.notification.plugin.wecom.entity.ReceiverConfig;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.type.InputParam;
import com.data.profile.common.utils.JSONUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class WecomNotificationHandlerPlugin implements NotificationHandlerPlugin {

    @Override
    public NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config) {
        Set<NotificationSenderConfig> wecomSenderSet = config.keySet().stream()
                .filter(x -> "wecom".equals(x.getType())).collect(Collectors.toSet());
        NotificationResult result = new NotificationResult();
        ArrayList<NotificationResultRecord> records = new ArrayList<>();
        result.setStatus(true);
        String subject = notificationMessage.getSubject();
        String message = notificationMessage.getMessage();
        for (NotificationSenderConfig senderMessage : wecomSenderSet) {
            WecomSender wecomSender = new WecomSender();
            Set<NotificationReceiverConfig> receiverConfigSet = config.get(senderMessage);
            HashSet<ReceiverConfig> toReceivers = new HashSet<>();
            for (NotificationReceiverConfig receiver : receiverConfigSet) {
                String receiverConfigStr = receiver.getConfig();
                ReceiverConfig receiverConfig = JSONUtils.parseObject(receiverConfigStr, ReceiverConfig.class);
                toReceivers.add(receiverConfig);
            }

            NotificationResultRecord record = wecomSender.sendMsg(toReceivers, subject, message);
            if (record.getStatus().equals(false)) {
                record.setMessage(record.getMessage());
                result.setStatus(false);
            }
            records.add(record);
        }
        result.setRecords(records);
        return result;
    }

    @Override
    public String getConfigSenderJson() {
        List<PluginParams> paramsList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;
        try {
            result = mapper.writeValueAsString(paramsList);
        } catch (JsonProcessingException e) {
            log.error("json parse error : {}", e.getMessage(), e);
        }
        return result;
    }

    @Override
    public String getConfigJson() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam webhook = InputParam.newBuilder("webhook", "webhook")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        paramsList.add(webhook);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String result = null;
        try {
            result = mapper.writeValueAsString(paramsList);
        } catch (JsonProcessingException e) {
            log.error("json parse error : {}", e.getMessage(), e);
        }
        return result;
    }
}
