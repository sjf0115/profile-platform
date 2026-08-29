package com.data.profile.web.controller;

import com.data.profile.common.enums.TriggerMode;
import com.data.profile.web.dto.DatasetDTO;
import com.data.profile.web.service.TaskExecutionService;
import com.data.profile.web.service.TaskInstanceService;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.annotation.RequiresPermission;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<DatasetVO> voList = DatasetConverter.do2voList(datasets);
        // 填充最新任务实例
        for (int i = 0; i < datasets.size(); i++) {
            TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(datasets.get(i).getDatasetId());
            voList.get(i).setLatestInstance(TaskInstanceConverter.do2vo(latestInstance));
        }
        return Response.success(voList);
    }

    @GetMapping(value = "/detail")
    public Response<DatasetVO> getDetail(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求查看数据集信息: {}", datasetId);
        DatasetDTO datasetDTO = datasetService.getDetail(datasetId);
        DatasetVO vo = DatasetConverter.dto2vo(datasetDTO);
        return Response.success(vo);
    }

    @RequiresPermission(code = "dataset:edit", name = "数据集-编辑")
    @PostMapping(value = "/save")
    public Response<String> save(@RequestBody DatasetRequest req) {
        log.info("请求保存/更新数据集: {}", JSONUtils.toJsonString(req));
        Dataset dataset = DatasetConverter.request2do(req);
        String datasetId = datasetService.save(dataset, req.getFields());
        return Response.success(datasetId);
    }

    @PutMapping(value = "/{datasetId}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "datasetId") String datasetId, @RequestParam Integer status) {
        log.info("请求更新数据集 {} 状态为：{}", datasetId, status);
        try {
            int result = datasetService.updateStatus(datasetId, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "dataset:delete", name = "数据集-删除")
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
    @RequiresPermission(code = "dataset:execute", name = "数据集-执行")
    @PostMapping(value = "/{datasetId}/execute")
    public Response<TaskInstance> execute(@PathVariable(value = "datasetId") String datasetId) {
        log.info("请求手动立即执行数据集 [{}] 同步", datasetId);
        try {
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
