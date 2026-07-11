package com.data.profile.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限校验注解
 * <p>
 * 标注在 Controller 方法上，AOP 自动从入参中提取资源 ID 并调用 DataAuthService.check()
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataAuth {

    /**
     * 资源类型（ModelType 编码，如 "08" = 标签）
     */
    String resourceType();

    /**
     * 权限动作（1=READ, 2=WRITE, 3=EXPORT, 4=MANAGE）
     */
    int action();

    /**
     * 资源 ID 所在的方法参数名（如 "labelId"、"id"）
     */
    String idParam();
}
