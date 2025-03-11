package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Task;
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
 * 功能：任务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/task", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {
    private static Logger LOG = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody Task task) {
        List<Task> tasks = taskService.getList(task);
        return Response.success(tasks);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String taskId) {
        Optional<Task> optional = taskService.getDetail(taskId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务不存在", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/add")
    public Response add(@RequestParam String taskName) {
        int result = taskService.add(taskName);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("创建调度任务失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/delete")
    public Response delete(@RequestParam String taskId) {
        int result = taskService.delete(taskId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除调度任务失败", ResponseCode.ERROR);
        }
    }
}