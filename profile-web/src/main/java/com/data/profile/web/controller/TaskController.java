package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.enums.TriggerMode;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.service.TaskExecutionService;
import com.data.profile.web.engine.ScheduleEngineService;
import com.data.profile.web.service.TaskService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private static Gson gson = new GsonBuilder().create();
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskExecutionService taskExecutionService;
    @Autowired
    private ScheduleEngineService scheduleEngineService;

    // 任务列表
    @PostMapping(value = "/list")
    public Response<List<Task>> getList(@RequestBody Task task) {
        log.info("根据任务参数请求查询任务: {}", gson.toJson(task));
        List<Task> tasks = taskService.getList(task);
        return Response.success(tasks);
    }

    // 任务详情
    @GetMapping(value = "/{taskId}/detail")
    public Response<Task> getDetail(@PathVariable(value = "taskId") String taskId) {
        log.info("根据任务ID请求查询群组信息: {}", taskId);
        Optional<Task> optional = taskService.getDetail(taskId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的任务不存在", ResponseCode.ERROR);
        }
    }

    // 创建调度任务
    @PostMapping
    public Response<Integer> create(@RequestBody Task task) {
        log.info("请求创建任务: {}", gson.toJson(task));
        int result = taskService.create(task);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("创建调度任务失败", ResponseCode.ERROR);
        }
    }

    // 修改调度任务
    @PutMapping("/{taskId}")
    public Response<Integer> update(@PathVariable(value = "taskId") String taskId, @RequestBody Task task) {
        task.setTaskId(taskId);
        log.info("请求修改任务: {}", gson.toJson(task));
        int result = taskService.update(task);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改调度任务失败", ResponseCode.ERROR);
        }
    }

    // 删除调度任务
    @DeleteMapping(value = "/{taskId}")
    public Response<Integer> delete(@PathVariable(value = "taskId") String taskId) {
        log.info("根据任务ID {} 请求删除任务", taskId);
        int result = taskService.delete(taskId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除调度任务失败", ResponseCode.ERROR);
        }
    }

    /**
     * 手动触发任务执行
     *
     * @param taskId 任务ID
     */
    @PostMapping(value = "/{taskId}/execute")
    public Response<TaskInstance> execute(@PathVariable(value = "taskId") String taskId) {
        log.info("手动触发任务执行: {}", taskId);
        try {
            TaskInstance instance = taskExecutionService.executeTask(taskId, TriggerMode.MANUAL);
            return Response.success(instance);
        } catch (RuntimeException e) {
            log.warn("任务触发被拒绝: taskId={}, reason={}", taskId, e.getMessage());
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        } catch (Exception e) {
            log.error("任务触发失败: taskId={}", taskId, e);
            return Response.error("任务触发失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 调度引擎回调接口（由调度引擎定时触发）
     *
     * @param taskId 任务ID
     */
    @PostMapping(value = "/{taskId}/callback")
    public Response<TaskInstance> scheduledCallback(@PathVariable(value = "taskId") String taskId) {
        log.info("调度引擎回调任务执行: {}", taskId);
        try {
            TaskInstance instance = taskExecutionService.executeTask(taskId, TriggerMode.SCHEDULED);
            return Response.success(instance);
        } catch (RuntimeException e) {
            log.warn("调度回调被拒绝: taskId={}, reason={}", taskId, e.getMessage());
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        } catch (Exception e) {
            log.error("调度回调失败: taskId={}", taskId, e);
            return Response.error("调度回调失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 通过关联ID触发任务执行
     */
    @Deprecated
    @PostMapping(value = "/trigger-by-related-id")
    public Response<TaskInstance> triggerByRelatedId(@RequestParam(name = "related_id") String relatedId) {
        log.info("通过关联ID触发任务执行: relatedId={}", relatedId);
        try {
            TaskInstance instance = taskExecutionService.executeByRelatedId(relatedId, TriggerMode.MANUAL);
            return Response.success(instance);
        } catch (RuntimeException e) {
            log.warn("任务触发被拒绝: relatedId={}, reason={}", relatedId, e.getMessage());
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        } catch (Exception e) {
            log.error("任务触发失败: relatedId={}", relatedId, e);
            return Response.error("任务触发失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 配置任务上游依赖
     */
    @PutMapping(value = "/{taskId}/upstream")
    public Response<String> configureUpstream(@PathVariable(value = "taskId") String taskId, @RequestBody Task taskUpdate) {
        log.info("配置任务上游依赖: taskId={}, upstreamTaskIds={}", taskId, taskUpdate.getUpstreamTaskIds());
        try {
            Task task = taskService.getDetail(taskId)
                    .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
            task.setUpstreamTaskIds(taskUpdate.getUpstreamTaskIds());
            taskService.update(task);
            return Response.success("配置成功");
        } catch (Exception e) {
            log.error("配置上游依赖失败: taskId={}", taskId, e);
            return Response.error("配置上游依赖失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 通过调度引擎触发任务执行
     */
    @PostMapping(value = "/{taskId}/schedule-trigger")
    public Response<String> scheduleTrigger(@PathVariable(value = "taskId") String taskId) {
        log.info("通过调度引擎触发任务: taskId={}", taskId);
        try {
            scheduleEngineService.triggerSchedule(taskId);
            return Response.success("触发成功");
        } catch (Exception e) {
            log.error("调度引擎触发失败: taskId={}", taskId, e);
            return Response.error("调度引擎触发失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }
}
