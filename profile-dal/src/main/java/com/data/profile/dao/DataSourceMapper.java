package com.data.profile.dao;

import com.data.profile.model.DataSource;
import com.data.profile.model.DataSourceSchema;
import com.data.profile.model.Dataset;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DataSourceMapper {
    //---------------------------------------------------------------------
    // 1. 数据源信息

    // 查询
    DataSource selectSimpleByDatasourceId(String datasourceId); // 根据ID查询

    List<DataSource> selectSimpleByDatasourceName(String datasourceName); // 根据名字查询

    List<DataSource> selectSimpleByParams(DataSource datasource); // 根据参数查询

    List<DataSource> selectSimpleByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(DataSource dataSource); // 插入全部

    int insertSelective(DataSource dataSource); // 选择性插入

    // 删除
    int deleteByDatasourceId(String datasourceId); // 根据ID删除

    // 更新
    int updateByDataSourceIdSelective(DataSource dataSource);

    int updateByDataSourceId(DataSource dataSource);

    //---------------------------------------------------------------------
    // 2. 关联Schema信息

    DataSource selectByDatasourceId(String datasourceId); // 根据ID查询

    List<DataSource> selectByDatasourceName(String datasourceName); // 根据名字查询

    List<DataSource> selectByParams(DataSource datasource); // 根据参数查询

    List<DataSource> selectByKeyword(String keyword); // 模糊查询
}