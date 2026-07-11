package com.data.profile.web.interceptor;

import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.model.Role;
import com.data.profile.web.model.User;
import com.data.profile.web.security.AccessInfo;
import com.data.profile.web.security.UserContext;
import com.data.profile.web.service.UserService;
import com.data.profile.web.utils.JwtUtil;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.enums.RoleType;
import com.data.profile.common.enums.UserStatus;
import com.data.profile.web.vo.Response;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.SESSION_USER_CONTEXT;

/**
 * Token 认证拦截器
 * 职责：解析 JWT Token、校验有效性、构建 UserContext 写入 request attribute
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Caffeine 本地缓存：userId -> AccessInfo
     * TTL 60s，最大 500 用户，避免每请求查库
     */
    private final Cache<String, AccessInfo> accessInfoCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private com.data.profile.web.service.PermissionService permissionService;

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

        // 查询用户信息（优先走缓存）
        AccessInfo cachedAccess = accessInfoCache.getIfPresent(userId);
        User user;
        AccessInfo accessInfo;

        if (cachedAccess != null) {
            // 缓存命中：直接用缓存的 AccessInfo，重建 User 基本信息
            accessInfo = cachedAccess;
            user = new User();
            user.setUserId(userId);
            user.setUserName(accessInfo.getUserName());
        } else {
            // 缓存未命中
            Optional<UserDTO> userOpt = userService.getDetail(userId);
            if (!userOpt.isPresent()) {
                writeUnauthorizedResponse(response, "用户不存在");
                return false;
            }
            UserDTO userDTO = userOpt.get();
            if (!Objects.equals(userDTO.getStatus(), UserStatus.ACTIVATED.getCode())) {
                writeUnauthorizedResponse(response, "用户已被禁用");
                return false;
            }

            // 构建 AccessInfo 并缓存
            accessInfo = new AccessInfo();
            accessInfo.setUserName(userDTO.getUserName());
            List<Role> roles = userDTO.getRoles();
            if (roles != null && !roles.isEmpty()) {
                Set<String> roleIds = Sets.newHashSet();
                List<String> roleList = Lists.newArrayList();
                boolean isAdmin = false;
                for (Role role : roles) {
                    String roleId = role.getRoleId();
                    Integer roleType = role.getRoleType();
                    // TODO 保留一个
                    roleIds.add(roleId);
                    roleList.add(roleId);
                    isAdmin = Objects.equals(RoleType.ADMIN.getCode(), roleType);
                }
                accessInfo.setUserRoles(roleIds);
                accessInfo.setSuperAdmin(isAdmin);
                // 填充权限码集合
                Set<String> permissions = permissionService.getPermissionCodesByRoleIds(roleList);
                accessInfo.setPermissions(permissions);
            }
            accessInfoCache.put(userId, accessInfo);

            // 不使用 UserConverter.dto2do（它会调 UserContextHolder，此时 context 尚未设置）
            user = new User();
            user.setUserId(userDTO.getUserId());
            user.setUserName(userDTO.getUserName());
            user.setEmail(userDTO.getEmail());
            user.setStatus(userDTO.getStatus());
            user.setSourceType(userDTO.getSourceType());
        }

        // 构建 UserContext 写入 request attribute（供 UserContextInterceptor 使用）
        UserContext userContext = new UserContext(user, accessInfo);
        request.setAttribute(SESSION_USER_CONTEXT, userContext);

        return true;
    }

    /**
     * 写入 401 未授权响应
     */
    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        Response<?> errorResponse = Response.error(message, ResponseCode.ERROR);
        errorResponse.setCode(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(JSONUtils.toJsonString(errorResponse));
    }
}
