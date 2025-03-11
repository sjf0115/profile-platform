package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Task;
import com.data.profile.model.TaskInstance;
import com.data.profile.service.TaskInstanceService;
import com.data.profile.service.TaskService;
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
    private static Logger LOG = LoggerFactory.getLogger(TaskInstanceController.class);

    @Autowired
    private TaskInstanceService instanceService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody TaskInstance instance) {
        List<TaskInstance> tasks = instanceService.getList(instance);
        return Response.success(tasks);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String instanceId) {
        Optional<TaskInstance> optional = instanceService.getDetail(instanceId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务实例不存在", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/add")
    public Response add(@RequestParam String instanceName) {
        int result = instanceService.add(instanceName);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("创建任务实例失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/delete")
    public Response delete(@RequestParam String instanceId) {
        int result = instanceService.delete(instanceId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除任务实例失败", ResponseCode.ERROR);
        }
    }
}