package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.UserMapper;
import com.data.profile.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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
     * 登录
     * @param userId
     * @param password
     * @return
     */
    public boolean login(String userId, String password) {
        User user = userMapper.selectByUserId(userId);
        if (Objects.equals(user, null)) {
            throw new RuntimeException("账号不存在, 请先注册");
        }
        // 验证密码
        if (Objects.equals(password, user.getPassword())) {
            RequestContext.setUser(user);
        } else {
            throw new RuntimeException("密码错误，请重新输入");
        }
        return true;
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
}