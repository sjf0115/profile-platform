package com.data.profile.web.service;

import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
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
 * <p>负责数据集完整用例：元数据 CRUD + 基础设施操作（引擎表、同步任务）。</p>
 * <p>独立的执行逻辑由 DatasetTask 负责。</p>
 *
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
    private DatasetFieldService datasetFieldService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private TaskService taskService;
    @Resource
    private TaskInstanceService taskInstanceService;

    /**
     * 根据查询条件获取数据集列表
     */
    public List<Dataset> getList(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByParams(dataset);
        log.info("根据查询条件获取 {} 个数据集", datasets.size());
        return datasets;
    }

    /**
     * 根据数据集ID获取数据集详细信息
     */
    public Optional<Dataset> getDetail(String datasetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset == null) {
            return Optional.empty();
        }
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);
        dataset.setFields(fields);
        // 查询最新任务实例（关联查询）
        TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(datasetId);
        dataset.setLatestInstance(latestInstance);
        return Optional.of(dataset);
    }

    /**
     * 创建/更新数据集
     * @return 数据集ID
     */
    @Transactional
    public String save(Dataset dataset) {
        if (StringUtils.isBlank(dataset.getDatasetId())) {
            return createDataset(dataset);
        } else {
            updateDataset(dataset);
            return dataset.getDatasetId();
        }
    }

    /**
     * 删除数据集
     */
    @Transactional
    public int delete(String datasetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset == null) {
            log.error("数据集 {} 不存在，无法删除", datasetId);
            throw new RuntimeException("数据集不存在，无法删除");
        }
        if (Objects.equals(dataset.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置数据集 {} 不允许删除", datasetId);
            throw new RuntimeException("内置数据集不允许删除");
        }

        // 1. 删除引擎表
        dropEngineTable(datasetId);

        // 2. 删除关联同步任务
        taskService.deleteByRelatedId(datasetId);

        // 3. 删除数据集字段
        datasetFieldService.deleteByDatasetId(datasetId);

        // 4. 删除元数据
        log.info("删除数据集: {}", datasetId);
        return datasetMapper.deleteByDatasetId(datasetId);
    }

    /**
     * 获取支持的数据源
     */
    public List<DataSource> getDataSources(String datasetType) {
        List<DataSource> dataSources = dataSourceService.getList(null);
        log.info("获取数据集类型 {} 支持的数据源: {} 个", datasetType, dataSources.size());
        return dataSources;
    }

    // =========================================================================
    // 私有方法：元数据操作
    // =========================================================================

    /**
     * 创建数据集（元数据 + 引擎表 + 同步任务）
     */
    private String createDataset(Dataset dataset) {
        // 检查名称唯一性
        List<Dataset> datasets = datasetMapper.selectByDatasetName(dataset.getDatasetName());
        if (!datasets.isEmpty()) {
            throw new RuntimeException("数据集已经存在，不允许重复添加");
        }
        String datasetId = IDGenerator.getInstance().generate(ModelType.DATASET);
        if (datasetMapper.selectByDatasetId(datasetId) != null) {
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

        // 保存元数据
        datasetMapper.insertSelective(dataset);
        log.info("创建数据集元数据: datasetId={}, datasetName={}", datasetId, dataset.getDatasetName());

        // 创建引擎表
        createEngineTable(dataset);
        // 创建同步任务 TODO 与定时任务的区别
        createSyncTask(datasetId, dataset.getDatasetName());
        return datasetId;
    }

    /**
     * 修改数据集（元数据 + 引擎表 Schema 更新）
     */
    private void updateDataset(Dataset dataset) {
        String datasetId = dataset.getDatasetId();
        // 先清空再保存字段
        datasetFieldService.deleteByDatasetId(datasetId);
        List<DatasetField> fields = dataset.getFields();
        if (fields != null && !fields.isEmpty()) {
            for (DatasetField field : fields) {
                field.setDatasetId(datasetId);
                datasetFieldService.save(field);
            }
        }

        // 更新元数据
        dataset.setModifier(RequestContext.currentUserId());
        datasetMapper.updateByDatasetIdSelective(dataset);
        log.info("更新数据集元数据: datasetId={}", datasetId);

        // 基础设施操作：更新引擎表 Schema
        updateEngineTable(dataset);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // 私有方法：基础设施操作

    /**
     * 创建引擎表
     */
    private void createEngineTable(Dataset dataset) {
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = "profile_dataset_" + dataset.getDatasetId();
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName);
                log.info("创建引擎表成功: {}", tableName);
            }
        } catch (Exception e) {
            log.error("创建引擎表失败: datasetId={}", dataset.getDatasetId(), e);
            // 非阻塞：引擎表创建失败不影响元数据保存
        }
    }

    /**
     * 更新引擎表
     */
    private void updateEngineTable(Dataset dataset) {
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = "profile_dataset_" + dataset.getDatasetId();
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName);
                log.info("更新引擎表成功: {}", tableName);
            }
        } catch (Exception e) {
            log.error("更新引擎表失败: datasetId={}", dataset.getDatasetId(), e);
        }
    }

    /**
     * 删除引擎表
     */
    private void dropEngineTable(String datasetId) {
        try {
            analysisEngineService.dropDatasetTable(datasetId);
            log.info("删除引擎表成功: {}", datasetId);
        } catch (Exception e) {
            log.error("删除引擎表失败: {}", datasetId, e);
            throw new RuntimeException("删除引擎表失败，请联系管理员", e);
        }
    }

    /**
     * 创建同步任务（手动触发类型）TODO
     */
    private void createSyncTask(String datasetId, String datasetName) {
        Task task = new Task();
        task.setTaskName(datasetName + "-同步任务");
        task.setTaskDesc("数据集[" + datasetName + "]的同步任务");
        task.setTaskType(TaskType.IMPORT.getCode());
        task.setTaskRelatedId(datasetId);
        task.setTriggerType(SchedulerType.MANUAL.getCode());
        taskService.create(task);
        log.info("创建同步任务: datasetId={}", datasetId);
    }
}
