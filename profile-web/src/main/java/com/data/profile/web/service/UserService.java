package com.data.profile.web.service;

import com.data.profile.web.config.AuthenticationProvidersConfig;
import com.data.profile.common.domain.Constant;
import com.data.profile.web.converter.UserConverter;
import com.data.profile.web.dao.UserMapper;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.dto.UserLoginDTO;
import com.data.profile.web.dto.UserOverviewDTO;
import com.data.profile.web.dto.UserRequest;
import com.data.profile.web.model.*;
import com.data.profile.web.security.IAuthenticationStrategy;
import com.data.profile.web.security.LDAPAuthenticationStrategy;
import com.data.profile.web.security.PasswdAuthenticationStrategy;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.dto.UserLoginRequest;
import com.data.profile.common.enums.*;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.utils.JwtUtil;
import com.data.profile.common.utils.UserUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.enums.ResponseCode.INVALID_AUTHENTICATION_PROVIDER;
import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

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
    private RoleService roleService;

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
     * 根据查询条件获取用户列表（含角色，批量 IN 查询避免 N+1）
     * @param user 用户查询条件
     * @return 用户 DTO 列表（含 roles）
     */
    public List<UserDTO> getList(User user) {
        List<User> users = userMapper.selectByParams(user);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查角色（2 条 SQL 代替 N+1）
        List<String> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
        Map<String, List<Role>> roleMap = getRolesByUserIds(userIds);
        // 组装 DTO
        return users.stream().map(u -> {
            UserDTO dto = UserConverter.do2dto(u);
            dto.setRoles(roleMap.getOrDefault(u.getUserId(), Collections.emptyList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 用户ID与名称映射
     */
    public Map<String, String> getUserNameMap() {
        Map<String, String> map = Maps.newHashMap();
        List<UserDTO> users = getList(new User());
        if (users == null || users.isEmpty()) {
            return map;
        }
        for (UserDTO user : users) {
            map.put(user.getUserId(), user.getUserName());
        }
        return map;
    }


    /**
     * 根据用户ID获取用户详细信息（含角色）
     * @param userId 用户ID
     * @return 用户 DTO
     */
    public Optional<UserDTO> getDetail(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            return Optional.empty();
        }
        UserDTO dto = UserConverter.do2dto(user);
        dto.setRoles(userMapper.selectRolesByUserId(userId));
        log.info("根据用户ID {} 获取用户详细信息", userId);
        return Optional.of(dto);
    }

    /**
     * 注册用户
     * @param userRequest 用户
     */
    @Transactional
    public int create(UserRequest userRequest) {
        List<User> users = userMapper.selectByUserName(userRequest.getUserName());
        if (!users.isEmpty()) {
            throw new RuntimeException("用户名已被占用");
        }
        String userId = IDGenerator.getInstance().generate(ModelType.USER);
        User target = userMapper.selectByUserId(userId);
        if (!Objects.equals(target, null)) {
            throw new RuntimeException("用户ID已经存在，不允许重复添加");
        }
        User user = UserConverter.request2do(userRequest);
        user.setUserId(userId);
        user.setUserName(UserUtil.generateRandomChineseNickname());
        user.setPassword(UserUtil.generateRandomPassword());
        user.setStatus(UserStatus.REGISTER.getCode());
        int result = userMapper.insertSelective(user);
        log.info("新增用户: {}", JSONUtils.toJsonString(user));
        // 为用户设置角色
        List<String> roles = userRequest.getRoles();
        if (roles != null && !roles.isEmpty()) {
            userRoleService.addRolesToUser(userId, roles);
        }
        return result;
    }

    /**
     * 修改用户
     * @param user 用户
     * @param roleIds 角色ID列表
     */
    public int update(User user, List<String> roleIds) {
        // 修改用户信息
        user.setModifier(UserContextHolder.currentUserId());
        int result = userMapper.updateByUserIdSelective(user);
        log.info("更新用户: {}", JSONUtils.toJsonString(user));
        // 修改用户角色
        if (roleIds != null) {
            userRoleService.setUserRoles(user.getUserId(), roleIds);
        }
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
     * 获取用户概览统计（复用 getList 批量角色查询）
     */
    public UserOverviewDTO getOverview() {
        List<UserDTO> users = getList(new User());
        int adminCount = 0, memberCount = 0;
        for (UserDTO u : users) {
            boolean isAdmin = u.getRoles().stream().anyMatch(r -> Objects.equals(r.getRoleType(), RoleType.ADMIN.getCode()));
            boolean isMember = u.getRoles().stream().anyMatch(r -> Objects.equals(r.getRoleType(), RoleType.MEMBER.getCode()));
            if (isAdmin) adminCount++;
            if (isMember) memberCount++;
        }
        return UserOverviewDTO.builder()
                .totalCount(users.size())
                .adminCount(adminCount)
                .memberCount(memberCount)
                .noPermissionCount(0)
                .build();
    }

    /**
     * 登录
     */
    public UserLoginDTO login(UserLoginRequest userLoinRequest, String authType, javax.servlet.http.HttpServletRequest request) {
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
        final String token = jwtUtil.genToken(userMap);

        // 记录登录历史
        UserLogin userLogin = UserLogin.builder()
                .userId(user.getUserId())
                .loginIp(getClientIp(request))
                .loginUa(request.getHeader("User-Agent"))
                .build();
        userLoginService.save(userLogin);

        // 查询角色
        List<Role> roles = userMapper.selectRolesByUserId(user.getUserId());

        // 构建登录聚合结果
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUser(user);
        loginDTO.setToken(token);
        loginDTO.setRoles(roles);
        log.info("用户登录成功: {}", user.getUserId());
        return loginDTO;
    }

    /**
     * 登出（记录登出事件，JWT 无状态架构下服务端不撤销 Token，由客户端清除）
     */
    public void logout() {
        String userId = UserContextHolder.currentUserId();
        log.info("用户登出: {}", userId);
    }

    /**
     * 批量查询多用户的角色并按 userId 分组（解决 N+1）
     */
    private Map<String, List<Role>> getRolesByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Role> allRoles = userMapper.selectRolesByUserIds(userIds);
        return allRoles.stream()
                .collect(Collectors.groupingBy(Role::getUserId));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(javax.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
