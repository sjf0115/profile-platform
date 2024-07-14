package com.data.profile.dao;

import com.data.profile.model.Entity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EntityMapper {

    Entity selectByEntityId(String entityId);

    List<Entity> selectByEntityName(String entityName);

    List<Entity> selectByParams(Entity entity);

    List<Entity> selectByKeyword(String entityName);

    int insert(Entity entity);

    int insertSelective(Entity entity);

    int deleteByEntityId(String entityId);

    int updateByEntityIdSelective(Entity row);

    int updateByEntityId(Entity entity);
}