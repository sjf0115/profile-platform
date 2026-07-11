package com.data.profile.web.dao;

import com.data.profile.web.model.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper {

    List<RolePermission> selectByRoleId(@Param("roleId") String roleId);

    List<String> selectPermissionIdsByRoleId(@Param("roleId") String roleId);

    int deleteByRoleId(@Param("roleId") String roleId);

    int batchInsert(@Param("list") List<RolePermission> list);
}
