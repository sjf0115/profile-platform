package com.data.profile.service;

import com.data.profile.common.utils.JSONUtils;
import com.data.profile.dao.UserRoleMapper;
import com.data.profile.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 功能：用户角色服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class UserRoleService {
    @Resource
    private UserRoleMapper userRoleMapper;

    /**
     * 根据查询条件获取用户角色列表
     * @param userRole 用户角色
     */
    public List<UserRole> getList(UserRole userRole) {
        List<UserRole> userRoles = userRoleMapper.selectByParams(userRole);
        log.info("根据查询条件获取 {} 个用户角色关系", userRoles.size());
        return userRoles;
    }

    /**
     * 根据ID获取用户角色详细信息
     * @param id ID
     */
    public Optional<UserRole> getDetail(Integer id) {
        UserRole userRole = userRoleMapper.selectById(id);
        log.info("根据ID获取用户角色详细信息: {}", JSONUtils.toJsonString(userRole));
        if (userRole == null) {
            return Optional.empty();
        }
        return Optional.of(userRole);
    }

    /**
     * 根据用户ID获取角色列表
     * @param userId 用户ID
     */
    public List<UserRole> getRolesByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        log.info("根据用户ID {} 获取 {} 个角色", userId, userRoles.size());
        return userRoles;
    }

    /**
     * 根据角色ID获取用户列表
     * @param roleId 角色ID
     */
    public List<UserRole> getUsersByRoleId(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new RuntimeException("角色ID不能为空");
        }
        List<UserRole> userRoles = userRoleMapper.selectByRoleId(roleId);
        log.info("根据角色ID {} 获取 {} 个用户", roleId, userRoles.size());
        return userRoles;
    }

    /**
     * 为用户添加角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public int addRoleToUser(String userId, String roleId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (StringUtils.isBlank(roleId)) {
            throw new RuntimeException("角色ID不能为空");
        }
        // 检查是否已存在
        UserRole exist = userRoleMapper.selectByUserIdAndRoleId(userId, roleId);
        if (exist != null) {
            log.warn("用户 {} 已拥有角色 {}，无需重复添加", userId, roleId);
            return 0;
        }
        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(roleId)
                .build();
        log.info("为用户 {} 添加角色 {}", userId, roleId);
        return userRoleMapper.insertSelective(userRole);
    }

    /**
     * 批量为用户添加角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    public int addRolesToUser(String userId, List<String> roleIds) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("角色ID列表不能为空");
        }
        int count = 0;
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId)) {
                count += addRoleToUser(userId, roleId);
            }
        }
        log.info("为用户 {} 批量添加 {} 个角色", userId, count);
        return count;
    }

    /**
     * 移除用户的角色
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public int removeRoleFromUser(String userId, String roleId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (StringUtils.isBlank(roleId)) {
            throw new RuntimeException("角色ID不能为空");
        }
        log.info("移除用户 {} 的角色 {}", userId, roleId);
        return userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
    }

    /**
     * 批量移除用户的角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    public int removeRolesFromUser(String userId, List<String> roleIds) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new RuntimeException("角色ID列表不能为空");
        }
        int count = 0;
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId)) {
                count += removeRoleFromUser(userId, roleId);
            }
        }
        log.info("移除用户 {} 的 {} 个角色", userId, count);
        return count;
    }

    /**
     * 修改用户角色（先删除旧角色，再添加新角色）
     * @param userId 用户ID
     * @param oldRoleId 旧角色ID
     * @param newRoleId 新角色ID
     */
    public int updateUserRole(String userId, String oldRoleId, String newRoleId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (StringUtils.isBlank(oldRoleId)) {
            throw new RuntimeException("旧角色ID不能为空");
        }
        if (StringUtils.isBlank(newRoleId)) {
            throw new RuntimeException("新角色ID不能为空");
        }
        // 删除旧角色
        int deleteCount = userRoleMapper.deleteByUserIdAndRoleId(userId, oldRoleId);
        // 添加新角色
        int addCount = addRoleToUser(userId, newRoleId);
        log.info("修改用户 {} 的角色从 {} 到 {}", userId, oldRoleId, newRoleId);
        return deleteCount + addCount;
    }

    /**
     * 设置用户角色（清空旧角色，设置新角色）
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    public int setUserRoles(String userId, List<String> roleIds) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        // 先删除该用户的所有角色
        int deleteCount = userRoleMapper.deleteByUserId(userId);
        log.info("清空用户 {} 的 {} 个旧角色", userId, deleteCount);
        // 添加新角色
        int addCount = 0;
        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                if (StringUtils.isNotBlank(roleId)) {
                    addCount += addRoleToUser(userId, roleId);
                }
            }
        }
        log.info("设置用户 {} 的 {} 个新角色", userId, addCount);
        return addCount;
    }

    /**
     * 删除用户的所有角色
     * @param userId 用户ID
     */
    public int deleteByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }
        log.info("删除用户 {} 的所有角色", userId);
        return userRoleMapper.deleteByUserId(userId);
    }

    /**
     * 删除角色的所有用户
     * @param roleId 角色ID
     */
    public int deleteByRoleId(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new RuntimeException("角色ID不能为空");
        }
        log.info("删除角色 {} 的所有用户", roleId);
        return userRoleMapper.deleteByRoleId(roleId);
    }
}
