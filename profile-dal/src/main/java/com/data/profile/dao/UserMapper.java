package com.data.profile.dao;

import com.data.profile.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(User row);

    int insertSelective(User row);

    User selectByPrimaryKey(Long id);

    User selectByUserId(String userId);

    int updateByPrimaryKeySelective(User row);

    int updateByPrimaryKey(User row);
}