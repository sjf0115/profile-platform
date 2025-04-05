package com.data.profile.dao;

import com.data.profile.model.Entity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EntityMapper {

    // 查询
    Entity selectSimpleByEntityId(String entityId); // 根据ID查询

    Entity selectByEntityId(String entityId); // 根据ID查询

    List<Entity> selectSimpleByEntityName(String entityName); // 根据名字查询

    List<Entity> selectByEntityName(String entityName); // 根据名字查询

    List<Entity> selectSimpleByParams(Entity entity); // 根据参数查询

    List<Entity> selectByParams(Entity entity); // 根据参数查询

    List<Entity> selectSimpleByKeyword(String keyword); // 模糊查询

    List<Entity> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Entity entity); // 插入全部

    int insertSelective(Entity entity); // 选择性插入

    // 删除
    int deleteByEntityId(String entityId); // 根据ID删除

    // 更新
    int updateByEntityIdSelective(Entity row);

    int updateByEntityId(Entity entity);
}