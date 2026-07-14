package com.data.profile.web.service;

import com.data.profile.common.enums.InstanceStatus;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.TriggerMode;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.converter.TaskConverter;
import com.data.profile.web.converter.TaskInstanceConverter;
import com.data.profile.web.dao.TaskInstanceMapper;
import com.data.profile.web.dao.TaskMapper;
import com.data.profile.web.dto.TaskDTO;
import com.data.profile.web.dto.TaskInstanceDTO;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：任务实例服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class TaskInstanceService {
    @Resource
    private TaskInstanceMapper instanceMapper;
    @Resource
    private TaskService taskService;
    @Resource
    private UserService userService;

    /**
     * 根据查询条件获取任务实例列表（返回 DTO，供 Controller 使用）
     */
    public List<TaskInstanceDTO> getList(TaskInstance instance) {
        log.info("查询任务实例参数: status={}, taskId={}, startTimeBegin={}, startTimeEnd={}",
                instance.getStatus(), instance.getTaskId(), instance.getStartTimeBegin(), instance.getStartTimeEnd());
        List<TaskInstance> taskInstances = instanceMapper.selectByParams(instance);
        log.info("根据查询条件获取到 {} 个任务执行实例", taskInstances.size());
        List<TaskInstanceDTO> dtos = TaskInstanceConverter.do2dtoList(taskInstances);
        Map<String, String> userMap = userService.getUserNameMap();
        for (TaskInstanceDTO dto : dtos) {
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 根据任务实例ID获取任务实例详细信息
     */
    public TaskInstanceDTO getDetail(String instanceId) {
        TaskInstance instance = instanceMapper.selectByInstanceId(instanceId);
        if (instance == null) {
            return null;
        }
        TaskInstanceDTO dto = TaskInstanceConverter.do2dto(instance);
        // 填充用户名称
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        // 填充关联任务
        TaskDTO taskDTO = taskService.getDetail(instance.getTaskId());
        dto.setTask(taskDTO);
        return dto;
    }

    /**
     * 根据关联ID获取最新任务实例。
     * <p>用于查询业务实体（数据集/群组）的最新执行状态。</p>
     */
    public TaskInstance getLatestByRelatedId(String relatedId) {
        return instanceMapper.selectLatestByRelatedId(relatedId);
    }

    /**
     * 创建任务实例。
     *
     * @param taskId            关联的任务ID
     * @param instanceName      实例名称
     * @param instanceRelatedId 实例关联ID（如 datasetId）
     * @param initialStatus     初始状态（PENDING 或 RUNNING）
     * @param triggerMode       触发模式
     * @return 创建后的实例
     */
    public TaskInstance createInstance(String taskId, String instanceName, String instanceRelatedId, InstanceStatus initialStatus, TriggerMode triggerMode) {
        String instanceId = IDGenerator.getInstance().generate(ModelType.INSTANCE);
        long startTime = System.currentTimeMillis();

        TaskInstance instance = new TaskInstance();
        instance.setInstanceId(instanceId);
        instance.setInstanceName(instanceName);
        instance.setTaskId(taskId);
        instance.setInstanceRelatedId(instanceRelatedId);
        instance.setStatus(initialStatus.getCode());
        instance.setTriggerMode(triggerMode != null ? triggerMode.getCode() : null);
        instance.setStartTime(startTime);
        instance.setEndTime(0L);
        instance.setDuration(0L);
        instance.setMessage("");
        instance.setCreator(UserContextHolder.currentUserId());
        instance.setModifier(UserContextHolder.currentUserId());

        instanceMapper.insertSelective(instance);
        log.info("创建任务实例: instanceId={}, taskId={}, relatedId={}, status={}, triggerMode={}", instanceId, taskId, instanceRelatedId, initialStatus, triggerMode);
        return instance;
    }

    /**
     * 创建任务实例（默认为 RUNNING 状态，向后兼容）。
     */
    public TaskInstance createInstance(String taskId, String instanceName, String instanceRelatedId) {
        return createInstance(taskId, instanceName, instanceRelatedId, InstanceStatus.RUNNING, null);
    }

    /**
     * 更新任务实例为运行中状态（被异步执行器调用）。
     */
    public void markRunning(String instanceId) {
        TaskInstance instance = instanceMapper.selectByInstanceId(instanceId);
        if (instance == null) {
            return;
        }
        instance.setStatus(InstanceStatus.RUNNING.getCode());
        instanceMapper.updateByInstanceIdSelective(instance);
        log.info("任务实例运行中: {}", instanceId);
    }

    /**
     * 更新任务实例为成功状态。
     */
    public void markSuccess(String instanceId, String message) {
        TaskInstance instance = instanceMapper.selectByInstanceId(instanceId);
        if (instance == null) {
            return;
        }
        long endTime = System.currentTimeMillis();
        instance.setStatus(InstanceStatus.SUCCESS.getCode());
        instance.setEndTime(endTime);
        instance.setDuration(endTime - instance.getStartTime());
        instance.setMessage(message != null ? message : "");
        instanceMapper.updateByInstanceIdSelective(instance);
        log.info("任务实例 [{}] 运行成功，耗时 {} ms", instanceId, instance.getDuration());
    }

    /**
     * 更新任务实例为失败状态。
     */
    public void markFailed(String instanceId, String errorMsg) {
        TaskInstance instance = instanceMapper.selectByInstanceId(instanceId);
        if (instance == null) {
            return;
        }
        long endTime = System.currentTimeMillis();
        instance.setStatus(InstanceStatus.FAILED.getCode());
        instance.setEndTime(endTime);
        instance.setDuration(endTime - instance.getStartTime());
        instance.setMessage(errorMsg != null ? errorMsg : "");
        instanceMapper.updateByInstanceIdSelective(instance);
        log.info("任务实例 [{}] 运行失败，耗时 {} ms, 失败原因为 {}", instanceId, instance.getDuration(), errorMsg);
    }

    /**
     * 根据实例ID删除单个实例
     */
    public int delete(String instanceId) {
        int count = instanceMapper.deleteByInstanceId(instanceId);
        log.info("删除任务实例: instanceId={}, count={}", instanceId, count);
        return count;
    }

    /**
     * 根据任务ID删除所有实例
     */
    public int deleteByTaskId(String taskId) {
        TaskInstance query = new TaskInstance();
        query.setTaskId(taskId);
        List<TaskInstance> instances = instanceMapper.selectByParams(query);
        int count = 0;
        for (TaskInstance instance : instances) {
            count += instanceMapper.deleteByInstanceId(instance.getInstanceId());
        }
        log.info("删除任务 {} 的所有实例: count={}", taskId, count);
        return count;
    }
}