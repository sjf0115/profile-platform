package com.data.profile.dao;

import com.data.profile.model.EntityType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EntityTypeMapper {
    EntityType selectByEntityTypeId(String entityTypeId);

    List<EntityType> selectByEntityTypeName(String entityTypeName);

    List<EntityType> selectByParams(EntityType entityType);

    List<EntityType> selectByKeyword(String entityTypeName);

    int insert(EntityType entity);

    int insertSelective(EntityType entity);

    int deleteByEntityTypeId(String entityTypeId);

    int updateByEntityTypeIdSelective(EntityType row);

    int updateByEntityTypeId(EntityType entity);
}