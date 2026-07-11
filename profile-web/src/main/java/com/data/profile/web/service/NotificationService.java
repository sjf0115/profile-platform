package com.data.profile.web.service;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.notification.core.NotificationManager;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 通知服务：封装通知发送与连通性测试能力
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NotificationManager notificationManager;

    /**
     * 测试 SMTP 连通性：读取 smtp 组配置，发送测试邮件
     */
    public NotificationResult testSmtp(String testReceiver) {
        Map<String, String> smtpConfig = systemConfigService.getConfigByGroup("smtp");
        if (smtpConfig.isEmpty()) {
            throw new RuntimeException("SMTP 配置未找到，请先保存邮件服务配置");
        }

        String senderConfigJson = JSONUtils.toJsonString(smtpConfig);

        // 测试收件人：如果未指定，使用 sender 自己作为测试收件人
        if (StringUtils.isBlank(testReceiver)) {
            testReceiver = smtpConfig.get("sender");
        }
        if (StringUtils.isBlank(testReceiver)) {
            throw new RuntimeException("未指定测试收件人，且 SMTP 配置中无发件人地址可作为默认收件人");
        }

        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put("to", testReceiver);
        String receiverConfigJson = JSONUtils.toJsonString(receiverMap);

        NotificationMessage message = new NotificationMessage();
        message.setSubject("邮件服务测试 - Profile Platform");
        message.setMessage("这是一封来自画像平台的测试邮件，如果您收到此邮件，说明 SMTP 配置正确。");

        NotificationSenderConfig sender = new NotificationSenderConfig();
        sender.setType("email");
        sender.setConfig(senderConfigJson);

        NotificationReceiverConfig receiver = new NotificationReceiverConfig();
        receiver.setType("email");
        receiver.setConfig(receiverConfigJson);

        Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config = new HashMap<>();
        config.put(sender, new HashSet<>(Collections.singletonList(receiver)));

        return notificationManager.notify(message, config);
    }
}
