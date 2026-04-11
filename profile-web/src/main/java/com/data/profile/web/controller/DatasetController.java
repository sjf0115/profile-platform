package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.service.DatasetService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private DatasetService datasetService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Dataset dataset) {
        log.info("根据数据集信息请求查询数据集: {}", gson.toJson(dataset));
        List<Dataset> datasets = datasetService.getList(dataset);
        return Response.success(datasets);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求查看数据集信息: {}", datasetId);
        Optional<Dataset> optional = datasetService.getDetail(datasetId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据集不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Dataset dataset) {
        log.info("请求保存/更新数据集: {}", gson.toJson(dataset));
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
        log.info("根据数据集类型 {} 请求查看支持的数据源", datasetType);
        List<DataSource> dataSources = datasetService.getDataSources(datasetType);
        return Response.success(dataSources);
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID {} 请求删除数据集", datasetId);
        int result = datasetService.delete(datasetId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除数据集失败", ResponseCode.ERROR);
        }
    }

    /*@GetMapping(value = "/refresh")
    public Response refresh(@RequestParam(name = "datasource_id") String datasourceId,
                            @RequestParam(name = "dataset_id") String datasetId,
                            @RequestParam(name = "table_name") String tableName) {
        log.info("根据数据源ID {}、数据集ID {}、表名 {} 请求刷新数据集字段", datasourceId, datasetId, tableName);
        List<DatasetField> fields = datasetService.refresh(datasourceId, datasetId, tableName);
        if (!Objects.equals(fields, null)) {
            return Response.success(fields);
        } else {
            return Response.error("刷新数据集字段失败", ResponseCode.ERROR);
        }
    }*/
}