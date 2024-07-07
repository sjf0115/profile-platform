package com.data.profile.dao;

import com.data.profile.model.EntityType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EntityTypeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(EntityType row);

    int insertSelective(EntityType row);

    EntityType selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(EntityType row);

    int updateByPrimaryKey(EntityType row);
}