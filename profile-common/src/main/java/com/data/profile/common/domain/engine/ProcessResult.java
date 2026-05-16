package com.data.profile.common.domain.engine;

import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.Data;

@Data
public class ProcessResult {

    private Integer exitStatusCode;

    private String applicationId;

    private Integer processId;

    private boolean success;

    private String errorMsg;

    private long recordCount;

    private long duration;

    public ProcessResult(){
        this.exitStatusCode = ExecutionStatus.FAILURE.getCode();
        this.processId = -1;
        this.applicationId = "-1";
        this.success = false;
        this.recordCount = 0;
        this.duration = 0;
    }

    public ProcessResult(Integer exitStatusCode){
        this.exitStatusCode = exitStatusCode;
        this.processId = -1;
        this.applicationId = "-1";
        this.success = false;
        this.recordCount = 0;
        this.duration = 0;
    }
}
