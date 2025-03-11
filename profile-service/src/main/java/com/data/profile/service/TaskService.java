package com.data.profile.service;

import com.data.profile.dao.TaskMapper;
import com.data.profile.model.Task;
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
public class TaskService {
    private static Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private TaskMapper taskMapper;

    /**
     * 根据查询条件获取任务列表
     * @param task
     * @return
     */
    public List<Task> getList(Task task) {
        List<Task> tasks = taskMapper.selectByParams(task);
        return tasks;
    }

    /**
     * 根据任务ID获取任务详细信息
     * @param taskId
     * @return
     */
    public Optional<Task> getDetail(String taskId) {
        Task task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of(task);
    }

    /**
     * 创建任务
     * @param taskName
     * @return
     */
    public int add(String taskName) {
        return -1;
    }

    /**
     * 根据任务ID删除任务
     * @param taskId
     * @return
     */
    public int delete(String taskId) {
        return -1;
    }
}