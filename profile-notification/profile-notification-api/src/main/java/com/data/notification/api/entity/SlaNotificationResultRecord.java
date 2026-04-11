package com.data.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
public class SlaNotificationResultRecord {
    private static final long serialVersionUID = -1L;

    private Boolean status;

    private String message;

    public SlaNotificationResultRecord(){
        this.status = false;
    }
}
