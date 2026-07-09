package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.web.vo.TaskInstanceVO;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.converter.TaskInstanceConverter;
import com.data.profile.web.dto.TaskInstanceParam;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.service.TaskInstanceService;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
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

    // 实例列表
    @PostMapping(value = "/list")
    public Response<List<TaskInstanceVO>> getList(@RequestBody TaskInstanceParam param) {
        log.info("根据任务执行实例信息查询任务执行实例: {}", JSONUtils.toJsonString(param));
        TaskInstance instance = TaskInstanceConverter.param2do(param);
        List<TaskInstance> instances = instanceService.getList(instance);
        return Response.success(TaskInstanceConverter.do2voList(instances));
    }

    // 实例详情
    @GetMapping(value = "/{instanceId}/detail")
    public Response<TaskInstanceVO> getDetail(@PathVariable(value = "instanceId") String instanceId) {
        Optional<TaskInstance> optional = instanceService.getDetail(instanceId);
        if (optional.isPresent()) {
            return Response.success(TaskInstanceConverter.do2vo(optional.get()));
        } else {
            return Response.error("请求的任务实例不存在", ResponseCode.ERROR);
        }
    }

    // 根据任务ID查询实例列表
    @GetMapping(value = "/{taskId}/list")
    public Response<List<TaskInstanceVO>> listByTaskId(@PathVariable(name = "taskId") String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstance> instances = instanceService.getList(query);
        return Response.success(TaskInstanceConverter.do2voList(instances));
    }
}
