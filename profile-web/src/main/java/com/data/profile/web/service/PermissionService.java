package com.data.profile.web.service;

import com.data.profile.web.dao.PermissionMapper;
import com.data.profile.web.dao.RolePermissionMapper;
import com.data.profile.web.model.Permission;
import com.data.profile.web.model.RolePermission;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：权限点服务
 */
@Slf4j
@Service
public class PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 获取全部权限点（菜单树）
     */
    public List<Permission> getPermissionTree() {
        List<Permission> all = permissionMapper.selectAll();
        return buildTree(all, "0");
    }

    /**
     * 获取角色已勾选的 permissionId 列表
     */
    public List<String> getPermissionsByRoleId(String roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    /**
     * 保存角色权限配置（事务：先删后插）
     */
    @Transactional
    public int saveRolePermissions(String roleId, List<String> permissionIds) {
        // 先清理旧关联
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            log.info("角色 {} 权限已清空", roleId);
            return 0;
        }
        // 批量插入新关联
        String creator = UserContextHolder.currentUserId();
        List<RolePermission> list = permissionIds.stream().map(pid -> RolePermission.builder()
                .roleId(roleId)
                .permissionId(pid)
                .creator(creator)
                .build()).collect(Collectors.toList());
        int count = rolePermissionMapper.batchInsert(list);
        log.info("角色 {} 保存 {} 条权限关联", roleId, count);
        return count;
    }

    /**
     * 根据角色ID列表查询权限码集合（去重）
     */
    public Set<String> getPermissionCodesByRoleIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<String> codes = permissionMapper.selectPermissionCodesByRoleIds(roleIds);
        return new HashSet<>(codes);
    }

    /**
     * 构建菜单树（parent_id 递归）
     */
    private List<Permission> buildTree(List<Permission> all, String parentId) {
        return all.stream()
                .filter(p -> Objects.equals(p.getParentId(), parentId))
                .peek(p -> {
                    // 不在 Permission model 上加 children 字段，前端根据 parent_id 自行构建树
                })
                .sorted(Comparator.comparingInt(p -> p.getSort() == null ? 0 : p.getSort()))
                .collect(Collectors.toList());
    }
}
