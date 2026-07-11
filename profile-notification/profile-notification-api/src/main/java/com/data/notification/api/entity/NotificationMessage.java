package com.data.notification.api.entity;

import com.data.notification.api.enums.NotificationTemplate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
public class NotificationMessage implements Serializable {

    private Long id;

    private String subject;

    private String message;

    /**
     * 通知模板类型
     */
    private NotificationTemplate template;

    /**
     * 模板参数（配合 template 使用）
     */
    private Map<String, Object> templateParams;
}
