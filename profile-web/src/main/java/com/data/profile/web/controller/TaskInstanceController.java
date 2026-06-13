package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.service.TaskInstanceService;
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
    @Autowired
    private TaskInstanceService instanceService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody TaskInstance instance) {
        List<TaskInstance> tasks = instanceService.getList(instance);
        return Response.success(tasks);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "instance_id") String instanceId) {
        Optional<TaskInstance> optional = instanceService.getDetail(instanceId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务实例不存在", ResponseCode.ERROR);
        }
    }

    /**
     * 根据任务ID查询实例列表
     */
    @GetMapping(value = "/listByTaskId")
    public Response listByTaskId(@RequestParam(name = "task_id") String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstance> instances = instanceService.getList(query);
        return Response.success(instances);
    }
}