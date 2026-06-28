package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.dto.ScheduleConfigRequest;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.vo.DatasetFieldVO;
import com.data.profile.web.vo.DatasetVO;
import com.data.profile.web.service.DatasetService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private DatasetService datasetService;

    @PostMapping(value = "/list")
    public Response<List<DatasetVO>> getList(@RequestBody Dataset dataset) {
        log.info("根据数据集信息请求查询数据集: {}", gson.toJson(dataset));
        List<DatasetVO> datasets = datasetService.getList(dataset);
        return Response.success(datasets);
    }

    @GetMapping(value = "/detail")
    public Response<DatasetVO> getDetail(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求查看数据集信息: {}", datasetId);
        Optional<DatasetVO> optional = datasetService.getDetail(datasetId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据集不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response<String> save(@RequestBody DatasetVO datasetVO) {
        log.info("请求保存/更新数据集: {}", gson.toJson(datasetVO));
        Dataset dataset = new Dataset();
        BeanUtils.copyProperties(datasetVO, dataset);
        // 将 DatasetFieldVO 转回 DatasetField
        List<DatasetField> fields = new ArrayList<>();
        if (datasetVO.getFields() != null) {
            for (DatasetFieldVO fvo : datasetVO.getFields()) {
                DatasetField f = new DatasetField();
                BeanUtils.copyProperties(fvo, f);
                fields.add(f);
            }
        }
        String datasetId = datasetService.save(dataset, fields);
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
            TaskInstance instance = datasetService.execute(datasetId);
            return Response.success(instance);
        } catch (Exception e) {
            log.error("手动立即执行数据集 [{}] 同步失败：{}", datasetId, e.getMessage());
            return Response.error("手动立即执行数据集同步失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // 配置数据集调度
    @PutMapping(value = "/{datasetId}/schedule")
    public Response<String> schedule(@PathVariable(value = "datasetId") String datasetId, @RequestBody ScheduleConfigRequest config) {
        log.info("请求配置数据集 [{}] 调度: {}", datasetId, gson.toJson(config));
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
    public Response getDataSources(@RequestParam(name = "dataset_type") String datasetType) {
        log.info("根据数据集类型 {} 请求查看支持的数据源", datasetType);
        List<DataSource> dataSources = datasetService.getDataSources(datasetType);
        return Response.success(dataSources);
    }
}
