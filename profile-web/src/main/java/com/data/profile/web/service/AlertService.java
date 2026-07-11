package com.data.profile.web.service;

import com.data.notification.api.entity.NotificationMessage;
import com.data.notification.api.entity.NotificationReceiverConfig;
import com.data.notification.api.entity.NotificationResult;
import com.data.notification.api.entity.NotificationSenderConfig;
import com.data.notification.core.NotificationManager;
import com.data.profile.common.enums.AlertChannel;
import com.data.profile.common.enums.AlertCondition;
import com.data.profile.common.enums.AlertReceiverType;
import com.data.profile.common.enums.InstanceStatus;
import com.data.profile.common.enums.TriggerMode;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.common.enums.ModelType;
import com.data.profile.web.dao.AlertHistoryMapper;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.model.AlertHistory;
import com.data.profile.web.model.AlertReceiver;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.security.UserContextHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警服务：事件驱动触发告警通知
 */
@Slf4j
@Service
public class AlertService {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AlertHistoryMapper alertHistoryMapper;

    /**
     * 触发告警（由 TaskExecutionService 调用）
     * 整体 try/catch，异常仅记录日志，不向上抛
     */
    public void trigger(Task task, TaskInstance instance, InstanceStatus status) {
        try {
            doTrigger(task, instance, status);
        } catch (Exception e) {
            log.error("告警触发异常: taskId={}, instanceId={}", task.getTaskId(), instance.getInstanceId(), e);
            recordFailure(task, instance, "告警触发异常: " + e.getMessage());
        }
    }

    private void doTrigger(Task task, TaskInstance instance, InstanceStatus status) {
        // 1. 手动触发跳过
        if (instance.getTriggerMode() == TriggerMode.MANUAL.getCode()) {
            log.debug("手动触发，跳过告警: taskId={}", task.getTaskId());
            return;
        }

        // 2. 无告警配置跳过
        String alertCondition = task.getAlertCondition();
        if (StringUtils.isBlank(alertCondition)) {
            log.debug("任务未配置告警: taskId={}", task.getTaskId());
            return;
        }

        // 3. 条件不匹配跳过
        AlertCondition condition = AlertCondition.of(alertCondition);
        if (condition == null || !condition.isMatch(status)) {
            log.debug("告警条件不匹配: taskId={}, condition={}, status={}", task.getTaskId(), alertCondition, status);
            return;
        }

        // 4. 解析通道
        String alertChannels = task.getAlertChannels();
        if (StringUtils.isBlank(alertChannels)) {
            log.debug("任务未配置报警方式: taskId={}", task.getTaskId());
            return;
        }

        String[] channels = alertChannels.split(",");
        for (String channelCode : channels) {
            AlertChannel channel = AlertChannel.of(channelCode.trim());
            if (channel == null) continue;

            if (!channel.isImplemented()) {
                // 通道未实现，落跳过记录
                AlertHistory skipRecord = buildHistory(task, instance, condition, channel, "", "", "",
                        2, "通道未实现");
                alertHistoryMapper.insertSelective(skipRecord);
                continue;
            }

            // 5. 解析接收人
            List<String> emails = resolveReceivers(task);
            if (emails.isEmpty()) {
                AlertHistory skipRecord = buildHistory(task, instance, condition, channel, "", "", "",
                        2, "无有效接收人");
                alertHistoryMapper.insertSelective(skipRecord);
                continue;
            }

            // 6. 发送
            sendEmail(task, instance, condition, channel, emails);
        }
    }

