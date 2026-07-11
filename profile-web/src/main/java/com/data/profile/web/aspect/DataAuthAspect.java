package com.data.profile.web.aspect;

import com.data.profile.web.annotation.DataAuth;
import com.data.profile.web.service.DataAuthService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据权限校验切面
 */
@Slf4j
@Aspect
@Component
public class DataAuthAspect {

    @Autowired
    private DataAuthService dataAuthService;

    @Before("@annotation(dataAuth)")
    public void checkDataPermission(JoinPoint joinPoint, DataAuth dataAuth) {
        String resourceId = extractResourceId(joinPoint, dataAuth.idParam());
        if (resourceId == null || resourceId.isEmpty()) {
            log.warn("DataAuth: 无法从参数 {} 中提取资源ID，跳过校验", dataAuth.idParam());
            return;
        }
        dataAuthService.check(dataAuth.resourceType(), resourceId, dataAuth.action());
    }

    /**
     * 从方法参数中提取资源ID值
     */
    private String extractResourceId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null) return null;

        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName) && args[i] != null) {
                return String.valueOf(args[i]);
            }
        }

        // 尝试从 @RequestBody 对象的字段中提取
        for (Object arg : args) {
            if (arg == null) continue;
            try {
                java.lang.reflect.Field field = arg.getClass().getDeclaredField(paramName);
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value != null) return String.valueOf(value);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // 字段不存在，继续尝试下一个参数
            }
        }
        return null;
    }
}
