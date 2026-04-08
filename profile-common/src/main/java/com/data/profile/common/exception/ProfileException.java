package com.data.profile.common.exception;

import com.data.profile.common.enums.ResponseCode;

public class ProfileException extends RuntimeException {
    private ResponseCode responseCode;

    public ProfileException() {
        super();
    }

    public ProfileException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public ProfileException(ResponseCode responseCode, Object... msg) {
        super(String.format(responseCode.getTemplate(), msg));
        this.responseCode = responseCode;
    }

    public ProfileException(String message) {
        super(message);
    }

    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileException(Throwable cause) {
        super(cause);
    }

    protected ProfileException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
