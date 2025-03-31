package com.data.profile.dao;

import com.data.profile.model.DataSourceSchema;

import java.util.List;

public interface DataSourceSchemaMapper {

    // 查询
    DataSourceSchema selectByDataSourceSchemaId(String schemaId); // 根据ID查询

    List<DataSourceSchema> selectByDataSourceSchemaName(String schemaName); // 根据名字查询

    List<DataSourceSchema> selectByParams(DataSourceSchema dataSourceType); //根据参数查询

    // 插入
    int insert(DataSourceSchema schema); // 插入全部

    int insertSelective(DataSourceSchema schema); // 选择性插入

    // 删除
    int deleteByDataSourceSchemaId(String schemaId);

    // 更新
    int updateByDataSourceSchemaId(DataSourceSchema schema);

    int updateByDataSourceSchemaIdSelective(DataSourceSchema schema);
}