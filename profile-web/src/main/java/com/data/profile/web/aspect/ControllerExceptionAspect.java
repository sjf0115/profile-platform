package com.data.profile.web.aspect;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger LOG = LoggerFactory.getLogger(ControllerExceptionAspect.class);

    @ExceptionHandler(Exception.class)
    public Response handleException(Exception e) {
        LOG.error(e.getMessage(), e);
        String message = e.getMessage();
        return Response.error(StringUtils.isBlank(message) ? message :"操作失败", ResponseCode.ERROR);
    }
}
