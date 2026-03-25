package com.data.profile.common.exception;

public class ProfileException extends RuntimeException {

    public ProfileException() {
        super();
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
