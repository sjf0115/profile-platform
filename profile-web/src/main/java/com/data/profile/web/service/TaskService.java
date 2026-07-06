package com.data.profile.web.service;

import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.dao.TaskMapper;
import com.data.profile.web.model.Task;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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

    /**
     * 根据调度任务查询条件获取调度任务列表
     */
    public List<Task> getList(Task task) {
        List<Task> tasks = taskMapper.selectByParams(task);
        log.info("根据查询条件获取 {} 个调度任务", tasks.size());
        return tasks;
    }

    /**
     * 根据调度任务ID获取任务详细信息
     */
    public Optional<Task> getDetail(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(task);
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
    public int create(Task task) {
        String taskId = IDGenerator.getInstance().generate(ModelType.TASK);
        Task target = taskMapper.selectByTaskId(taskId);
        if (!Objects.equals(target, null)) {
            log.error("调度任务ID {} 已经存在", taskId);
            throw new RuntimeException("调度任务ID已经存在");
        }
        task.setTaskId(taskId);
        task.setSourceType(SourceType.CUSTOM.getCode());
        task.setStatus(Status.ENABLE.getCode());
        task.setOwner(UserContextHolder.currentUserId());
        task.setCreator(UserContextHolder.currentUserId());
        task.setModifier(UserContextHolder.currentUserId());
        log.info("创建调度任务: {}", task.getTaskName());
        return taskMapper.insertSelective(task);
    }

    /**
     * 修改调度任务
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
        /*if (Objects.equals(task.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置调度任务 {} 不允许删除", task.getTaskId());
            throw new RuntimeException("内置调度任务不允许删除");
        }
        log.info("根据关联ID删除调度任务: relatedId={}", relatedId);
        taskMapper.deleteByRelatedId(relatedId);*/
    }
}