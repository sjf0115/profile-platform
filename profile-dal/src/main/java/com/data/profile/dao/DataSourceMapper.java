package com.data.profile.dao;

import com.data.profile.model.DataSource;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataSourceMapper {
    // 根据ID查询
    DataSource selectSimpleByDatasourceId(String datasourceId);

    DataSource selectByDatasourceId(String datasourceId);

    // 根据名称查询
    List<DataSource> selectSimpleByDatasourceName(String datasourceName);

    List<DataSource> selectByDatasourceName(String datasourceName);

    // 根据参数查询
    List<DataSource> selectSimpleByParams(DataSource datasource);

    List<DataSource> selectByParams(DataSource datasource);

    // 插入
    int insert(DataSource dataSource);

    int insertSelective(DataSource dataSource);

    // 根据ID更新
    int updateByDataSourceIdSelective(DataSource dataSource);

    int updateByDataSourceId(DataSource dataSource);

    // 根据ID删除
    int deleteByDatasourceId(String datasourceId);
}