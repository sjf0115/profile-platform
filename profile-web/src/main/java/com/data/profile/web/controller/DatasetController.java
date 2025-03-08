package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Dataset;
import com.data.profile.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger LOG = LoggerFactory.getLogger(DatasetController.class);

    @Autowired
    private DatasetService datasetService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody Dataset dataset) {
        List<Dataset> datasets = datasetService.getList(dataset);
        return Response.success(datasets);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String datasetId) {
        Optional<Dataset> optional = datasetService.getDetail(datasetId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据集不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Dataset dataset) {
        int result = 0;
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据集失败", ResponseCode.ERROR);
        }
    }
}