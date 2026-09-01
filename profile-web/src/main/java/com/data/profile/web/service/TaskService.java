package com.data.profile.web.service;

import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.converter.TaskConverter;
import com.data.profile.web.dao.TaskMapper;
import com.data.profile.web.dto.TaskDTO;
import com.data.profile.web.dto.TaskRequest;
import com.data.profile.web.model.Task;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：任务服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class TaskService {
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private UserService userService;

    /**
     * 根据调度任务查询条件获取调度任务列表
     */
    public List<TaskDTO> getList(Task task) {
        List<Task> tasks = taskMapper.selectByParams(task);
        log.info("根据查询条件获取 {} 个调度任务", tasks.size());
        List<TaskDTO> dtos = TaskConverter.do2dtoList(tasks);
        // 填充人员名称
        Map<String, String> userMap = userService.getUserNameMap();
        for (TaskDTO dto : dtos) {
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 根据调度任务ID获取任务详细信息（返回 DTO，供 Controller 使用）
     */
    public TaskDTO getDetail(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            return null;
        }
        TaskDTO dto = TaskConverter.do2dto(task);
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        return dto;
    }

    /**
     * 根据调度任务ID获取任务 DO，不存在则抛异常（供内部服务使用）
     */
    public Task getTaskOrThrow(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        return task;
    }

    /**
     * 根据关联ID获取调度任务
     */
    public Task getDetailByRelatedId(String relatedId) {
        return taskMapper.selectByRelatedId(relatedId);
    }

    /**
     * 创建调度任务
     */
    public TaskDTO create(TaskRequest request) {
        String taskId = IDGenerator.getInstance().generate(ModelType.TASK);
        Task existing = taskMapper.selectByTaskId(taskId);
        if (!Objects.equals(existing, null)) {
            log.error("调度任务ID {} 已经存在", taskId);
            throw new RuntimeException("调度任务ID已经存在");
        }
        Task task = TaskConverter.request2do(request);
        task.setTaskId(taskId);
        task.setSourceType(SourceType.CUSTOM.getCode());
        task.setStatus(Status.ENABLE.getCode());
        task.setOwner(UserContextHolder.currentUserId());
        task.setCreator(UserContextHolder.currentUserId());
        task.setModifier(UserContextHolder.currentUserId());
        log.info("创建调度任务: {}", task.getTaskName());
        taskMapper.insertSelective(task);
        return TaskConverter.do2dto(task);
    }

    /**
     * 修改调度任务
     */
    public int update(String taskId, TaskRequest request) {
        Task task = TaskConverter.request2do(request);
        task.setTaskId(taskId);
        task.setModifier(UserContextHolder.currentUserId());
        log.info("修改调度任务: {}", taskId);
        return taskMapper.updateByTaskIdSelective(task);
    }

    /**
     * 更新任务状态（启用/停用）
     */
    public int updateStatus(String taskId, Integer status) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setStatus(status);
        task.setModifier(UserContextHolder.currentUserId());
        log.info("更新调度任务状态: taskId={}, status={}", taskId, status);
        return taskMapper.updateByTaskIdSelective(task);
    }

    /**
     * 创建调度任务（内部使用，接受 Task DO）
     */
    public Task createTask(Task task) {
        String taskId = IDGenerator.getInstance().generate(ModelType.TASK);
        task.setTaskId(taskId);
        task.setSourceType(SourceType.CUSTOM.getCode());
        task.setStatus(Status.ENABLE.getCode());
        task.setCreator(UserContextHolder.currentUserId());
        task.setModifier(UserContextHolder.currentUserId());
        log.info("创建调度任务: {}", task.getTaskName());
        taskMapper.insertSelective(task);
        return task;
    }

    /**
     * 修改调度任务（内部使用，接受 Task DO）
     */
    public int update(Task task) {
        task.setModifier(UserContextHolder.currentUserId());
        log.info("修改调度任务: {}", task.getTaskId());
        return taskMapper.updateByTaskIdSelective(task);
    }

    /**
     * 根据执行任务ID删除调度任务
     */
    public int delete(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (Objects.equals(task, null)) {
            log.warn("执行任务 [{}] 不存在", taskId);
            return 1;
        }
        if (Objects.equals(task.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置执行任务 [{}] 不允许删除", taskId);
            throw new RuntimeException("内置执行任务不允许删除");
        }
        log.info("删除执行任务: {}", taskId);
        return taskMapper.deleteByTaskId(taskId);
    }

    /**
     * 根据关联ID删除调度任务
     */
    public void deleteByRelatedId(String relatedId) {
        Task task = taskMapper.selectByRelatedId(relatedId);
        if (Objects.equals(task, null)) {
            log.warn("关联ID {} 对应的任务不存在", relatedId);
            return;
        }
        delete(task.getTaskId());
    }

    /**
     * 保存告警配置
     */
    public int saveAlertConfig(String taskId, String alertCondition, String alertChannels, String alertReceivers) {
        Task alertPart = new Task();
        alertPart.setTaskId(taskId);
        alertPart.setAlertCondition(alertCondition);
        alertPart.setAlertChannels(alertChannels);
        alertPart.setAlertReceivers(alertReceivers);
        alertPart.setModifier(UserContextHolder.currentUserId());
        log.info("保存告警配置: taskId={}", taskId);
        return taskMapper.updateByTaskIdSelective(alertPart);
    }

    /**
     * 获取告警配置
     */
    public Optional<Task> getAlertConfig(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(task);
    }
}
