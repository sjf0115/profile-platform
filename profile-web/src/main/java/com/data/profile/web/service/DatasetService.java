package com.data.profile.web.service;

import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.dto.ScheduleConfigRequest;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.vo.DatasetFieldVO;
import com.data.profile.web.dto.DatasetDTO;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;

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
    @Autowired
    private ScheduleEngineService scheduleEngineService;
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Autowired
    private LineageService lineageService;

    /**
     * 根据查询条件获取数据集列表（仅元数据，不含字段）
     */
    public List<Dataset> getList(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByParams(dataset);
        log.info("根据查询条件获取 {} 个数据集", datasets.size());
        return datasets;
    }

    /**
     * 根据查询条件获取数据集列表（含字段详情，跨表聚合）
     *
     * @param dataset 查询条件
     * @return 带字段的数据集 DTO 列表
     */
    public List<DatasetDTO> getListWithFields(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByParams(dataset);
        List<DatasetDTO> dtos = new ArrayList<>();
        for (Dataset ds : datasets) {
            DatasetDTO dto = toDTO(ds);
            List<DatasetField> fields = datasetFieldService.getListByDatasetId(ds.getDatasetId());
            dto.setFields(toFieldVOList(fields, ds.getEntityField()));
            dtos.add(dto);
        }
        log.info("根据查询条件获取 {} 个数据集（含字段）", datasets.size());
        return dtos;
    }

    /**
     * 根据数据集ID获取数据集 Model（单表查询）
     */
    public Optional<Dataset> getDetail(String datasetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset == null) {
            return Optional.empty();
        }
        return Optional.of(dataset);
    }

    /**
     * 将 Dataset Model 转换为 DatasetDTO
     */
    private DatasetDTO toDTO(Dataset dataset) {
        DatasetDTO dto = new DatasetDTO();
        BeanUtils.copyProperties(dataset, dto);
        dto.setEngineTableName(ENGINE_DATASET_TABLE_PREFIX + dataset.getDatasetId());
        return dto;
    }

    /**
     * 创建/更新数据集
     * @param dataset 数据集元数据
     * @param fields  数据集字段列表（可为 null）
     * @return 数据集ID
     */
    @Transactional
    public String save(Dataset dataset, List<DatasetField> fields) {
        if (StringUtils.isBlank(dataset.getDatasetId())) {
            String datasetId = createDataset(dataset, fields);
            lineageService.refreshLineage(AssetType.DATASET.getCode(), datasetId);
            // 刷新新建字段关联的标签血缘
            if (fields != null) {
                for (DatasetField f : fields) {
                    if (f.getRelatedId() != null && !f.getRelatedId().isEmpty()) {
                        lineageService.refreshLineage(AssetType.LABEL.getCode(), f.getRelatedId());
                    }
                }
            }
            return datasetId;
        } else {
            String datasetId = dataset.getDatasetId();
            // 更新前：收集旧的标签关联
            Set<String> oldLabelIds = new HashSet<>();
            List<DatasetField> oldFields = datasetFieldService.getListByDatasetId(datasetId);
            for (DatasetField f : oldFields) {
                if (f.getRelatedId() != null && !f.getRelatedId().isEmpty()) {
                    oldLabelIds.add(f.getRelatedId());
                }
            }
            updateDataset(dataset, fields);
            lineageService.refreshLineage(AssetType.DATASET.getCode(), datasetId);
            // 刷新受影响的标签血缘（旧 ∪ 新）
            Set<String> newLabelIds = new HashSet<>();
            if (fields != null) {
                for (DatasetField f : fields) {
                    if (f.getRelatedId() != null && !f.getRelatedId().isEmpty()) {
                        newLabelIds.add(f.getRelatedId());
                    }
                }
            }
            Set<String> allLabelIds = new HashSet<>(oldLabelIds);
            allLabelIds.addAll(newLabelIds);
            for (String labelId : allLabelIds) {
                lineageService.refreshLineage(AssetType.LABEL.getCode(), labelId);
            }
            return datasetId;
        }
    }

    /**
     * 删除数据集
     */
    @Transactional
    public int delete(String datasetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset == null) {
            log.error("数据集 [{}] 不存在，无法删除", datasetId);
            throw new RuntimeException("数据集不存在，无法删除");
        }
        if (Objects.equals(dataset.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置数据集 [{}] 不允许删除", datasetId);
            throw new RuntimeException("内置数据集不允许删除");
        }

        // 删除前：收集关联标签ID（用于刷新血缘）
        Set<String> labelIds = new HashSet<>();
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);
        for (DatasetField f : fields) {
            if (f.getRelatedId() != null && !f.getRelatedId().isEmpty()) {
                labelIds.add(f.getRelatedId());
            }
        }

        // 删除保护：检查下游依赖
        lineageService.checkDeletable(AssetType.DATASET.getCode(), datasetId);

        // 1. 删除引擎表
        analysisEngineService.dropDatasetTable(datasetId);

        // 2. 删除关联同步任务
        taskService.deleteByRelatedId(datasetId);

        // 3. 删除数据集字段
        datasetFieldService.deleteByDatasetId(datasetId);

        // 4. 删除血缘
        lineageService.removeLineage(AssetType.DATASET.getCode(), datasetId);

        // 5. 删除数据集
        int result = datasetMapper.deleteByDatasetId(datasetId);

        // 6. 刷新受影响标签的血缘（上游 dataset 已删除，label→dataset 边被清除）
        for (String labelId : labelIds) {
            lineageService.refreshLineage(AssetType.LABEL.getCode(), labelId);
        }

        return result;
    }

    /**
     * 手动立即执行数据集同步
     * @param datasetId 数据集ID
     * @return 同步任务执行实例
     */
    /*public TaskInstance execute(String datasetId) {
        // TODO 需要根据数据集ID和任务类型获取
        Task task = taskService.getDetailByRelatedId(datasetId);
        if (task == null) {
            log.error("数据集 [{}] 没有关联任务无法执行同步", datasetId);
            throw new RuntimeException("数据集没有关联任务无法执行");
        }
        return taskExecutionService.executeTask(task, TriggerMode.MANUAL);
    }*/

    /**
     * 配置数据集调度
     */
    public void schedule(String datasetId, ScheduleConfigRequest config) {
        Task task = taskService.getDetailByRelatedId(datasetId);
        if (task == null) {
            log.error("数据集 [{}] 没有关联的同步任务，无法配置调度", datasetId);
            throw new RuntimeException("数据集没有关联的同步任务，请先创建数据集");
        }
        scheduleEngineService.configureSchedule(
                task.getTaskId(),
                config.getTriggerType(),
                config.getTriggerCron(),
                config.getTriggerStartTime(),
                config.getTriggerEndTime());
    }

    /**
     * 获取数据集关联的调度任务配置
     */
    public Task getSchedulerConfig(String datasetId) {
        return taskService.getDetailByRelatedId(datasetId);
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
    private String createDataset(Dataset dataset, List<DatasetField> fields) {
        // 1. 校验
        List<Dataset> datasets = datasetMapper.selectByDatasetName(dataset.getDatasetName());
        if (!datasets.isEmpty()) {
            log.error("数据集名称 [{}] 已经存在，不允许重复创建", dataset.getDatasetName());
            throw new RuntimeException("数据集已经存在，不允许重复创建");
        }
        String datasetId = IDGenerator.getInstance().generate(ModelType.DATASET);
        if (datasetMapper.selectByDatasetId(datasetId) != null) {
            log.error("数据集ID [{}] 已经存在，不允许重复创建", datasetId);
            throw new RuntimeException("数据集ID已经存在，不允许重复添加");
        }

        // 2. 数据集信息
        dataset.setDatasetId(datasetId);
        dataset.setStatus(Status.ENABLE.getCode());
        dataset.setSourceType(SourceType.CUSTOM.getCode());
        dataset.setOwner(UserContextHolder.currentUserId());
        dataset.setCreator(UserContextHolder.currentUserId());
        dataset.setModifier(UserContextHolder.currentUserId());
        datasetMapper.insertSelective(dataset);

        // 3. 数据集字段
        if (fields != null && !fields.isEmpty()) {
            for (DatasetField field : fields) {
                field.setDatasetId(datasetId);
                datasetFieldService.save(field);
            }
        }

        // TODO 4.5步骤 是否需要转移到手动执行/定时调度首次执行时执行，而不是创建数据集时执行
        // 4. 创建引擎表
        createEngineTable(dataset, fields);

        // 5. 创建同步任务
        createSyncTask(datasetId, dataset.getDatasetName());

        log.info("成功创建数据集: {}", gson.toJson(dataset));
        // 自动授权 MANAGE 给创建者
        resourceGrantService.grantOwner("06", datasetId, UserContextHolder.currentUserId());
        return datasetId;
    }

    /**
     * 修改数据集（元数据 + 引擎表 Schema 更新）
     */
    private void updateDataset(Dataset dataset, List<DatasetField> fields) {
        String datasetId = dataset.getDatasetId();
        // 先清空再保存字段
        datasetFieldService.deleteByDatasetId(datasetId);
        if (fields != null && !fields.isEmpty()) {
            for (DatasetField field : fields) {
                field.setDatasetId(datasetId);
                datasetFieldService.save(field);
            }
        }

        // 更新元数据
        dataset.setModifier(UserContextHolder.currentUserId());
        datasetMapper.updateByDatasetIdSelective(dataset);
        log.info("更新数据集元数据: datasetId={}", datasetId);

        // 基础设施操作：更新引擎表 Schema
        updateEngineTable(dataset, fields);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // 私有方法：基础设施操作

    /**
     * 创建引擎表
     */
    private void createEngineTable(Dataset dataset, List<DatasetField> fields) {
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = ENGINE_DATASET_TABLE_PREFIX + dataset.getDatasetId();
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName, fields);
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
    private void updateEngineTable(Dataset dataset, List<DatasetField> fields) {
        try {
            DataSource dataSource = dataSourceService.getDetail(dataset.getDatasourceId());
            if (dataSource != null) {
                String tableName = "profile_dataset_" + dataset.getDatasetId();
                analysisEngineService.buildAndUpsertTable(dataset, dataSource, tableName, fields);
                log.info("更新引擎表成功: {}", tableName);
            }
        } catch (Exception e) {
            log.error("更新引擎表失败: datasetId={}", dataset.getDatasetId(), e);
        }
    }

    /**
     * 创建同步任务
     */
    private void createSyncTask(String datasetId, String datasetName) {
        Task task = new Task();
        task.setTaskName(datasetName + "-同步任务");
        task.setTaskDesc("数据集[" + datasetName + "]的同步任务");
        task.setTaskType(TaskType.IMPORT.getCode());
        task.setTaskRelatedId(datasetId);
        task.setTriggerType(TriggerType.MANUAL.getCode()); // 默认无调度
        taskService.create(task);
        log.info("为数据集 [{}] 创建同步任务", datasetId);
    }

    /**
     * 将 DatasetField 列表转换为 DatasetFieldVO 列表，并标记 isEntityField
     */
    // TODO 数据集字段服务处理
    private List<DatasetFieldVO> toFieldVOList(List<DatasetField> fields, String entityField) {
        List<DatasetFieldVO> vos = new ArrayList<>();
        for (DatasetField f : fields) {
            DatasetFieldVO vo = new DatasetFieldVO();
            BeanUtils.copyProperties(f, vo);
            // 是否是实体ID对应字段
            vo.setEntityField(f.getFieldName() != null && f.getFieldName().equals(entityField));
            vos.add(vo);
        }
        return vos;
    }
}
