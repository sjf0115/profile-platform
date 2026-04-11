package com.data.profile.web.dao;

import com.data.profile.web.model.DatasetField;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DatasetFieldMapper {

    // 查询
    List<DatasetField> selectByDatasetId(String datasetId); // 根据数据集ID查询

    DatasetField selectById(Long id); // 根据数据集ID查询

    List<DatasetField> selectByParams(DatasetField datasetField); // 根据参数查询

    DatasetField selectByDatasetIdAndFieldName(String datasetId, String fieldName); // 根据数据集ID和字段名查询

    DatasetField selectByRelatedId(String relatedId); // 根据数据集ID和字段名查询

    // 插入
    int insert(DatasetField datasetField); // 插入全部

    int insertSelective(DatasetField datasetField); // 选择性插入

    // 删除
    int deleteByDatasetId(String datasetId); // 根据数据集ID删除

    int deleteById(Long id); // 根据数据集字段ID删除

    int deleteByDatasetIdAndFieldName(String datasetId, String fieldName); // 根据数据集ID和字段名称删除

    // 更新
    int updateByIdSelective(DatasetField datasetField);

    int updateById(DatasetField datasetField);
}
