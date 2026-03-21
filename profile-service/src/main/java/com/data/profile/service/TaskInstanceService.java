package com.data.profile.service;

import com.data.profile.dao.TaskInstanceMapper;
import com.data.profile.dao.TaskMapper;
import com.data.profile.model.Task;
import com.data.profile.model.TaskInstance;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
public class TaskInstanceService {
    @Resource
    private TaskInstanceMapper instanceMapper;

    /**
     * 根据查询条件获取任务实例列表
     * @param instance
     * @return
     */
    public List<TaskInstance> getList(TaskInstance instance) {
        List<TaskInstance> instances = instanceMapper.selectByParams(instance);
        return instances;
    }

    /**
     * 根据任务实例ID获取任务实例详细信息
     * @param instanceId
     * @return
     */
    public Optional<TaskInstance> getDetail(String instanceId) {
        TaskInstance instance = instanceMapper.selectByInstanceId(instanceId);
        if (instance == null) {
            return Optional.empty();
        }
        return Optional.of(instance);
    }

    /**
     * 创建任务实例
     * @param instanceName
     * @return
     */
    public int add(String instanceName) {
        return -1;
    }

    /**
     * 根据任务实例ID删除任务实例
     * @param instanceId
     * @return
     */
    public int delete(String instanceId) {
        return -1;
    }
}