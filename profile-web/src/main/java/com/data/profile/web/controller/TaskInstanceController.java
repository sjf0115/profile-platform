package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.service.TaskInstanceService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：任务实例
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/instance", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskInstanceController {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private TaskInstanceService instanceService;

    // 实例列表
    @PostMapping(value = "/list")
    public Response getList(@RequestBody TaskInstance instance) {
        log.info("根据任务执行实例信息请求查询任务执行实例: {}", gson.toJson(instance));
        List<TaskInstance> tasks = instanceService.getList(instance);
        return Response.success(tasks);
    }

    // 实例详情
    @GetMapping(value = "/{instanceId}/detail")
    public Response getDetail(@PathVariable(value = "instanceId") String instanceId) {
        Optional<TaskInstance> optional = instanceService.getDetail(instanceId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务实例不存在", ResponseCode.ERROR);
        }
    }

    // 根据任务ID查询实例列表
    @GetMapping(value = "/{taskId}/list")
    public Response listByTaskId(@RequestParam(name = "taskId") String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstance> instances = instanceService.getList(query);
        return Response.success(instances);
    }
}