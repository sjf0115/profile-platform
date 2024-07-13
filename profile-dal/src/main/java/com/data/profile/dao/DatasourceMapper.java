package com.data.profile.dao;

import com.data.profile.model.Datasource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasourceMapper {
    // 根据ID查询
    Datasource selectSimpleByDatasourceId(String datasourceId);

    Datasource selectByDatasourceId(String datasourceId);

    // 根据名称查询
    List<Datasource> selectSimpleByDatasourceName(String datasourceName);

    List<Datasource> selectByDatasourceName(String datasourceName);

    // 根据参数查询
    List<Datasource> selectSimpleByParams(Datasource datasource);

    List<Datasource> selectByParams(Datasource datasource);

    // 根据ID删除
    int deleteByDatasourceId(String datasourceId);

    // 插入
    int insert(Datasource row);

    int insertSelective(Datasource row);

    // 根据ID更新
    int updateByDataSourceIdSelective(Datasource row);

    int updateByDataSourceId(Datasource row);
}