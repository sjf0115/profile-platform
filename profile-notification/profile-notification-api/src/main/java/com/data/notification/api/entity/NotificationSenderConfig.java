package com.data.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class NotificationSenderConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    private Long workspaceId;

    private String type;

    private String name;

    private String config;
}
