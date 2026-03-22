package com.data.profile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Long id;

    private Integer status;

    private String eventId;

    private String eventName;

    private String eventDesc;

    private String eventRules;

    private String datasetId;

    private Integer sourceType;

    private String owner;

    private String creator;

    private String modifier;

    private Date gmtCreate;

    private Date gmtModified;
}