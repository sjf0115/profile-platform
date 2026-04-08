package com.data.profile.common.domain.engine;

import com.data.profile.common.enums.engine.TimeoutStrategy;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class JobExecutionRequest implements Serializable {

    private long id;

    private long jobExecutionId;

    private String jobExecutionUniqueId;

    private String jobExecutionName;

    private String executePlatformType;

    private String executePlatformParameter;

    private String engineType;

    private String engineParameter;

    private String errorDataStorageType;

    private String errorDataStorageParameter;

    private String validateResultDataStorageType;

    private String validateResultDataStorageParameter;

    private String notificationParameters;

    private String applicationParameter;

    private boolean isEn;

    private String tenantCode;

    private Integer retryTimes;

    private Integer retryInterval;

    private Integer timeout;

    private TimeoutStrategy timeoutStrategy;

    private String executeHost;

    private Integer status;

    private String applicationId;

    private int processId;

    private String executeFilePath;

    private String logPath;

    private String env;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
