package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.Engine;
import com.data.profile.web.service.EngineService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.vo.Item;
import com.data.profile.web.vo.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 功能：计算引擎管理
 * 作者：@Smartsi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@RestController
@RequestMapping(value = "/engine", produces = MediaType.APPLICATION_JSON_VALUE)
public class EngineController {
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private EngineService engineService;

    @Autowired
    private ScheduleEngineService scheduleEngineService;

   /**
     * 获取引擎列表
     */
    @PostMapping(value = "/list")
    public Response getList(@RequestBody Engine engine) {
        log.info("请求查询引擎列表: {}", gson.toJson(engine));
        List<Engine> engines = engineService.getList(engine);
        return Response.success(engines);
    }

    /**
     * 获取引擎详情
     */
    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "engine_id") String engineId) {
        log.info("根据引擎ID {} 请求查看引擎信息", engineId);
        Engine engine = engineService.getDetail(engineId);
        if (!Objects.equals(engine, null)) {
            return Response.success(engine);
        } else {
            return Response.error("请求的引擎不存在", ResponseCode.ERROR);
        }
    }

    /**
     * 获取默认引擎（如果不存在则返回null）
     */
    @GetMapping(value = "/default")
    public Response getDefaultEngine() {
        log.info("请求获取默认引擎");
        Engine engine = engineService.getDefaultEngine();
        return Response.success(engine);
    }

    /**
     * 保存引擎（新增/修改）
     */
    @PostMapping(value = "/save")
    public Response save(@RequestBody Engine engine) {
        log.info("请求创建/修改引擎: {}", gson.toJson(engine));
        try {
            int result = engineService.save(engine);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("保存引擎失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            log.error("保存引擎失败: {}", e.getMessage(), e);
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除引擎
     */
    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "engine_id") String engineId) {
        log.info("根据引擎ID {} 请求删除引擎", engineId);
        try {
            int result = engineService.delete(engineId);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除引擎失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            log.error("删除引擎失败: {}", e.getMessage(), e);
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 设置默认引擎
     */
    @PostMapping(value = "/{engineId}/set-default")
    public Response setDefaultEngine(@PathVariable String engineId) {
        log.info("设置引擎 {} 为默认引擎", engineId);
        try {
            engineService.setDefaultEngine(engineId);
            return Response.success("设置默认引擎成功");
        } catch (RuntimeException e) {
            log.error("设置默认引擎失败: {}", e.getMessage(), e);
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取所有支持的引擎类型
     */
    @GetMapping(value = "/type/list")
    public Response getEngineTypeList() {
        log.info("请求获取引擎类型列表");
        List<Item> engineTypes = engineService.getEngineTypeList();
        return Response.success(engineTypes);
    }

    /**
     * 调度引擎联通测试
     * 测试指定引擎（category=schedule）与 DolphinScheduler 服务的连通性
     */
    @PostMapping(value = "/{engineId}/schedule-connect")
    public Response testScheduleConnection(@PathVariable String engineId) {
        log.info("测试调度引擎联通性: engineId={}", engineId);
        try {
            java.util.Map<String, Object> result = scheduleEngineService.testConnection(engineId);
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
