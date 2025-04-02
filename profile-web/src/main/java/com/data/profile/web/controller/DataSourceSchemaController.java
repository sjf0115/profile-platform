package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.DataSourceSchema;
import com.data.profile.service.DataSourceSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：数据源类型Schema
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/datasource/schema", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataSourceSchemaController {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceSchemaController.class);

    @Autowired
    private DataSourceSchemaService dataSourceSchemaService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody DataSourceSchema schema) {
        List<DataSourceSchema> schemas = dataSourceSchemaService.getList(schema);
        return Response.success(schemas);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String schemaId) {
        Optional<DataSourceSchema> optional = dataSourceSchemaService.getDetail(schemaId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据源Schema不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody DataSourceSchema schema) {
        int result = dataSourceSchemaService.save(schema);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据源Schema失败", ResponseCode.ERROR);
        }
    }
}