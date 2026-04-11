package com.data.notification.plugin.email.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class ReceiverConfig {

    private String to;

    private String cc;
}
