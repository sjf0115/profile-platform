package com.data.profile.dao;

import com.data.profile.model.EntityIdentifier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EntityIdentifierMapper {

    // 查询
    EntityIdentifier selectSimpleByEntityIdentifierId(String entityIdentifierId); // 根据ID查询

    EntityIdentifier selectByEntityIdentifierId(String entityIdentifierId); // 根据ID查询

    List<EntityIdentifier> selectSimpleByEntityIdentifierName(String entityIdentifierName); // 根据名字查询

    List<EntityIdentifier> selectByEntityIdentifierName(String entityIdentifierName); // 根据名字查询

    List<EntityIdentifier> selectSimpleByParams(EntityIdentifier entityIdentifier); // 根据参数查询

    List<EntityIdentifier> selectByParams(EntityIdentifier entityIdentifier); // 根据参数查询

    List<EntityIdentifier> selectSimpleByKeyword(String keyword); // 模糊查询

    List<EntityIdentifier> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(EntityIdentifier entityIdentifier); // 插入全部

    int insertSelective(EntityIdentifier entityIdentifier); // 选择性插入

    // 删除
    int deleteByEntityIdentifierId(String entityIdentifierId); // 根据ID删除

    // 更新
    int updateByEntityIdentifierIdSelective(EntityIdentifier row);

    int updateByEntityIdentifierId(EntityIdentifier entityIdentifier);
}