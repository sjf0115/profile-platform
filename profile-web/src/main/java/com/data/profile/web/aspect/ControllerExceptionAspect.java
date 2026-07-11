package com.data.profile.web.aspect;

import com.data.profile.web.exception.PermissionDeniedException;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 功能：Controller 异常 Aspect
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/13 14:47
 */
@RestControllerAdvice
@Slf4j
public class ControllerExceptionAspect {

    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response handlePermissionDenied(PermissionDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        Response response = Response.error(StringUtils.isBlank(e.getMessage()) ? "无权访问" : e.getMessage(), ResponseCode.ERROR);
        response.setCode(HttpStatus.FORBIDDEN.value());
        return response;
    }

    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error(e.getMessage(), e);
        String message = e.getMessage();
        return Response.error(StringUtils.isBlank(message) ? "操作失败" : message, ResponseCode.ERROR);
    }
}
