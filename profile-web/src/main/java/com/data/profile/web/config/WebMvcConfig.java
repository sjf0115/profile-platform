package com.data.profile.web.config;

import com.data.profile.web.interceptor.AuthenticationInterceptor;
import com.data.profile.web.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器链：AuthenticationInterceptor -> UserContextInterceptor
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. Token 认证拦截器（先执行）：解析 Token、校验有效性、构建 UserContext
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",      // 登录接口无需认证
                        "/error",           // Spring Boot 默认错误页面
                        "/favicon.ico"      // 图标
                )
                .order(1);

        // 2. 用户上下文拦截器（后执行）：将 request attribute 中的 UserContext 设置到 ThreadLocal
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/error",
                        "/favicon.ico"
                )
                .order(2);
    }
}
