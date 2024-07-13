package com.data.profile.dao;

import com.data.profile.model.Datasource;

public interface DatasourceMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Datasource row);

    int insertSelective(Datasource row);

    Datasource selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Datasource row);

    int updateByPrimaryKeyWithBLOBs(Datasource row);

    int updateByPrimaryKey(Datasource row);
}