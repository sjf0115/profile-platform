package com.data.profile.web.controller;

import com.data.profile.web.dao.AlertHistoryMapper;
import com.data.profile.web.model.AlertHistory;
import com.data.profile.web.model.Task;
import com.data.profile.web.service.TaskService;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：告警配置
 */
@Slf4j
@RestController
@RequestMapping(value = "/task/{taskId}/alert", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlertController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AlertHistoryMapper alertHistoryMapper;

    /**
     * 获取告警配置
     */
    @GetMapping
    public Response<Task> getAlertConfig(@PathVariable String taskId) {
        Optional<Task> task = taskService.getAlertConfig(taskId);
        if (!task.isPresent()) {
            return Response.error("任务不存在", ResponseCode.ERROR);
        }
        return Response.success(task.get());
    }

    /**
     * 保存告警配置
     */
    @PutMapping
    public Response<Integer> saveAlertConfig(@PathVariable String taskId, @RequestBody Task alertPart) {
        int rows = taskService.saveAlertConfig(
                taskId,
                alertPart.getAlertCondition(),
                alertPart.getAlertChannels(),
                alertPart.getAlertReceivers()
        );
        return Response.success(rows);
    }

    /**
     * 查询告警历史记录
     */
    @GetMapping("/history")
    public Response<List<AlertHistory>> getAlertHistory(
            @PathVariable String taskId,
            @RequestParam(required = false) String instanceId) {
        List<AlertHistory> list = alertHistoryMapper.selectByParams(taskId, instanceId);
        return Response.success(list);
    }
}
