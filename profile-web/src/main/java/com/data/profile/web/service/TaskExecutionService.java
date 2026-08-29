package com.data.profile.web.service;

import com.data.profile.common.enums.InstanceStatus;
import com.data.profile.common.enums.TaskType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.enums.TriggerMode;
import com.data.profile.web.dto.TaskInstanceDTO;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.task.ExecutionContext;
import com.data.profile.web.task.TaskExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 功能：任务执行服务
 * <p>负责任务执行的完整协调流程：创建实例 → 异步分发执行器 → 更新状态。</p>
 * <p>使用策略模式通过 TaskExecutor 接口分发执行逻辑。</p>
 * <p>支持异步执行和并发控制，防止重复执行。</p>
 *
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class TaskExecutionService {
    @Resource
    private TaskService taskService;
    @Resource
    private TaskInstanceService taskInstanceService;
    @Resource
    private AlertService alertService;

    /**
     * 任务执行器映射（策略模式）
     * Spring 自动注入所有 TaskExecutor 实现
     */
    private final Map<TaskType, TaskExecutor> executorMap;

    /**
     * 异步执行线程池
     * TODO: 生产环境应使用可配置的线程池，并考虑持久化队列
     */
    private final ExecutorService taskExecutorPool = Executors.newFixedThreadPool(10);

    @Autowired
    public TaskExecutionService(List<TaskExecutor> executors) {
        this.executorMap = executors.stream()
                .collect(Collectors.toMap(TaskExecutor::getType, Function.identity()));
        log.info("注册任务执行器: {}", executorMap.keySet());
    }

    /**
     * 通过任务ID异步执行任务
     * @param taskId 任务ID
     * @param triggerMode 触发模式
     */
    public TaskInstance executeTask(String taskId, TriggerMode triggerMode) {
        // TODO
        Task task = taskService.getTaskOrThrow(taskId);
        return executeTask(task, triggerMode);
    }

    /**
     * 通过对象ID异步执行任务
     * @param relatedId 对象ID
     * @param triggerMode 触发模式
     */
    public TaskInstance executeByRelatedId(String relatedId, TriggerMode triggerMode) {
        Task task = taskService.getDetailByRelatedId(relatedId);
        if (task == null) {
            log.error("关联ID [{}] 没有找到对应的异步执行任务", relatedId);
            throw new RuntimeException("关联ID " + relatedId + " 对应的任务不存在");
        }
        return executeTask(task, triggerMode);
    }

    /**
     * 通过任务异步执行任务
     * @param task 任务
     * @param triggerMode 触发模式
     */
    public TaskInstance executeTask(Task task, TriggerMode triggerMode) {
        String taskId = task.getTaskId();
        if (!Objects.equals(task.getStatus(), Status.ENABLE.getCode())) {
            log.error("任务 [{}] 已停用，无法执行", taskId);
            throw new RuntimeException("任务已停用，无法执行");
        }

        // 并发控制：检查是否有运行中的实例
        // TODO 是否需要抛出异常来提示
        checkRunningInstance(taskId);

        // 创建实例
        String instanceName = task.getTaskId() + "-" + System.currentTimeMillis();
        TaskInstance instance = new TaskInstance();
        instance.setInstanceName(instanceName);
        instance.setTaskId(taskId);
        instance.setInstanceRelatedId(task.getTaskRelatedId());
        instance.setStatus(InstanceStatus.PENDING.getCode());
        instance.setTriggerMode(triggerMode.getCode());
        TaskInstance taskInstance = taskInstanceService.createInstance(instance);

        // 异步执行实例
        CompletableFuture.runAsync(() -> executeAsync(task, taskInstance), taskExecutorPool);

        return instance;
    }



    // -------------------------------------------------------------------------
    // 私有方法
    // -------------------------------------------------------------------------

    /**
     * 检查任务是否有运行中的实例
     * @param taskId 任务ID
     */
    private void checkRunningInstance(String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstanceDTO> instances = taskInstanceService.getList(query);
        for (TaskInstanceDTO instance : instances) {
            int status = instance.getStatus();
            if (status == InstanceStatus.PENDING.getCode() || status == InstanceStatus.RUNNING.getCode()) {
                log.error("任务 [{}] 正在执行中，请稍后再试", taskId);
                throw new RuntimeException("任务正在执行中，请稍后再试");
            }
        }
    }

    /**
     * 异步执行任务逻辑
     * @param task 任务
     * @param instance 任务实例
     */
    // TODO TaskInstance -> TaskInstanceDTO
    private void executeAsync(Task task, TaskInstance instance) {
        String instanceId = instance.getInstanceId();

        // 1. 获取执行器
        TaskType taskType = TaskType.of(task.getTaskType());
        TaskExecutor executor = executorMap.get(taskType);
        if (executor == null) {
            log.error("未找到 [{}] 任务类型对应的执行器", taskType);
            throw new IllegalStateException("未找到任务对应的执行器，请联系管理员");
        }
        // 2. 构建执行上下文
        ExecutionContext context = ExecutionContext.builder()
                .task(task)
                .instance(instance)
                .relatedId(task.getTaskRelatedId())
                .build();

        // 3. 执行任务实例
        try {
            taskInstanceService.markRunning(instanceId);
            executor.execute(context);
            taskInstanceService.markSuccess(instanceId, "执行成功");
            executor.onSuccess(context);

            // 触发通知
            alertService.trigger(task, instance, InstanceStatus.SUCCESS);
        } catch (Exception e) {
            log.error("任务实例 [{}] 执行失败", instanceId, e);
            taskInstanceService.markFailed(instanceId, e.getMessage());
            // 触发通知
            alertService.trigger(task, instance, InstanceStatus.FAILED);
            // 回调 onFailure
            executor.onFailure(context, e);
        }
    }
}
