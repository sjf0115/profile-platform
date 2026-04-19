package com.data.engine.plugin.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobTask {
    private String jobName;
    private Long id;
    private Long versionId;
    private String pluginId;
    private String connectorType;
    private Long dataSourceId;
    private String dataSourceOption;
    private String selectTableFields;
    private String sceneMode;
    private String transformOptions;
    private String outputSchema;
    private String config;
    private String type;
    private String name;
    private Date createTime;
    private Date updateTime;
}