    /**
     * 解析接收人，返回有效邮箱列表（去重去空）
     */
    private List<String> resolveReceivers(Task task) {
        String alertReceivers = task.getAlertReceivers();
        if (StringUtils.isBlank(alertReceivers)) {
            return Collections.emptyList();
        }

        List<AlertReceiver> receivers;
        try {
            receivers = JSONUtils.parseObject(alertReceivers, new TypeReference<List<AlertReceiver>>() {});
        } catch (Exception e) {
            log.warn("解析 alert_receivers JSON 失败: taskId={}", task.getTaskId(), e);
            return Collections.emptyList();
        }

        if (receivers == null || receivers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> emailSet = new LinkedHashSet<>();
        for (AlertReceiver r : receivers) {
            AlertReceiverType type = AlertReceiverType.of(r.getType());
            if (type == null) continue;

            switch (type) {
                case OWNER:
                    // 任务责任人
                    String ownerEmail = getUserEmail(task.getOwner());
                    if (StringUtils.isNotBlank(ownerEmail)) {
                        emailSet.add(ownerEmail);
                    }
                    break;
                case USER:
                    // 指定用户
                    if (StringUtils.isNotBlank(r.getValue())) {
                        String userEmail = getUserEmail(r.getValue());
                        if (StringUtils.isNotBlank(userEmail)) {
                            emailSet.add(userEmail);
                        }
                    }
                    break;
                default:
                    log.debug("接收人类型未实现: {}", type);
                    break;
            }
        }
        return new ArrayList<>(emailSet);
    }

    private String getUserEmail(String userId) {
        if (StringUtils.isBlank(userId)) return null;
        return userService.getDetail(userId)
                .map(UserDTO::getEmail)
                .orElse(null);
    }

    /**
     * 发送邮件告警
     */
    private void sendEmail(Task task, TaskInstance instance, AlertCondition condition,
                           AlertChannel channel, List<String> emails) {
        // 读取 SMTP 配置
        Map<String, String> smtpConfig = systemConfigService.getConfigByGroup("smtp");
        if (smtpConfig.isEmpty()) {
            log.warn("SMTP 配置为空，无法发送邮件告警: taskId={}", task.getTaskId());
            AlertHistory record = buildHistory(task, instance, condition, channel,
                    String.join(",", emails), "", "", 0, "SMTP 配置为空");
            alertHistoryMapper.insertSelective(record);
            return;
        }

        String senderConfigJson = JSONUtils.toJsonString(smtpConfig);
        String toEmails = String.join(",", emails);

        // 构建消息
        String subject = String.format("[告警] %s - %s", task.getTaskName(), condition.getDescription());
        String content = buildEmailContent(task, instance, condition, toEmails);

        NotificationSenderConfig sender = new NotificationSenderConfig();
        sender.setType("email");
        sender.setConfig(senderConfigJson);

        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put("to", toEmails);
        NotificationReceiverConfig receiver = new NotificationReceiverConfig();
        receiver.setType("email");
        receiver.setConfig(JSONUtils.toJsonString(receiverMap));

        NotificationMessage message = new NotificationMessage();
        message.setSubject(subject);
        message.setMessage(content);

        Map<NotificationSenderConfig, Set<NotificationReceiverConfig>> config = new HashMap<>();
        config.put(sender, new HashSet<>(Collections.singletonList(receiver)));

        try {
            NotificationResult result = notificationManager.notify(message, config);
            int sendStatus = (result != null && Boolean.TRUE.equals(result.getStatus())) ? 1 : 0;
            String sendMsg = result != null ? String.valueOf(result.getStatus()) : "返回结果为空";

            AlertHistory record = buildHistory(task, instance, condition, channel,
                    toEmails, subject, content, sendStatus, sendMsg);
            alertHistoryMapper.insertSelective(record);

            log.info("告警邮件发送完成: taskId={}, status={}, receivers={}", task.getTaskId(), sendStatus, toEmails);
        } catch (Exception e) {
            log.error("告警邮件发送失败: taskId={}", task.getTaskId(), e);
            AlertHistory record = buildHistory(task, instance, condition, channel,
                    toEmails, subject, content, 0, "发送异常: " + e.getMessage());
            alertHistoryMapper.insertSelective(record);
        }
    }

    private String buildEmailContent(Task task, TaskInstance instance, AlertCondition condition, String receivers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>任务告警通知</h3>");
        sb.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse'>");
        sb.append("<tr><td>任务名称</td><td>").append(task.getTaskName()).append("</td></tr>");
        sb.append("<tr><td>任务ID</td><td>").append(task.getTaskId()).append("</td></tr>");
        sb.append("<tr><td>实例ID</td><td>").append(instance.getInstanceId()).append("</td></tr>");
        sb.append("<tr><td>触发条件</td><td>").append(condition.getDescription()).append("</td></tr>");
        sb.append("<tr><td>告警时间</td><td>").append(new Date()).append("</td></tr>");
        sb.append("<tr><td>接收人</td><td>").append(receivers).append("</td></tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private AlertHistory buildHistory(Task task, TaskInstance instance, AlertCondition condition,
                                       AlertChannel channel, String receivers, String subject,
                                       String content, int sendStatus, String sendMessage) {
        String operator = UserContextHolder.currentUserId();
        return AlertHistory.builder()
                .historyId(IDGenerator.getInstance().generate(ModelType.ALERT))
                .taskId(task.getTaskId())
                .instanceId(instance.getInstanceId())
                .alertCondition(condition.getCode())
                .alertChannel(channel.getCode())
                .receivers(receivers)
                .subject(subject)
                .content(content)
                .sendStatus(sendStatus)
                .sendMessage(sendMessage)
                .creator(operator)
                .modifier(operator)
                .build();
    }

    /**
     * 触发异常时记录失败
     */
    private void recordFailure(Task task, TaskInstance instance, String message) {
        try {
            String operator = UserContextHolder.currentUserId();
            AlertHistory record = AlertHistory.builder()
                    .historyId(IDGenerator.getInstance().generate(ModelType.ALERT))
                    .taskId(task.getTaskId())
                    .instanceId(instance.getInstanceId())
                    .alertCondition("")
                    .alertChannel("email")
                    .sendStatus(0)
                    .sendMessage(message)
                    .creator(operator)
                    .modifier(operator)
                    .build();
            alertHistoryMapper.insertSelective(record);
        } catch (Exception e) {
            log.error("记录告警失败历史时异常", e);
        }
    }
}
