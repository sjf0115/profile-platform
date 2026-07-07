package com.data.profile.web.dao;

import com.data.profile.web.model.UserLogin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserLoginMapper {
    UserLogin selectById(Long id);

    List<UserLogin> selectByParams(UserLogin userLogin);

    int insert(UserLogin userLogin);

    int insertSelective(UserLogin userLogin);

    int deleteById(Long id);

    int deleteByUserId(String userId);
}
