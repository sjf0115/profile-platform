package com.data.profile.service;

import com.data.profile.common.enums.UserTokenStatus;
import com.data.profile.dao.UserLoginMapper;
import com.data.profile.model.UserLogin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：用户登录服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class UserLoginService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private UserLoginMapper userLoginMapper;

    /**
     * 根据查询条件获取用户登录列表
     * @param userLogin 用户登录
     */
    public List<UserLogin> getList(UserLogin userLogin) {
        List<UserLogin> userLogins = userLoginMapper.selectByParams(userLogin);
        log.info("根据查询条件获取 {} 个用户登录记录", userLogins.size());
        return userLogins;
    }

    /**
     * 根据ID获取用户登录详细信息
     * @param id ID
     */
    public Optional<UserLogin> getDetail(Long id) {
        UserLogin userLogin = userLoginMapper.selectById(id);
        log.info("根据ID获取用户登录详细信息: {}", gson.toJson(userLogin));
        if (userLogin == null) {
            return Optional.empty();
        }
        return Optional.of(userLogin);
    }

    /**
     * 根据用户ID获取用户登录信息
     * @param userId 用户ID
     */
    public Optional<UserLogin> getByUserId(String userId) {
        UserLogin userLogin = userLoginMapper.selectByUserId(userId);
        log.info("根据用户ID获取用户登录信息: {}", gson.toJson(userLogin));
        if (userLogin == null) {
            return Optional.empty();
        }
        return Optional.of(userLogin);
    }

    /**
     * 保存用户登录记录
     * @param userLogin 用户登录
     */
    public int save(UserLogin userLogin) throws RuntimeException {
        // 新增
        if (StringUtils.isBlank(userLogin.getUserId())) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (StringUtils.isBlank(userLogin.getToken())) {
            throw new RuntimeException("Token不能为空");
        }
        if (userLogin.getTokenStatus() == null) {
            userLogin.setTokenStatus(UserTokenStatus.ENABLE.getCode()); // 默认有效
        }
        log.info("新增用户登录记录: {}", gson.toJson(userLogin));
        return userLoginMapper.insertSelective(userLogin);
    }

    /**
     * 更新Token状态
     * @param userId 用户ID
     * @param tokenStatus Token状态
     */
    public int updateTokenStatus(String userId, Integer tokenStatus) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (tokenStatus == null) {
            throw new RuntimeException("Token状态不能为空");
        }
        log.info("更新用户 {} 的Token状态为: {}", userId, tokenStatus);
        return userLoginMapper.updateTokenStatusByUserId(userId, tokenStatus);
    }

    /**
     * 用户 Token 失效
     * @param userId 用户ID
     */
    public void disableToken(String userId) {
        updateTokenStatus(userId, UserTokenStatus.DISABLE.getCode());
    }

    /**
     * 删除用户登录记录
     * @param id ID
     */
    public int delete(Long id) {
        UserLogin userLogin = userLoginMapper.selectById(id);
        if (Objects.equals(userLogin, null)) {
            log.error("用户登录记录 {} 不存在，无法删除", id);
            throw new RuntimeException("用户登录记录不存在，无法删除");
        }
        log.info("删除用户登录记录: {}", id);
        return userLoginMapper.deleteById(id);
    }

    /**
     * 根据用户ID删除用户登录记录
     * @param userId 用户ID
     */
    public int deleteByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        log.info("删除用户 {} 的登录记录", userId);
        return userLoginMapper.deleteByUserId(userId);
    }
}
