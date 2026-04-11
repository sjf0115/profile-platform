package com.data.profile.web.service;

import com.data.profile.web.dao.RoleMapper;
import com.data.profile.web.model.Role;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：角色服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleService userRoleService;

    /**
     * 根据查询条件获取角色列表
     * @param role 角色
     */
    public List<Role> getList(Role role) {
        List<Role> roles = roleMapper.selectByParams(role);
        log.info("根据查询条件获取 {} 个角色", roles.size());
        return roles;
    }

    /**
     * 根据角色ID获取角色详细信息
     * @param roleId 角色ID
     */
    public Optional<Role> getDetail(String roleId) {
        Role role = roleMapper.selectByRoleId(roleId);
        log.info("根据角色ID获取角色详细信息: {}", JSONUtils.toJsonString(role));
        if (role == null) {
            return Optional.empty();
        }
        return Optional.of(role);
    }

    /**
     * 新增角色
     * @param role 角色
     */
    public int create(Role role) {
        if (role.getRoleType() == null) {
            throw new RuntimeException("角色类型不能为空");
        }
        if (StringUtils.isBlank(role.getRoleName())) {
            throw new RuntimeException("角色名称不能为空");
        }
        String roleId = IDGenerator.getInstance().generate(ModelType.ROLE);
        role.setRoleId(roleId);
        role.setSourceType(SourceType.CUSTOM.getCode());
        role.setCreator(RequestContext.currentUserId());
        role.setModifier(RequestContext.currentUserId());
        log.info("新增角色: {}", JSONUtils.toJsonString(role));
        return roleMapper.insertSelective(role);
    }

    /**
     * 修改角色
     * @param role 角色
     */
    public int update(Role role) {
        role.setModifier(RequestContext.currentUserId());
        log.info("更新角色: {}", JSONUtils.toJsonString(role));
        return roleMapper.updateByRoleIdSelective(role);
    }

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    @Transactional
    public int delete(String roleId) {
        if (StringUtils.isBlank(roleId)) {
            throw new RuntimeException("角色ID不能为空");
        }
        Role role = roleMapper.selectByRoleId(roleId);
        if (Objects.equals(role, null)) {
            log.error("角色 {} 不存在，无法删除", roleId);
            throw new RuntimeException("角色不存在，无法删除");
        }
        // 系统内置角色不允许删除
        if (Objects.equals(role.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置角色 {} 不允许删除", roleId);
            throw new RuntimeException("内置角色不允许删除");
        }

        // 删除角色下的用户关系
        userRoleService.deleteByRoleId(roleId);
        // 删除角色
        log.info("删除角色: {}", roleId);
        return roleMapper.deleteByRoleId(roleId);
    }
}
