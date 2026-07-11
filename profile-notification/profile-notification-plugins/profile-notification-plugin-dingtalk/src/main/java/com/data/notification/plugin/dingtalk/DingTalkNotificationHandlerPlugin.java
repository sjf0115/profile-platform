package com.data.notification.plugin.dingtalk;

import com.data.notification.api.entity.*;
import com.data.notification.api.spi.NotificationHandlerPlugin;
import com.data.notification.plugin.dingtalk.entity.ReceiverConfig;
import com.data.profile.common.domain.connector.param.ParamsOptions;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.type.InputParam;
import com.data.profile.common.domain.connector.param.type.RadioParam;
import com.data.profile.common.utils.JSONUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.data.notification.api.constants.NotificationConstants.*;

@Slf4j
public class DingTalkNotificationHandlerPlugin implements NotificationHandlerPlugin {

    @Override
    public NotificationResult notify(NotificationMessage notificationMessage, Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config) {
        Set<NotificationSenderConfig> dingTalkSenderSet = config.keySet().stream()
                .filter(x -> "dingtalk".equals(x.getType())).collect(Collectors.toSet());
        NotificationResult result = new NotificationResult();
        ArrayList<NotificationResultRecord> records = new ArrayList<>();
        result.setStatus(true);
        String subject = notificationMessage.getSubject();
        String message = notificationMessage.getMessage();
        for (NotificationSenderConfig senderMessage : dingTalkSenderSet) {
            DingTalkSender dingTalkSender = new DingTalkSender();
            Set<NotificationReceiverConfig> receiverConfigSet = config.get(senderMessage);
            HashSet<ReceiverConfig> toReceivers = new HashSet<>();
            for (NotificationReceiverConfig receiver : receiverConfigSet) {
                String receiverConfigStr = receiver.getConfig();
                ReceiverConfig receiverConfig = JSONUtils.parseObject(receiverConfigStr, ReceiverConfig.class);
                toReceivers.add(receiverConfig);
            }

            NotificationResultRecord record = dingTalkSender.sendCardMsg(toReceivers, subject, message);
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
        InputParam webHook = InputParam.newBuilder("webhook", "webhook")
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();
        InputParam keyWord = InputParam.newBuilder("keyWord", "keyWord")
                .addValidate(Validate.newBuilder().setRequired(false).build())
                .build();
        InputParam secret = InputParam.newBuilder("secret", "secret")
                .addValidate(Validate.newBuilder().setRequired(false).build())
                .build();
        InputParam atMobiles = InputParam.newBuilder("atMobiles", "atMobiles")
                .addValidate(Validate.newBuilder().setRequired(false).build())
                .build();
        InputParam atDingtalkIds = InputParam.newBuilder("atDingtalkIds", "atDingtalkIds")
                .addValidate(Validate.newBuilder().setRequired(false).build())
                .build();
        RadioParam isAtAll = RadioParam.newBuilder("isAtAll", "isAtAll")
                .addParamsOptions(new ParamsOptions(STRING_YES, STRING_TRUE, false))
                .addParamsOptions(new ParamsOptions(STRING_NO, STRING_FALSE, false))
                .setValue(STRING_FALSE)
                .addValidate(Validate.newBuilder().setRequired(true).build())
                .build();

        paramsList.add(webHook);
        paramsList.add(secret);
        paramsList.add(keyWord);
        paramsList.add(atMobiles);
        paramsList.add(atDingtalkIds);
        paramsList.add(isAtAll);

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
