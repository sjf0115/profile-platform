package com.data.profile.web.dao;

import com.data.profile.web.model.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    UserRole selectById(Integer id);

    List<UserRole> selectByUserId(String userId);

    List<UserRole> selectByRoleId(String roleId);

    UserRole selectByUserIdAndRoleId(String userId, String roleId);

    List<UserRole> selectByParams(UserRole userRole);

    int insert(UserRole userRole);

    int insertSelective(UserRole userRole);

    int deleteById(Integer id);

    int deleteByUserId(String userId);

    int deleteByRoleId(String roleId);

    int deleteByUserIdAndRoleId(String userId, String roleId);

    int updateByIdSelective(UserRole userRole);

    int updateById(UserRole userRole);
}
