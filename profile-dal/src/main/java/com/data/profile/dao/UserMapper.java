package com.data.profile.dao;

import com.data.profile.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(User row);

    int insertSelective(User row);

    User selectByUserId(String userId);

    List<User> selectByUserName(String userName);

    int updateByPrimaryKeySelective(User row);

    int updateByPrimaryKey(User row);
}