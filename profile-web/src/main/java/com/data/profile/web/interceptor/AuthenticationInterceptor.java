package com.data.profile.web.interceptor;

import com.data.profile.common.enums.UserTokenStatus;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.User;
import com.data.profile.web.model.UserLogin;
import com.data.profile.web.security.AccessInfo;
import com.data.profile.web.security.UserContext;
import com.data.profile.web.service.UserLoginService;
import com.data.profile.web.service.UserService;
import com.data.profile.web.utils.JwtUtil;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.data.profile.common.domain.Constant.SESSION_USER_CONTEXT;
import static com.data.profile.common.domain.Constant.USER_ID;

/**
 * Token 认证拦截器
 * 职责：解析 JWT Token、校验有效性、构建 UserContext 写入 request attribute
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 从 Authorization 请求头提取 Token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            writeUnauthorizedResponse(response, "缺少有效的认证 Token");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 解析 JWT Token
        Map<String, Object> claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            writeUnauthorizedResponse(response, "Token 无效或已过期");
            return false;
        }

        // 获取 userId
        String userId = (String) claims.get("id");
        if (StringUtils.isEmpty(userId)) {
            writeUnauthorizedResponse(response, "Token 中缺少用户标识");
            return false;
        }

        // 校验 Token：与数据库中最新登录记录精确匹配
        Optional<UserLogin> loginOptional = userLoginService.getByUserId(userId);
        if (!loginOptional.isPresent()) {
            writeUnauthorizedResponse(response, "用户未登录或登录记录不存在");
            return false;
        }
        UserLogin userLogin = loginOptional.get();
        if (!token.equals(userLogin.getToken()) || !UserTokenStatus.ENABLE.getCode().equals(userLogin.getTokenStatus())) {
            writeUnauthorizedResponse(response, "Token 已失效，请重新登录");
            return false;
        }

        // 查询用户信息
        Optional<com.data.profile.web.vo.UserVO> userVOOptional = userService.getDetail(userId);
        if (!userVOOptional.isPresent()) {
            writeUnauthorizedResponse(response, "用户不存在");
            return false;
        }

        // 构建 User 对象（用于 RequestContext/UserContextHolder）
        User user = new User();
        user.setUserId(userVOOptional.get().getUserId());
        user.setUserName(userVOOptional.get().getUserName());
        user.setStatus(userVOOptional.get().getStatus());
        user.setUserType(userVOOptional.get().getUserType());

        // 构建 AccessInfo
        AccessInfo accessInfo = new AccessInfo();
        accessInfo.setUserName(user.getUserName());

        // 构建 UserContext 写入 request attribute（供 UserContextInterceptor 使用）
        UserContext userContext = new UserContext(user, accessInfo);
        request.setAttribute(SESSION_USER_CONTEXT, userContext);

        // 设置 USER_ID 供 LogoutAspect 使用
        request.setAttribute(USER_ID, userId);

        return true;
    }

    /**
     * 写入 401 未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        Response<?> errorResponse = Response.error(message, null);
        errorResponse.setCode(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(JSONUtils.toJsonString(errorResponse));
    }
}
