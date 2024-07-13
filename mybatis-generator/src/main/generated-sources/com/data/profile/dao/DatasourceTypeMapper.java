package com.data.profile.dao;

import com.data.profile.model.DatasourceType;

public interface DatasourceTypeMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DatasourceType row);

    int insertSelective(DatasourceType row);

    DatasourceType selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DatasourceType row);

    int updateByPrimaryKeyWithBLOBs(DatasourceType row);

    int updateByPrimaryKey(DatasourceType row);
}