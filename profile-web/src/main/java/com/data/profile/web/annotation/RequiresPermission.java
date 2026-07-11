package com.data.profile.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能权限注解：标注方法所需的权限码
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * 权限码，如 "label:create"
     */
    String code();

    /**
     * 权限点中文名称，如 "标签-创建"，用于自动同步时展示
     */
    String name() default "";
}
