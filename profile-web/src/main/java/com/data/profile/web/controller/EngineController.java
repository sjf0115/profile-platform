package com.data.profile.web.controller;

import com.data.profile.common.domain.connector.request.TestConnectionRequestParam;
import com.data.profile.web.service.EngineService;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 功能：Engine 测试
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 12:59
 */
@Slf4j
@RestController
@RequestMapping(value = "/engine", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineController {

    @Resource
    private EngineService engineService;

    @PostMapping(value = "/connect", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response testConnection(@RequestBody TestConnectionRequestParam param)  {
        engineService.testConnect(param);
        return Response.success(null);
    }

    @PostMapping(value = "/di/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response executeDiJob(@RequestBody TestConnectionRequestParam param)  {
        engineService.executeDiTask(null);
        return Response.success(null);
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response test(@RequestBody TestConnectionRequestParam param)  {
        engineService.test();
        return Response.success(null);
    }
}
