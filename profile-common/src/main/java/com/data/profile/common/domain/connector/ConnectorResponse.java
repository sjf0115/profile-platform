package com.data.profile.common.domain.connector;

import lombok.Builder;

@Builder
public class ConnectorResponse {

    private Status status;

    private Object result;

    private String errorMsg;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public static enum Status {
        /**
         *
         */
        IN_PROGRESS(0),
        SUCCESS(1),
        ERROR(-1);

        private final int status;

        Status(int status) {
            this.status = status;
        }

        static Status getStatus(int status) {
            switch(status) {
                case -1:
                    return ERROR;
                case 0:
                    return IN_PROGRESS;
                case 1:
                    return SUCCESS;
                default:
                    assert false : "Unknown status!";
                    return ERROR;
            }
        }

        int status() {
            return this.status;
        }

        public boolean isSuccess() {
            return this.status == SUCCESS.status();
        }
    }
}
