package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.vo.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 功能：调度引擎管理
 * 作者：@Smartsi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@RestController
@RequestMapping(value = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
public class ScheduleEngineController {
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private ScheduleEngineService engineService;

    // 调度引擎联通测试
    @GetMapping(value = "/test")
    public Response testScheduleConnection(@RequestParam(name = "engine_id") String engineId) {
        log.info("测试调度引擎 {} 联通性", engineId);
        try {
            Map<String, Object> result = engineService.testConnection(engineId);
            Boolean connected = (Boolean) result.get("connected");
            if (Boolean.TRUE.equals(connected)) {
                return Response.success(result);
            } else {
                return Response.error("调度引擎连通失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            log.error("调度引擎联通测试异常: {}", e.getMessage(), e);
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
