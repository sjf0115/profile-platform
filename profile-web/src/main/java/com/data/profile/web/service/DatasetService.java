package com.data.profile.web.service;

import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
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
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private TaskService taskService;
    @Resource
    private ScheduleEngineService scheduleEngineService;

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
        // TODO 检查依赖确保无下游使用
        // 删除关联数据集字段
        datasetFieldService.deleteByDatasetId(datasetId);
        // 删除数据集对应的引擎表
        try {
            analysisEngineService.dropDatasetTable(datasetId);
        } catch (Exception e) {
            log.error("删除数据集 {} 引擎表失败，数据集已删除但引擎表可能残留", datasetId, e);
            throw new RuntimeException("删除数据集引擎表失败，请连续管理员");
        }

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

        // 保存数据集基本信息
        int result = datasetMapper.insertSelective(dataset);

        // 创建引擎表（仅 Schema，不同步数据）
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = "profile_dataset_" + datasetId;
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName);
            }
        } catch (Exception e) {
            log.error("创建引擎表失败: {}", datasetId, e);
        }

        // 自动创建数据集同步任务
        createDatasetSyncTask(datasetId, dataset.getDatasetName());

        log.info("创建数据集: {}", gson.toJson(dataset));
        return result;
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
        int result = datasetMapper.updateByDatasetIdSelective(dataset);

        // 更新引擎表 Schema（不同步数据）
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = "profile_dataset_" + datasetId;
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName);
            }
        } catch (Exception e) {
            log.error("更新引擎表失败: {}", datasetId, e);
        }

        log.info("修改数据集: {}", gson.toJson(dataset));
        return result;
    }

    /**
     * 获取支持的数据源
     * @param datasetType 数据集类型
     */
    public List<DataSource> getDataSources(String datasetType) {
        DataSource dataSource = null;
        List<DataSource> dataSources = dataSourceService.getList(dataSource);
        log.info("获取数据集类型 {} 支持的数据源: {}", datasetType, gson.toJson(dataSources));
        return dataSources;
    }

    /**
     * 自动创建数据集同步任务（手动触发类型）。
     */
    private void createDatasetSyncTask(String datasetId, String datasetName) {
        Task task = new Task();
        task.setTaskName(datasetName + "-同步任务");
        task.setTaskDesc("数据集[" + datasetName + "]的同步任务");
        task.setTaskType(SchedulerJobType.IMPORT.getCode());
        task.setTaskRelatedId(datasetId);
        task.setTriggerType(SchedulerType.MANUAL.getCode());
        taskService.create(task);
        log.info("自动创建数据集同步任务: datasetId={}", datasetId);
    }

    /**
     * 更新数据集的最新实例状态。
     */
    public void updateInstanceStatus(String datasetId, int instanceStatus, String instanceMsg) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset != null) {
            dataset.setInstanceStatus(instanceStatus);
            dataset.setInstanceMsg(instanceMsg != null ? Integer.valueOf(instanceMsg.length()) : null);
            datasetMapper.updateByDatasetIdSelective(dataset);
        }
    }

    /**
     * 配置数据集调度。
     * <p>更新数据集关联的同步任务的调度配置（触发类型、Cron 表达式、生效时间）。</p>
     * @param datasetId 数据集ID
     * @param schedulerConfig 调度配置
     */
    public void configureScheduler(String datasetId, Task schedulerConfig) {
        // 调度类型: 1-手动触发, 3-日周期调度, 4-小时周期调度
        Integer triggerType = schedulerConfig.getTriggerType();
        // Cron 表达式（周期调度时必填）
        String triggerCron = schedulerConfig.getTriggerCron();
        // 生效开始时间
        String triggerStartTime = schedulerConfig.getTriggerStartTime();
        // 生效结束时间
        String triggerEndTime = schedulerConfig.getTriggerEndTime();

        Task task = taskService.getDetailByRelatedId(datasetId);
        if (task == null) {
            log.error("数据集 {} 没有关联的同步任务，无法配置调度", datasetId);
            throw new RuntimeException("数据集没有关联的同步任务，请先创建数据集");
        }
        // 委托 ScheduleEngineService（同时更新元数据 + 同步调度引擎）
        scheduleEngineService.configureSchedule(task.getTaskId(), triggerType, triggerCron, triggerStartTime, triggerEndTime);
    }

    /**
     * 获取数据集关联的调度任务配置。
     */
    public Task getSchedulerConfig(String datasetId) {
        return taskService.getDetailByRelatedId(datasetId);
    }
}