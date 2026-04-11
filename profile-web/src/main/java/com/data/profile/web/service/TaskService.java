package com.data.profile.web.service;

import com.data.profile.web.dao.TaskMapper;
import com.data.profile.web.model.Task;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class TaskService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private TaskMapper taskMapper;

    /**
     * 根据调度任务查询条件获取调度任务列表
     * @param task 调度任务查询条件
     */
    public List<Task> getList(Task task) {
        List<Task> tasks = taskMapper.selectByParams(task);
        log.info("根据查询条件获取 {} 个调度任务: {}", tasks.size(), gson.toJson(tasks));
        return tasks;
    }

    /**
     * 根据调度任务ID获取任务详细信息
     * @param taskId 调度任务ID
     */
    public Optional<Task> getDetail(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            return Optional.empty();
        }
        log.info("根据调度任务ID {} 获取任务详细信息: {}", taskId, gson.toJson(task));
        return Optional.of(task);
    }

    /**
     * 根据关联ID获取调度任务
     * @param relatedId 关联ID
     */
    public Task getDetailByRelatedId(String relatedId) {
        Task task = taskMapper.selectByRelatedId(relatedId);
        log.info("根据关联ID {} 获取调度任务详细信息: {}", relatedId, gson.toJson(task));
        return task;
    }

    /**
     * 创建调度任务
     * @param task 调度任务
     */
    public int create(Task task) {
        String taskId = IDGenerator.getInstance().generate(ModelType.TASK);
        Task target = taskMapper.selectByTaskId(taskId);
        if (!Objects.equals(target, null)) {
            log.error("调度任务ID {} 已经存在，不允许重复添加", taskId);
            throw new RuntimeException("调度任务ID已经存在，不允许重复添加");
        }
        task.setTaskId(taskId);
        task.setSourceType(SourceType.CUSTOM.getCode());
        task.setStatus(Status.ENABLE.getCode());
        task.setOwner(RequestContext.currentUserId());
        task.setCreator(RequestContext.currentUserId());
        task.setModifier(RequestContext.currentUserId());
        log.info("创建调度任务: {}", gson.toJson(task));
        // Todo 注册定时任务
        return taskMapper.insertSelective(task);
    }

    /**
     * 修改调度任务
     * @param task 调度任务
     */
    public int update(Task task) {
        task.setModifier(RequestContext.currentUserId());
        log.info("修改调度任务: {}", gson.toJson(task));
        // Todo 修改定时任务
        return taskMapper.updateByTaskIdSelective(task);
    }

    /**
     * 根据调度任务ID删除调度任务
     * @param taskId 调度任务ID
     */
    public int delete(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (Objects.equals(task, null)) {
            log.warn("调度任务 {} 不存在，无法删除", taskId);
            return 1;
        }
        if (Objects.equals(task.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置调度任务 {} 不允许删除", taskId);
            throw new RuntimeException("内置调度任务不允许删除");
        }
        // Todo 删除定时任务
        log.info("根据调度任务ID删除任务: {}", taskId);
        return taskMapper.deleteByTaskId(taskId);
    }

    /**
     * 根据关联ID删除调度任务
     * @param relatedId 关联ID
     */
    public int deleteByRelatedId(String relatedId) {
        Task task = taskMapper.selectByRelatedId(relatedId);
        if (Objects.equals(task, null)) {
            log.warn("调度关联ID {} 对应的任务不存在，无法删除", relatedId);
            return 1;
        }
        if (Objects.equals(task.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("调度关联ID {} 对应的内置调度任务 {} 不允许删除", relatedId, task.getTaskId());
            throw new RuntimeException("内置调度任务不允许删除");
        }
        // Todo 删除定时任务
        log.info("根据关联ID删除调度任务: {}", relatedId);
        return taskMapper.deleteByRelatedId(relatedId);
    }
}