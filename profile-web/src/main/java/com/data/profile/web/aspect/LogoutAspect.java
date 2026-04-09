package com.data.profile.web.aspect;

import com.data.profile.dao.UserMapper;
import com.data.profile.service.UserLoginService;
import com.data.profile.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.data.profile.common.domain.Constant.USER_ID;

@Slf4j
@Aspect
@Component
@Order(2)
public class LogoutAspect {

    @Resource
    private UserLoginService userLoginService;

    @Pointcut("execution(public * com.data.profile.web.controller.UserController.logout(..))")
    public void logoutPointCut() {}

    @Before("logoutPointCut()")
    public void check(JoinPoint point) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        final String userId = (String) request.getAttribute(USER_ID);
        userLoginService.disableToken(userId);
    }
}
