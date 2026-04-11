package com.data.profile.web.dao;


import com.data.profile.web.model.Dataset;

import java.util.List;

public interface DatasetMapper {
    // 查询
    Dataset selectByDatasetId(String datasetId); // 根据ID查询
    List<Dataset> selectByDatasetName(String datasetName); // 根据名字查询
    List<Dataset> selectByParams(Dataset dataset); //根据参数查询
    List<Dataset> selectByKeyword(String keyword); // 模糊查询
    // 插入
    int insert(Dataset dataset); // 插入全部
    int insertSelective(Dataset dataset); // 选择性插入
    // 删除
    int deleteByDatasetId(String datasetId); // 根据ID删除
    // 更新
    int updateByDatasetId(Dataset dataset); // 全部更新
    int updateByDatasetIdSelective(Dataset dataset); // 部分更新
}