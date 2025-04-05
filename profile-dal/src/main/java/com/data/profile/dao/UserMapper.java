package com.data.profile.dao;

import com.data.profile.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    // 查询
    User selectByUserId(String UserId); // 根据ID查询
    List<User> selectByUserName(String UserName); // 根据名字查询
    List<User> selectByParams(User User); //根据参数查询
    List<User> selectByKeyword(String keyword); // 模糊查询
    // 插入
    int insert(User User); // 插入全部
    int insertSelective(User User); // 选择性插入
    // 删除
    int deleteByUserId(String UserId);
    // 更新
    int updateByUserId(User User);
    int updateByUserIdSelective(User User);
}