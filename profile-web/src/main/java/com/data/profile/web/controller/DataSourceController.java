package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.DataSource;
import com.data.profile.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：数据源
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/dataSource", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataSourceController {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody DataSource dataSource) {
        List<DataSource> dataSources = dataSourceService.getList(dataSource);
        return Response.success(dataSources);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String datasourceId) {
        Optional<DataSource> optional = dataSourceService.getDetail(datasourceId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的数据源不存在", ResponseCode.DATASOURCE_NO_ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody DataSource datasource) {
        int result = dataSourceService.save(datasource);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加数据源失败", ResponseCode.DATASOURCE_NO_ERROR);
        }
    }
}