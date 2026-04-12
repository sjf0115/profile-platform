package com.data.profile.web.aspect;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        log.error(e.getMessage(), e);
        String message = e.getMessage();
        return Response.error(StringUtils.isBlank(message) ? "操作失败" : message, ResponseCode.ERROR);
    }
}
