package com.data.profile.web.dao;

import com.data.profile.web.model.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<Permission> selectAll();

    List<Permission> selectByPermissionIds(@Param("permissionIds") List<String> permissionIds);

    Permission selectByPermissionId(String permissionId);

    Permission selectByPermissionCode(String permissionCode);

    int insertSelective(Permission permission);

    int updateByPermissionIdSelective(Permission permission);

    int deleteByPermissionId(String permissionId);

    /**
     * 根据角色ID查询权限码列表（JOIN role_permission）
     */
    List<String> selectPermissionCodesByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色ID列表查询所有权限码（去重）
     */
    List<String> selectPermissionCodesByRoleIds(@Param("roleIds") List<String> roleIds);
}
