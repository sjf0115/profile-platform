package com.data.profile.web.service;

import com.data.profile.web.dao.DatasetFieldMapper;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.security.RequestContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：数据集字段服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DatasetFieldService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private DatasetFieldMapper datasetFieldMapper;

    /**
     * 根据查询条件获取数据集字段列表
     * @param datasetField 数据集字段
     */
    public List<DatasetField> getList(DatasetField datasetField) {
        List<DatasetField> datasetFields = datasetFieldMapper.selectByParams(datasetField);
        log.info("根据查询条件获取 {} 个数据集字段: {}", datasetFields.size(), gson.toJson(datasetFields));
        return datasetFields;
    }

    /**
     * 根据数据集ID获取数据集字段列表
     * @param datasetId 数据集ID
     */
    public List<DatasetField> getListByDatasetId(String datasetId) {
        List<DatasetField> datasetFields = datasetFieldMapper.selectByDatasetId(datasetId);
        log.info("根据数据集ID {} 获取 {} 个数据集字段", datasetId, datasetFields.size());
        return datasetFields;
    }



    /**
     * 根据关联ID获取数据集字段
     * @param relatedId 关联ID
     */
    public DatasetField getDetailByRelatedId(String relatedId) {
        DatasetField datasetField = datasetFieldMapper.selectByRelatedId(relatedId);
        log.info("根据数据集关联ID {} 获取数据集字段: {}", relatedId, gson.toJson(datasetField));
        return datasetField;
    }

    /**
     * 根据ID获取数据集字段详细信息
     * @param id 数据集字段ID
     */
    public Optional<DatasetField> getDetail(Long id) {
        DatasetField datasetField = datasetFieldMapper.selectById(id);
        log.info("根据数据集字段ID获取数据集字段详细信息: {}", gson.toJson(datasetField));
        if (datasetField == null) {
            return Optional.empty();
        }
        return Optional.of(datasetField);
    }

    /**
     * 保存数据集字段 新增/修改
     * @param datasetField 数据集字段
     */
    public int save(DatasetField datasetField) throws RuntimeException {
        String datasetId = datasetField.getDatasetId();
        String fieldName = datasetField.getFieldName();
        if (datasetField.getId() == null) {
            // 新增
            DatasetField target = getListByDatasetIdAndFieldName(datasetId, fieldName);
            if (!Objects.equals(target, null)) {
                log.error("数据集字段 {} 已经存在，不允许重复添加", fieldName);
                throw new RuntimeException("数据集字段已经存在，不允许重复添加");
            }
            datasetField.setCreator(RequestContext.currentUserId());
            datasetField.setModifier(RequestContext.currentUserId());
            log.info("新增数据集字段: {}", gson.toJson(datasetField));
            return datasetFieldMapper.insertSelective(datasetField);
        } else {
            // 修改
            datasetField.setModifier(RequestContext.currentUserId());
            log.info("更新数据集字段: {}", gson.toJson(datasetField));
            return datasetFieldMapper.updateByIdSelective(datasetField);
        }
    }

    /**
     * 删除数据集字段
     * @param id 字段ID
     */
    public int deleteById(Long id) {
        DatasetField datasetField = datasetFieldMapper.selectById(id);
        if (Objects.equals(datasetField, null)) {
            log.warn("数据集字段 {} 不存在，无法删除", id);
            // throw new RuntimeException("数据集字段不存在，无法删除");
            return 1;
        }
        log.info("删除数据集字段: {}", id);
        return datasetFieldMapper.deleteById(id);
    }

    /**
     * 根据数据集ID删除所有字段
     * @param datasetId 数据集ID
     */
    public int deleteByDatasetId(String datasetId) {
        List<DatasetField> datasetFields = getListByDatasetId(datasetId);
        if (Objects.equals(datasetFields, null) || datasetFields.isEmpty()) {
            log.warn("数据集字段不存在，无法删除");
            // throw new RuntimeException("数据集字段不存在，无法删除");
            return 1;
        }
        log.info("根据数据集ID删除所有字段: {}", datasetId);
        return datasetFieldMapper.deleteByDatasetId(datasetId);
    }

    /**
     * 根据数据集ID和字段名称删除字段
     * @param datasetId 数据集ID
     */
    public int deleteByDatasetIdAndFieldName(String datasetId, String fieldName) {
        DatasetField datasetField = getListByDatasetIdAndFieldName(datasetId, fieldName);
        if (Objects.equals(datasetField, null)) {
            log.warn("数据集字段 {} 不存在，无法删除", fieldName);
            // throw new RuntimeException("数据集字段不存在，无法删除");
            return 1;
        }
        log.info("根据数据集ID {} 和字段名称 {} 删除字段", datasetId, fieldName);
        return datasetFieldMapper.deleteByDatasetId(datasetId);
    }

    /**
     * 根据数据集ID和字段名称获取数据集字段信息
     * @param datasetId 数据集ID
     * @param fieldName 字段名称
     */
    public DatasetField getListByDatasetIdAndFieldName(String datasetId, String fieldName) {
        DatasetField field = datasetFieldMapper.selectByDatasetIdAndFieldName(datasetId, fieldName);
        log.info("根据数据集ID {} 和字段名称 {} 获取数据集字段信息: {}", datasetId, fieldName, gson.toJson(field));
        return field;
    }
}