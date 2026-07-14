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
import java.util.Optional;
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
     * 异步执行任务：创建实例并异步执行，立即返回实例信息。
     * <p>包含并发控制：同一任务只允许一个运行中的实例。</p>
     * @param task 任务
     * @param triggerMode 触发模式
     * @return 创建的任务实例
     */
    public TaskInstance executeTask(Task task, TriggerMode triggerMode) {
        String taskId = task.getTaskId();
        if (!Objects.equals(task.getStatus(), Status.ENABLE.getCode())) {
            throw new RuntimeException("任务已停用，无法执行: " + taskId);
        }

        // 并发控制：检查是否有运行中的实例
        checkRunningInstance(taskId);

        // 创建实例（PENDING 状态，记录触发模式）
        String instanceName = task.getTaskName() + "-" + System.currentTimeMillis();
        TaskInstance instance = taskInstanceService.createInstance(
                taskId, instanceName, task.getTaskRelatedId(), InstanceStatus.PENDING, triggerMode);

        // 异步执行
        CompletableFuture.runAsync(() -> executeAsync(task, instance), taskExecutorPool);

        return instance;
    }

    /**
     * 通过任务ID异步执行任务
     */
    public TaskInstance executeTask(String taskId, TriggerMode triggerMode) {
        Task task = taskService.getTaskOrThrow(taskId);
        return executeTask(task, triggerMode);
    }

    /**
     * 通过关联ID异步执行任务。
     */
    public TaskInstance executeByRelatedId(String relatedId, TriggerMode triggerMode) {
        Task task = taskService.getDetailByRelatedId(relatedId);
        if (task == null) {
            throw new RuntimeException("关联ID " + relatedId + " 对应的任务不存在");
        }
        return executeTask(task, triggerMode);
    }

    // -------------------------------------------------------------------------
    // 私有方法
    // -------------------------------------------------------------------------

    /**
     * 检查任务是否有运行中的实例（并发控制）
     */
    private void checkRunningInstance(String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstanceDTO> instances = taskInstanceService.getList(query);
        for (TaskInstanceDTO inst : instances) {
            int status = inst.getStatus();
            if (status == InstanceStatus.PENDING.getCode() || status == InstanceStatus.RUNNING.getCode()) {
                throw new RuntimeException(
                        "任务正在执行中，请稍后再试: instanceId=" + inst.getInstanceId() + ", status=" + inst.getStatus());
            }
        }
    }

    /**
     * 异步执行任务逻辑
     */
    private void executeAsync(Task task, TaskInstance instance) {
        String taskId = task.getTaskId();
        String instanceId = instance.getInstanceId();

        try {
            // 更新状态为 RUNNING
            taskInstanceService.markRunning(instanceId);

            // 获取执行器
            TaskType taskType = TaskType.of(task.getTaskType());
            TaskExecutor executor = executorMap.get(taskType);
            if (executor == null) {
                throw new IllegalStateException("未找到任务类型对应的执行器: " + taskType);
            }

            // 构建执行上下文
            ExecutionContext context = ExecutionContext.builder()
                    .task(task)
                    .instance(instance)
                    .relatedId(task.getTaskRelatedId())
                    .build();

            // 执行
            executor.execute(context);
            taskInstanceService.markSuccess(instanceId, "执行成功");
            executor.onSuccess(context);

            // 触发告警（成功）
            try {
                alertService.trigger(task, instance, InstanceStatus.SUCCESS);
            } catch (Exception alertEx) {
                log.warn("告警触发异常(不影响主流程): instanceId={}", instanceId, alertEx);
            }

        } catch (Exception e) {
            log.error("任务执行失败: taskId={}, instanceId={}", taskId, instanceId, e);
            taskInstanceService.markFailed(instanceId, e.getMessage());

            // 触发告警（失败）
            try {
                alertService.trigger(task, instance, InstanceStatus.FAILED);
            } catch (Exception alertEx) {
                log.warn("告警触发异常(不影响主流程): instanceId={}", instanceId, alertEx);
            }

            // 尝试回调 onFailure（如果执行器存在）
            try {
                TaskType taskType = TaskType.of(task.getTaskType());
                TaskExecutor executor = executorMap.get(taskType);
                if (executor != null) {
                    ExecutionContext context = ExecutionContext.builder()
                            .task(task)
                            .instance(instance)
                            .relatedId(task.getTaskRelatedId())
                            .build();
                    executor.onFailure(context, e);
                }
            } catch (Exception callbackEx) {
                log.warn("执行器 onFailure 回调失败: instanceId={}", instanceId, callbackEx);
            }
        }
    }
}
