package com.data.profile.web.aspect;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.exception.PermissionDeniedException;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 功能权限切面：校验 @RequiresPermission 标注的方法
 */
@Aspect
@Component
@Slf4j
public class RequiresPermissionAspect {

    /**
     * 灰度开关：为 false 时所有权限校验直接放行
     */
    @Value("${profile.permission.enabled:false}")
    private boolean permissionEnabled;

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        if (!permissionEnabled) {
            // 灰度关闭，直接放行
            return;
        }
        String code = requiresPermission.code();
        if (!UserContextHolder.hasPermission(code)) {
            log.warn("用户 {} 无权访问 {} (需要 {})",
                    UserContextHolder.currentUserId(),
                    joinPoint.getSignature().toShortString(),
                    code);
            throw new PermissionDeniedException("无权访问：缺少权限 " + code);
        }
    }
}
