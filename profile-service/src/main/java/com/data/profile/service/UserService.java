package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.UserStatus;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

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
    private static Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private UserMapper userMapper;

    // 保存用户
    public int save(User user) {
        User targetUser = userMapper.selectByUserId(user.getUserId());
        if (StringUtils.isBlank(targetUser.getUserId())) {
            // 新增用户
            user.setCreator(RequestContext.currentUserId());
            user.setModifier(RequestContext.currentUserId());
            user.setSourceType(SourceType.BUILT_IN.getCode());
            user.setStatus(UserStatus.ENABLE.getCode());
            int result = userMapper.insert(user);
            return result;
        } else {
            // 修改用户
            user.setModifier(RequestContext.currentUserId());
            int result = userMapper.updateByPrimaryKey(user);
            return result;
        }
    }

    // 根据用户ID获取用户详细信息
    public Optional<User> getDetail(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}