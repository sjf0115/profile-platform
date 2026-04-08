package com.data.profile.dao;

import com.data.profile.model.UserLogin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserLoginMapper {
    UserLogin selectById(Long id);

    UserLogin selectByUserId(String userId);

    List<UserLogin> selectByParams(UserLogin userLogin);

    int insert(UserLogin userLogin);

    int insertSelective(UserLogin userLogin);

    int deleteById(Long id);

    int deleteByUserId(String userId);

    int updateByIdSelective(UserLogin userLogin);

    int updateById(UserLogin userLogin);

    int updateTokenStatusByUserId(String userId, Integer tokenStatus);
}
