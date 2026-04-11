package com.data.profile.service;

import com.clickhouse.client.internal.com.google.common.collect.Maps;
import com.data.profile.common.config.AuthenticationProvidersConfig;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.common.domain.security.IAuthenticationStrategy;
import com.data.profile.common.domain.security.LDAPAuthenticationStrategy;
import com.data.profile.common.domain.security.PasswdAuthenticationStrategy;
import com.data.profile.common.enums.*;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.common.utils.JwtUtil;
import com.data.profile.common.utils.UserUtil;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import com.data.profile.model.UserLogin;
import com.data.profile.model.UserOverview;
import com.data.profile.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.enums.ResponseCode.INVALID_AUTHENTICATION_PROVIDER;

/**
 * 功能：用户服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */
@Slf4j
@Service
public class UserService {
    private final Map<String, IAuthenticationStrategy> strategies = new HashMap<>();
    @Autowired
    private PasswdAuthenticationStrategy passwdAuthenticationStrategy;
    @Autowired
    private AuthenticationProvidersConfig authenticationProvidersConfig;
    @Autowired
    private LDAPAuthenticationStrategy ldapAuthenticationStrategy;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserLoginService userLoginService;
    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        List<String> providers = authenticationProvidersConfig.getProviders();
        if (providers.isEmpty() || providers.contains(Constant.AUTHENTICATION_PROVIDER_PASSWORD)) {
            strategies.put(Constant.AUTHENTICATION_PROVIDER_PASSWORD, passwdAuthenticationStrategy);
        }
        if (providers.contains(Constant.AUTHENTICATION_PROVIDER_LDAP)) {
            strategies.put(Constant.AUTHENTICATION_PROVIDER_LDAP, ldapAuthenticationStrategy);
        }
    }

    /**
     * 根据查询条件获取用户列表
     * @param user 用户信息
     */
    public List<User> getList(User user) {
        List<User> users = userMapper.selectByParams(user);
        log.info("根据查询条件获取 {} 个角色", users.size());
        return users;
    }

    /**
     * 根据用户ID获取用户详细信息
     * @param userId 用户ID
     */
    public Optional<User> getDetail(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            return Optional.empty();
        }
        List<String> roles = userRoleService.getRolesByUserId(userId).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        user.setRoles(roles);
        log.info("根据角色ID {} 获取角色详细信息: {}", userId, JSONUtils.toJsonString(user));
        return Optional.of(user);
    }

    /**
     * 注册用户
     * @param user 用户
     */
    @Transactional
    public int create(User user) {
        List<User> users = userMapper.selectByUserName(user.getUserName());
        if (!users.isEmpty()) {
            throw new RuntimeException("用户名已被占用");
        }
        String userId = IDGenerator.getInstance().generate(ModelType.USER);
        User target = userMapper.selectByUserId(userId);
        if (!Objects.equals(target, null)) {
            throw new RuntimeException("用户ID已经存在，不允许重复添加");
        }
        user.setUserId(userId);
        user.setUserName(UserUtil.generateRandomChineseNickname());
        user.setPassword(UserUtil.generateRandomPassword());
        user.setCreator(RequestContext.currentUserId());
        user.setModifier(RequestContext.currentUserId());
        user.setSourceType(SourceType.CUSTOM.getCode());
        user.setStatus(UserStatus.REGISTER.getCode());
        int result = userMapper.insertSelective(user);
        log.info("新增用户: {}", JSONUtils.toJsonString(user));
        // 为用户设置角色
        userRoleService.addRolesToUser(userId, user.getRoles());
        return result;
    }

    /**
     * 修改用户
     * @param user 用户
     */
    public int update(User user) {
        // 修改用户信息
        user.setModifier(RequestContext.currentUserId());
        int result = userMapper.updateByUserIdSelective(user);
        log.info("更新用户: {}", JSONUtils.toJsonString(user));
        // 修改用户角色
        userRoleService.setUserRoles(user.getUserId(), user.getRoles());
        return result;
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    public int delete(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (Objects.equals(user, null)) {
            log.error("用户 {} 不存在，无法删除", userId);
            throw new RuntimeException("用户不存在, 无法删除");
        }
        if(Objects.equals(user.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置用户 {} 不允许删除", userId);
            throw new RuntimeException("内置用户不能删除");
        }
        int result = userMapper.deleteByUserId(userId);
        log.info("删除用户: {}", userId);
        // 删除用户角色信息
        userRoleService.deleteByUserId(userId);
        return result;
    }

    /**
     * 获取用户概览统计
     */
    public UserOverview getOverview() {
        int totalCount = userMapper.countTotal();
        // 已加入用户（status=1）
        int joinedCount = userMapper.countByStatus(1);
        // 管理员数量（需要根据实际业务逻辑统计，这里简化处理）
        int adminCount = 0;
        // 成员数量
        int memberCount = joinedCount - adminCount;
        // 无权限用户
        int noPermissionCount = totalCount - joinedCount;

        return UserOverview.builder()
                .totalCount(totalCount)
                .adminCount(adminCount)
                .memberCount(memberCount)
                .noPermissionCount(noPermissionCount)
                .build();
    }

    /**
     * 登录
     */
    public User login(UserLoginRequest userLoinRequest, String authType) {
        // 登录验证
        authType = StringUtils.isEmpty(authType) ? Constant.AUTHENTICATION_PROVIDER_PASSWORD : authType;
        if (!strategies.containsKey(authType)) {
            throw new ProfileException(INVALID_AUTHENTICATION_PROVIDER, authType);
        }
        IAuthenticationStrategy strategy = strategies.get(authType);
        User user = strategy.authenticate(userLoinRequest);

        Map<String, Object> userMap = Maps.newConcurrentMap();
        userMap.put("id", user.getUserId());
        userMap.put("name", user.getUserName());
        userMap.put("status", user.getStatus());
        userMap.put("type", user.getUserType());
        final String token = jwtUtil.genToken(userMap);

        // 保存登录记录
        UserLogin userLogin = UserLogin.builder()
                .token(token)
                .tokenStatus(UserTokenStatus.ENABLE.getCode())
                .userId(user.getUserId())
                .build();
        userLoginService.save(userLogin);
        return user;
    }
}
