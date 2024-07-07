package com.data.profile.dao;

import com.data.profile.model.Entity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EntityMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Entity row);

    int insertSelective(Entity row);

    Entity selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Entity row);

    int updateByPrimaryKey(Entity row);
}