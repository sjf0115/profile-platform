package com.data.profile.web.controller;

import com.data.profile.common.enums.TriggerMode;
import com.data.profile.web.service.TaskExecutionService;
import com.data.profile.web.service.TaskInstanceService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.converter.DatasetConverter;
import com.data.profile.web.converter.TaskInstanceConverter;
import com.data.profile.web.dto.DatasetParam;
import com.data.profile.web.dto.DatasetRequest;
import com.data.profile.web.dto.ScheduleConfigRequest;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.vo.DatasetFieldVO;
import com.data.profile.web.vo.DatasetVO;
import com.data.profile.web.service.DatasetService;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.data.profile.common.domain.Constant.ENGINE_DATASET_TABLE_PREFIX;

/**
 * 功能：数据集
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataset", produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetController {
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private TaskExecutionService taskExecutionService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private DatasetFieldService datasetFieldService;

    @PostMapping(value = "/list")
    public Response<List<DatasetVO>> getList(@RequestBody DatasetParam param) {
        log.info("根据数据集信息请求查询数据集: {}", JSONUtils.toJsonString(param));
        Dataset dataset = DatasetConverter.param2do(param);
        List<Dataset> datasets = datasetService.getList(dataset);
        return Response.success(DatasetConverter.do2voList(datasets));
    }

    @GetMapping(value = "/detail")
    public Response<DatasetVO> getDetail(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求查看数据集信息: {}", datasetId);
        Optional<Dataset> optional = datasetService.getDetail(datasetId);
        if (!optional.isPresent()) {
            return Response.error("请求的数据集不存在", ResponseCode.ERROR);
        }
        Dataset dataset = optional.get();
        DatasetVO vo = DatasetConverter.do2vo(dataset);
        // 计算引擎表名
        vo.setEngineTableName(ENGINE_DATASET_TABLE_PREFIX + datasetId);
        // 填充字段列表
        List<DatasetField> fields = datasetFieldService.getListByDatasetId(datasetId);
        List<DatasetFieldVO> fieldVOs = new java.util.ArrayList<>();
        for (DatasetField f : fields) {
            DatasetFieldVO fvo = new DatasetFieldVO();
            org.springframework.beans.BeanUtils.copyProperties(f, fvo);
            fvo.setEntityField(f.getFieldName() != null && f.getFieldName().equals(dataset.getEntityField()));
            fieldVOs.add(fvo);
        }
        vo.setFields(fieldVOs);
        // 填充最新任务实例
        TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(datasetId);
        vo.setLatestInstance(TaskInstanceConverter.do2vo(latestInstance));
        return Response.success(vo);
    }

    @PostMapping(value = "/save")
    public Response<String> save(@RequestBody DatasetRequest req) {
        log.info("请求保存/更新数据集: {}", JSONUtils.toJsonString(req));
        Dataset dataset = DatasetConverter.request2do(req);
        String datasetId = datasetService.save(dataset, req.getFields());
        return Response.success(datasetId);
    }

    @DeleteMapping(value = "/delete")
    public Response<Integer> delete(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID {} 请求删除数据集", datasetId);
        int result = datasetService.delete(datasetId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除数据集失败", ResponseCode.ERROR);
        }
    }

    // 立即执行数据集同步
    @PostMapping(value = "/{datasetId}/execute")
    public Response<TaskInstance> execute(@PathVariable(value = "datasetId") String datasetId) {
        log.info("请求手动立即执行数据集 [{}] 同步", datasetId);
        try {
            // TODO 需要根据数据集ID和任务类型
            TaskInstance instance = taskExecutionService.executeByRelatedId(datasetId, TriggerMode.MANUAL);
            return Response.success(instance);
        } catch (Exception e) {
            log.error("手动立即执行数据集 [{}] 同步失败：{}", datasetId, e.getMessage());
            return Response.error("手动立即执行数据集同步失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // 配置数据集调度
    @PutMapping(value = "/{datasetId}/schedule")
    public Response<String> schedule(@PathVariable(value = "datasetId") String datasetId, @RequestBody ScheduleConfigRequest config) {
        log.info("请求配置数据集 [{}] 调度: {}", datasetId, JSONUtils.toJsonString(config));
        try {
            datasetService.schedule(datasetId, config);
            return Response.success("配置成功");
        } catch (Exception e) {
            log.error("配置数据集 [{}] 调度失败", datasetId, e);
            return Response.error("配置调度失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // 获取数据集调度配置
    @GetMapping(value = "/{datasetId}/schedule")
    public Response<Task> getScheduleConfig(@PathVariable(value = "datasetId") String datasetId) {
        log.info("请求获取数据集 [{}] 调度配置", datasetId);
        Task task = datasetService.getSchedulerConfig(datasetId);
        if (task != null) {
            return Response.success(task);
        } else {
            return Response.error("数据集没有关联的调度任务", ResponseCode.ERROR);
        }
    }

    // 获取支持数据集的数据源
    @GetMapping(value = "/datasources")
    public Response<List<DataSource>> getDataSources(@RequestParam(name = "dataset_type") String datasetType) {
        log.info("根据数据集类型 {} 请求查看支持的数据源", datasetType);
        List<DataSource> dataSources = datasetService.getDataSources(datasetType);
        return Response.success(dataSources);
    }
}
