package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.DatasetField;
import com.data.profile.service.DatasetFieldService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：DatasetFieldController
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/3/20 21:00
 */
@Deprecated
@Slf4j
@RestController
@RequestMapping(value = "/dataset/field", produces = MediaType.APPLICATION_JSON_VALUE)
public class DatasetFieldController {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private DatasetFieldService datasetFieldService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody DatasetField datasetField) {
        log.info("根据查询条件请求获取数据集字段: {}", gson.toJson(datasetField));
        List<DatasetField> datasetFields = datasetFieldService.getList(datasetField);
        return Response.success(datasetFields);
    }

    @GetMapping(value = "/listByDataset")
    public Response getListByDataset(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求获取数据集字段: {}", datasetId);
        List<DatasetField> datasetFields = datasetFieldService.getListByDatasetId(datasetId);
        return Response.success(datasetFields);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "id") Long id) {
        log.info("根据数据集字段ID请求获取数据集字段详细信息: {}", id);
        Optional<DatasetField> optional = datasetFieldService.getDetail(id);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据集字段不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody DatasetField datasetField) {
        log.info("请求保存数据集字段: {}", gson.toJson(datasetField));
        int result = datasetFieldService.save(datasetField);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据集字段失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "id") Long id) {
        log.info("根据数据集字段ID请求删除数据集字段: {}", id);
        int result = datasetFieldService.deleteById(id);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除数据集字段失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/deleteByDataset")
    public Response deleteByDataset(@RequestParam(name = "dataset_id") String datasetId) {
        log.info("根据数据集ID请求删除所有字段: {}", datasetId);
        int result = datasetFieldService.deleteByDatasetId(datasetId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除数据集字段失败", ResponseCode.ERROR);
        }
    }
}