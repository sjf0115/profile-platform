package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Task;
import com.data.profile.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：调度任务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/task", produces = MediaType.APPLICATION_JSON_VALUE)
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Task task) {
        List<Task> tasks = taskService.getList(task);
        return Response.success(tasks);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "task_id") String taskId) {
        Optional<Task> optional = taskService.getDetail(taskId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务不存在", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/detailByRelatedId")
    public Response getDetailByRelatedId(@RequestParam(name = "related_id") String relatedId) {
        Task task = taskService.getDetailByRelatedId(relatedId);
        if (!Objects.equals(task, null)) {
            return Response.success(task);
        } else {
            return Response.error("请求的任务不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Task task) {
        String taskId = task.getTaskId();
        if (StringUtils.isEmpty(taskId)) {
            // 创建调度任务
            int result = taskService.create(task);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("创建调度任务失败", ResponseCode.ERROR);
            }
        } else {
            // 修改调度任务
            int result = taskService.update(task);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改调度任务失败", ResponseCode.ERROR);
            }
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "task_id") String taskId) {
        int result = taskService.delete(taskId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除调度任务失败", ResponseCode.ERROR);
        }
    }
}