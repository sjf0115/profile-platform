package com.data.profile.web.service;

import com.data.profile.common.enums.InstanceStatus;
import com.data.profile.common.enums.SchedulerJobType;
import com.data.profile.common.enums.Status;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.DiEngineService;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.task.GroupTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 功能：任务执行服务
 * <p>负责任务执行的完整协调流程：创建实例 → 分发执行 → 更新状态。</p>
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
    private DiEngineService diEngineService;
    @Resource
    private AnalysisEngineService analysisEngineService;
    @Resource
    private DatasetService datasetService;
    @Resource
    private GroupTask groupTask;
    @Resource
    private GroupService groupService;

    /**
     * 执行任务：创建实例并根据任务类型分发执行逻辑。
     * @param taskId 任务ID
     * @return 创建的任务实例
     */
    public TaskInstance executeTask(String taskId) {
        Task task = taskService.getDetail(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
        if (task.getStatus() != Status.ENABLE.getCode()) {
            throw new RuntimeException("任务已停用，无法执行: " + taskId);
        }

        // 创建实例
        String instanceName = task.getTaskName() + "-" + System.currentTimeMillis();
        TaskInstance instance = taskInstanceService.createInstance(taskId, instanceName, task.getTaskRelatedId());

        // 执行并更新状态
        try {
            dispatch(task);
            taskInstanceService.markSuccess(instance.getInstanceId(), "执行成功");
        } catch (Exception e) {
            log.error("任务执行失败: taskId={}, instanceId={}", taskId, instance.getInstanceId(), e);
            taskInstanceService.markFailed(instance.getInstanceId(), e.getMessage());
            updateRelatedInstanceStatus(task, InstanceStatus.FAILED.getCode(), e.getMessage());
            throw new RuntimeException("任务执行失败: " + e.getMessage(), e);
        }

        updateRelatedInstanceStatus(task, InstanceStatus.SUCCESS.getCode(), "执行成功");
        return instance;
    }

    /**
     * 通过关联ID执行任务。
     */
    public TaskInstance executeByRelatedId(String relatedId) {
        Task task = taskService.getDetailByRelatedId(relatedId);
        if (task == null) {
            throw new RuntimeException("关联ID " + relatedId + " 对应的任务不存在");
        }
        return executeTask(task.getTaskId());
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 根据任务类型分发执行逻辑。
     */
    private void dispatch(Task task) throws Exception {
        int taskType = task.getTaskType();
        if (taskType == SchedulerJobType.IMPORT.getCode()) {
            // 数据集同步
            diEngineService.executeDatasetSync(task.getTaskRelatedId());
        } else if (taskType == SchedulerJobType.GROUP.getCode()) {
            // 群组圈选
            groupTask.executeGroupSelection(task.getTaskRelatedId());
        } else if (taskType == SchedulerJobType.EXPORT.getCode()) {
            // 群组投递
            throw new UnsupportedOperationException("群组投递任务暂未实现");
        } else {
            throw new IllegalStateException("未知的任务类型: " + taskType);
        }
    }

    /**
     * 回写关联对象（数据集/群组等）的最新实例状态。
     */
    private void updateRelatedInstanceStatus(Task task, int status, String msg) {
        int taskType = task.getTaskType();
        if (taskType == SchedulerJobType.IMPORT.getCode()) {
            datasetService.updateInstanceStatus(task.getTaskRelatedId(), status, msg);
        } else if (taskType == SchedulerJobType.GROUP.getCode()) {
            groupService.updateInstanceStatus(task.getTaskRelatedId(), status, msg);
        }
        // 其他类型后续扩展
    }
}