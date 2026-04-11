package com.data.notification.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode
@ToString
public class SlaConfigMessage implements Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    /**
     * receiver type
     */
    private String type;

    /**
     * receiver config like config email address and receiver type
     */
    private String config;

}
