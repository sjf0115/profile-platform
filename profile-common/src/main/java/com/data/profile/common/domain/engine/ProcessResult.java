package com.data.profile.common.domain.engine;

import com.data.profile.common.enums.engine.ExecutionStatus;
import lombok.Data;

@Data
public class ProcessResult {

    private Integer exitStatusCode;

    private String applicationId;

    private Integer processId;

    public ProcessResult(){
        this.exitStatusCode = ExecutionStatus.FAILURE.getCode();
        this.processId = -1;
        this.applicationId = "-1";
    }

    public ProcessResult(Integer exitStatusCode){
        this.exitStatusCode = exitStatusCode;
        this.processId = -1;
        this.applicationId = "-1";
    }
}
