package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.DataSource;
import com.data.profile.model.Dataset;
import com.data.profile.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private DatasetService datasetService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Dataset dataset) {
        List<Dataset> datasets = datasetService.getList(dataset);
        return Response.success(datasets);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("发起请求查看 {} 的数据集信息", datasetId);
        Optional<Dataset> optional = datasetService.getDetail(datasetId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据集不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Dataset dataset) {
        // TODO 输入验证
        int result = datasetService.save(dataset);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据集失败", ResponseCode.ERROR);
        }
    }

    // 获取支持数据集的数据源
    @GetMapping(value = "/datasources")
    public Response getDataSources(@RequestParam(name = "dataset_type") String datasetType) {
        List<DataSource> dataSources = datasetService.getDataSources(datasetType);
        return Response.success(dataSources);
    }

    /*@GetMapping(value = "/fields")
    public Response getFields(@RequestParam String datasourceId, @RequestParam String tableName, @RequestParam String datasetId) {
        List<DatasetField> fields = datasetService.getDatasetField(datasourceId, tableName, datasetId);
        return Response.success(fields);
    }*/
}