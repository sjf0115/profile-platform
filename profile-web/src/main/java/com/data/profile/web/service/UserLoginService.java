package com.data.profile.web.service;

import com.data.profile.web.dao.UserLoginMapper;
import com.data.profile.web.model.UserLogin;
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
 * 功能：用户登录历史服务
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
     * 根据查询条件获取用户登录历史列表
     * @param userLogin 查询条件
     */
    public List<UserLogin> getList(UserLogin userLogin) {
        List<UserLogin> userLogins = userLoginMapper.selectByParams(userLogin);
        log.info("根据查询条件获取 {} 条登录历史", userLogins.size());
        return userLogins;
    }

    /**
     * 根据ID获取登录历史详细信息
     * @param id ID
     */
    public Optional<UserLogin> getDetail(Long id) {
        UserLogin userLogin = userLoginMapper.selectById(id);
        if (userLogin == null) {
            return Optional.empty();
        }
        return Optional.of(userLogin);
    }

    /**
     * 记录用户登录事件
     * @param userLogin 登录记录
     */
    public int save(UserLogin userLogin) throws RuntimeException {
        if (StringUtils.isBlank(userLogin.getUserId())) {
            throw new RuntimeException("用户ID不能为空");
        }
        log.info("记录用户登录事件: userId={}", userLogin.getUserId());
        return userLoginMapper.insertSelective(userLogin);
    }

    /**
     * 删除登录历史记录
     * @param id ID
     */
    public int delete(Long id) {
        UserLogin userLogin = userLoginMapper.selectById(id);
        if (Objects.equals(userLogin, null)) {
            log.error("登录记录 {} 不存在，无法删除", id);
            throw new RuntimeException("登录记录不存在，无法删除");
        }
        log.info("删除登录记录: {}", id);
        return userLoginMapper.deleteById(id);
    }

    /**
     * 根据用户ID删除所有登录历史记录
     * @param userId 用户ID
     */
    public int deleteByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        log.info("删除用户 {} 的所有登录记录", userId);
        return userLoginMapper.deleteByUserId(userId);
    }
}
