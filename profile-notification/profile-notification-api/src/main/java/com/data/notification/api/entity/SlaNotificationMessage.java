package com.data.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlaNotificationMessage implements Serializable {

    private Long slaId;

    private String subject;

    private String message;
}
