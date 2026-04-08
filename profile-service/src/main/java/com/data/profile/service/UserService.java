package com.data.profile.service;

import com.clickhouse.client.internal.com.google.common.collect.Maps;
import com.data.profile.common.config.AuthenticationProvidersConfig;
import com.data.profile.common.domain.Constant;
import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.common.domain.security.IAuthenticationStrategy;
import com.data.profile.common.domain.security.LDAPAuthenticationStrategy;
import com.data.profile.common.domain.security.PasswdAuthenticationStrategy;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.enums.UserTokenStatus;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.JwtUtil;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import com.data.profile.model.UserLogin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

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
     * @param user
     * @return
     */
    public List<User> getList(User user) {
        List<User> users = userMapper.selectByParams(user);
        return users;
    }

    /**
     * 保存用户
     * @param user
     * @return
     */
    public int save(User user) {
        if (StringUtils.isBlank(user.getUserId())) {
            return registerUser(user);
        } else {
            return updateUser(user);
        }
    }

    /**
     * 根据用户ID获取用户详细信息
     * @param userId
     * @return
     */
    public Optional<User> getDetail(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * 删除用户
     * @param userId
     * @return
     */
    public int delete(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (Objects.equals(user, null)) {
            throw new RuntimeException("账号不存在, 无法删除");
        }
        if(Objects.equals(user.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("系统账号不能删除");
        }

        int result = userMapper.deleteByUserId(userId);
        return result;
    }


    /**
     * 注册用户
     * @param user
     * @return
     */
    private int registerUser(User user) {
        List<User> users = userMapper.selectByUserName(user.getUserName());
        if (users.size() > 0) {
            throw new RuntimeException("用户名已被占用");
        }
        String userId = IDGenerator.getInstance().generate(ModelType.USER);
        User target = userMapper.selectByUserId(userId);
        if (!Objects.equals(target, null)) {
            throw new RuntimeException("用户ID已经存在，不允许重复添加");
        }
        user.setUserId(userId);
        user.setCreator(RequestContext.currentUserId());
        user.setModifier(RequestContext.currentUserId());
        user.setSourceType(SourceType.CUSTOM.getCode());
        user.setStatus(Status.ENABLE.getCode());
        int result = userMapper.insertSelective(user);
        return result;
    }

    /**
     * 修改用户
     * @param user
     * @return
     */
    private int updateUser(User user) {
        // 修改用户
        user.setModifier(RequestContext.currentUserId());
        int result = userMapper.updateByUserIdSelective(user);
        return result;
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

        // TODO
        Map<String, Object> map = Maps.newConcurrentMap();
        final String token = jwtUtil.genToken(map);

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