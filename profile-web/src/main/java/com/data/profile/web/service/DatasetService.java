package com.data.profile.web.service;

import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 功能：数据集服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */
@Slf4j
@Service
public class DatasetService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private DatasetMapper datasetMapper;
    @Resource
    private DataSourceService dataSourceService;
    @Resource
    private DataSourceSchemaService schemaService;
    @Resource
    private DatasetFieldService datasetFieldService;

    /**
     * 根据查询条件获取数据集列表
     * @param dataset 数据集
     */
    public List<Dataset> getList(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByParams(dataset);
        // 暂时没有查询数据集字段
        log.info("根据查询条件获取 {} 个数据集: {}", datasets.size(), gson.toJson(datasets));
        return datasets;
    }

    /**
     * 根据数据集ID获取数据集详细信息
     * @param datasetId 数据集ID
     */
    public Optional<Dataset> getDetail(String datasetId) {
        // 获取数据集信息
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        log.info("根据数据集ID {} 获取数据集详细信息: {}", datasetId, gson.toJson(dataset));
        if (dataset == null) {
            return Optional.empty();
        }
        // 获取数据集字段
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);
        dataset.setFields(fields);
        return Optional.of(dataset);
    }

    /**
     * 保存数据集 创建/修改
     * @param dataset 数据集
     */
    @Transactional
    public int save(Dataset dataset) {
        /*List<DatasetField> fields = dataset.getFields();
        for (DatasetField field : fields) {
            int status = field.getFieldStatus();
            if (Objects.equals(status, FieldStatus.DELETE_FIELD.getCode())) {
                throw new RuntimeException("数据集字段[" + field.getFieldName() + "]在原始表中已经被删除，请尽快联系原始表Owner处理");
            }
        }*/
        if (StringUtils.isBlank(dataset.getDatasetId())) {
            // 创建数据集
            return createDataset(dataset);
        } else {
            // 修改数据集
            return updateDataset(dataset);
        }
    }

    /**
     * 删除数据集ID
     * @param datasetId 数据集ID
     */
    @Transactional
    public int delete(String datasetId) {
        // 判断数据集是否存在
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (Objects.equals(dataset, null)) {
            log.error("数据集 {} 不存在，无法删除", datasetId);
            throw new RuntimeException("数据集不存在，无法删除");
        }
        // 内置数据集不可以删除
        if (Objects.equals(dataset.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置数据集 {} 不允许删除", datasetId);
            throw new RuntimeException("内置数据集不允许删除");
        }
        // 删除关联数据集字段
        datasetFieldService.deleteByDatasetId(datasetId);
        // TODO 检查依赖确保无下游使用
        log.info("删除数据集: {}", datasetId);
        return datasetMapper.deleteByDatasetId(datasetId);
    }

    /**
     * 创建数据集
     * @param dataset 数据集
     */
    private int createDataset(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByDatasetName(dataset.getDatasetName());
        if (!datasets.isEmpty()) {
            log.error("创建数据集失败，数据集 {} 已经存在，不允许重复添加", dataset.getDatasetName());
            throw new RuntimeException("数据集已经存在，不允许重复添加");
        }
        String datasetId = IDGenerator.getInstance().generate(ModelType.DATASET);
        Dataset target = datasetMapper.selectByDatasetId(datasetId);
        if (!Objects.equals(target, null)) {
            log.error("创建数据集失败，数据集ID {} 已经存在，不允许重复添加", datasetId);
            throw new RuntimeException("数据集ID已经存在，不允许重复添加");
        }
        dataset.setDatasetId(datasetId);
        dataset.setStatus(Status.ENABLE.getCode());
        dataset.setSourceType(SourceType.CUSTOM.getCode());
        dataset.setOwner(RequestContext.currentUserId());
        dataset.setCreator(RequestContext.currentUserId());
        dataset.setModifier(RequestContext.currentUserId());

        // 保存数据集字段
        List<DatasetField> fields = dataset.getFields();
        if (fields != null && !fields.isEmpty()) {
            for (DatasetField field : fields) {
                field.setDatasetId(datasetId);
                datasetFieldService.save(field);
            }
        }

        // TODO 创建数据集表 在引擎中创建数据集表

        // TODO 生成同步任务/实例/调度

        log.info("创建数据集: {}", gson.toJson(dataset));
        return datasetMapper.insertSelective(dataset);
    }

    /**
     * 修改数据集
     * @param dataset 数据集
     */
    private int updateDataset(Dataset dataset) {
        String datasetId = dataset.getDatasetId();
        // 先清空再保存数据集字段
        datasetFieldService.deleteByDatasetId(datasetId);
        List<DatasetField> fields = dataset.getFields();
        if (fields != null && !fields.isEmpty()) {
            for (DatasetField field : fields) {
                field.setDatasetId(datasetId);
                datasetFieldService.save(field);
            }
        }

        // 修改数据集
        dataset.setModifier(RequestContext.currentUserId());
        log.info("修改数据集: {}", gson.toJson(dataset));
        return datasetMapper.updateByDatasetIdSelective(dataset);
    }

    /**
     * 获取支持的数据源
     * @param datasetType 数据集类型
     */
    public List<DataSource> getDataSources(String datasetType) {
        // 模拟数据
        //DataSource dataSource = DataSource.builder().sourceType(1).build();
        DataSource dataSource = null;
        List<DataSource> dataSources = dataSourceService.getList(dataSource);
        log.info("获取数据集类型 {} 支持的数据源: {}", datasetType, gson.toJson(dataSources));
        return dataSources;
    }
}