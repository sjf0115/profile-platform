package com.data.profile.dao;

import com.data.profile.model.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper {
    Role selectByRoleId(String roleId);

    List<Role> selectByParams(Role role);

    int insert(Role role);

    int insertSelective(Role role);

    int deleteByRoleId(String roleId);

    int updateByRoleIdSelective(Role role);

    int updateByRoleId(Role role);
}
