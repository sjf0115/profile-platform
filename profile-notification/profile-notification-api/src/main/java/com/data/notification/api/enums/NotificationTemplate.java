package com.data.notification.api.enums;

public enum NotificationTemplate {

    ALERT("alert", "告警通知"),
    USER_INVITATION("user_invitation", "用户邀请"),
    TASK_COMPLETE("task_complete", "任务完成通知"),
    CUSTOM("custom", "自定义");

    private final String templateName;
    private final String description;

    NotificationTemplate(String templateName, String description) {
        this.templateName = templateName;
        this.description = description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDescription() {
        return description;
    }
}
