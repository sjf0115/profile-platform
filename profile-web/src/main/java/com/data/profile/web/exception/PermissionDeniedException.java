package com.data.profile.web.exception;

/**
 * 权限不足异常（403）
 */
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
