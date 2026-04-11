package com.data.profile.web.service;

import com.data.profile.web.dao.GroupMapper;
import com.data.profile.web.model.EntityIdentifier;
import com.data.profile.web.model.Group;
import com.data.profile.web.model.Task;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.enums.TaskType;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 功能：群组服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class GroupService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private GroupMapper groupMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskInstanceService taskInstanceService;
    @Autowired
    private EntityIdentifierService entityIdentifierService;

    /**
     * 根据查询条件获取群组列表
     * @param group 群组
     */
    public List<Group> getList(Group group) {
        List<Group> groups = groupMapper.selectByParams(group);
        List<Group> targets = groups.stream().map(target -> {
            // 获取群组实体信息
            String entityIdentifierId = target.getEntityIdentifierId();
            Optional<EntityIdentifier> entityIdentifierOp = entityIdentifierService.getDetail(entityIdentifierId);
            if (entityIdentifierOp.isPresent()) {
                EntityIdentifier entityIdentifier = entityIdentifierOp.get();
                target.setEntityId(entityIdentifier.getEntityId());
                target.setEntityName(entityIdentifier.getEntityName());
                target.setEntityIdentifierName(entityIdentifier.getEntityIdentifierName());
            }
            return target;
        }).collect(Collectors.toList());
        log.info("根据查询条件获取 {} 个群组: {}", groups.size(), gson.toJson(groups));
        return targets;
    }

    /**
     * 根据群组ID获取群组详细信息
     * @param groupId 群组ID
     */
    public Optional<Group> getDetail(String groupId) {
        // 获取群组信息
        Group group = groupMapper.selectByGroupId(groupId);
        if (group == null) {
            return Optional.empty();
        }

        // 获取群组实体信息
        String entityIdentifierId = group.getEntityIdentifierId();
        Optional<EntityIdentifier> entityIdentifierOp = entityIdentifierService.getDetail(entityIdentifierId);
        if (entityIdentifierOp.isPresent()) {
            EntityIdentifier entityIdentifier = entityIdentifierOp.get();
            group.setEntityId(entityIdentifier.getEntityId());
            group.setEntityName(entityIdentifier.getEntityName());
            group.setEntityIdentifierName(entityIdentifier.getEntityIdentifierName());
        }

        // 获取群组调度配置
        Task task = taskService.getDetailByRelatedId(groupId);
        if (!Objects.equals(task, null)) {
            group.setTaskId(task.getTaskId());
            group.setTriggerType(task.getTriggerType());
            group.setTriggerCron(task.getTriggerCron());
            group.setTriggerStartTime(task.getTriggerStartTime());
            group.setTriggerEndTime(task.getTriggerEndTime());
            // Todo 获取群组调度任务最新实例
        }
        log.info("根据群组ID获取群组详细信息: {}", gson.toJson(group));
        return Optional.of(group);
    }

    /**
     * 创建群组
     * @param group 群组
     */
    @Transactional
    public int create(Group group) throws RuntimeException {
        String groupName = group.getGroupName();
        // Todo 保存时执行一次预估人数
        group.setGroupCount(100);

        // 群组处理
        List<Group> groups = groupMapper.selectSimpleByGroupName(groupName);
        if (!groups.isEmpty()) {
            log.error("群组 {} 已经存在，不允许重复添加", groupName);
            throw new RuntimeException("群组已经存在，不允许重复添加");
        }
        String groupId = IDGenerator.getInstance().generate(ModelType.GROUP);
        Group target = groupMapper.selectSimpleByGroupId(groupId);
        if (!Objects.equals(target, null)) {
            log.error("群组ID {} 已经存在，不允许重复添加", groupId);
            throw new RuntimeException("群组ID已经存在，不允许重复添加");
        }
        group.setGroupId(groupId);
        group.setSourceType(SourceType.CUSTOM.getCode());
        group.setGroupStatus(Status.ENABLE.getCode());
        group.setOwner(RequestContext.currentUserId());
        group.setCreator(RequestContext.currentUserId());
        group.setModifier(RequestContext.currentUserId());

        // 创建调度任务
        Task task = Task.builder()
                .taskName(groupName + "调度任务")
                .taskType(TaskType.GROUP_CREATE.getCode())
                .taskRelatedId(groupId)
                .triggerType(group.getTriggerType())
                .triggerCron(group.getTriggerCron())
                .triggerStartTime(group.getTriggerStartTime())
                .triggerEndTime(group.getTriggerEndTime())
                .build();
        taskService.create(task);

        log.info("新增群组: {}", gson.toJson(group));
        return groupMapper.insertSelective(group);
    }

    /**
     * 修改群组
     * @param group 群组
     */
    @Transactional
    public int update(Group group) {
        String groupId = group.getGroupId();
        // Todo 保存时执行一次预估人数
        group.setGroupCount(100);

        // 修改群组
        group.setModifier(RequestContext.currentUserId());

        // 修改调度任务
        Task task = Task.builder()
                .taskId(group.getTaskId()) // 根据TaskId修改
                .taskName(group.getGroupName() + "调度任务")
                .taskType(TaskType.GROUP_CREATE.getCode())
                .taskRelatedId(groupId)
                .triggerType(group.getTriggerType())
                .triggerCron(group.getTriggerCron())
                .triggerStartTime(group.getTriggerStartTime())
                .triggerEndTime(group.getTriggerEndTime())
                .build();
        taskService.update(task);

        log.info("更新群组: {}", gson.toJson(group));
        return groupMapper.updateByGroupIdSelective(group);
    }

    /**
     * 删除群组
     * @param groupId 群组ID
     */
    @Transactional
    public int delete(String groupId) {
        Group group = groupMapper.selectSimpleByGroupId(groupId);
        if (Objects.equals(group, null)) {
            log.error("群组 {} 不存在，无法删除", groupId);
            throw new RuntimeException("群组不存在，无法删除");
        }
        if (Objects.equals(group.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置群组 {} 不允许删除", groupId);
            throw new RuntimeException("内置群组不允许删除");
        }

        // 删除调度任务
        int result = taskService.deleteByRelatedId(groupId);

        // TODO 检查依赖确保无下游使用
        log.info("删除群组: {}", groupId);
        return groupMapper.deleteByGroupId(groupId);
    }
}
