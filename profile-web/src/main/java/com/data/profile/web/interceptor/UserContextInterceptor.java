package com.data.profile.web.interceptor;

import com.data.profile.web.security.UserContext;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.data.profile.common.domain.Constant.SESSION_USER_CONTEXT;

@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserContext userContext = (UserContext) request.getAttribute(SESSION_USER_CONTEXT);
        if (userContext != null) {
            log.debug("Setting user context for user: {}", userContext.getUser().getUserName());
            UserContextHolder.setUserContext(userContext);
        } else {
            log.warn("No user context found in request attributes");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.debug("Clearing user context");
        UserContextHolder.clear();
    }
}
