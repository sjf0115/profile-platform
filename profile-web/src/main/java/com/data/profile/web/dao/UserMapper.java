package com.data.profile.web.dao;

import com.data.profile.web.model.Role;
import com.data.profile.web.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    // 查询
    User selectByUserId(String UserId); // 根据ID查询
    List<User> selectByUserName(String UserName); // 根据名字查询
    List<User> selectByParams(User User); //根据参数查询（自动加载角色）
    List<User> selectByKeyword(String keyword); // 模糊查询
    List<Role> selectRolesByUserId(String userId); // 根据用户ID查询角色

    /**
     * 批量查询多用户的角色（IN 查询，解决 N+1）
     * 返回的 Role 中 userId 字段用于按用户分组
     */
    List<Role> selectRolesByUserIds(@Param("userIds") List<String> userIds);
    // 密码校验
    User checkPassword(@Param("userName") String userName, @Param("password") String password, @Param("authType") String authType);
    // 统计
    /*int countTotal(); // 统计总用户数
    int countByStatus(Integer status); // 根据状态统计*/
    // 插入
    int insert(User User); // 插入全部
    int insertSelective(User User); // 选择性插入
    // 删除
    int deleteByUserId(String UserId);
    // 更新
    int updateByUserId(User User);
    int updateByUserIdSelective(User User);
}