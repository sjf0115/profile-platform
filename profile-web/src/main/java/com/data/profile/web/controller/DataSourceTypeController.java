package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.DataSourceType;
import com.data.profile.service.DataSourceTypeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：数据源类型
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataSourceType", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataSourceTypeController {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceTypeController.class);

    @Autowired
    private DataSourceTypeService dataSourceTypeService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody DataSourceType dataSourceType) {
        List<DataSourceType> dataSources = dataSourceTypeService.getList(dataSourceType);
        return Response.success(dataSources);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String dataSourceTypeId) {
        Optional<DataSourceType> optional = dataSourceTypeService.getDetail(dataSourceTypeId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据源类型不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody DataSourceType dataSourceType) {
        int result = dataSourceTypeService.save(dataSourceType);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据源类型失败", ResponseCode.ERROR);
        }
    }
}