package com.data.profile.dao;

import com.data.profile.model.Task;

import java.util.List;

public interface TaskMapper {
    // 查询
    Task selectByTaskId(String taskId); // 根据ID查询
    Task selectByRelatedId(String relatedId); // 根据关联ID查询
    List<Task> selectByTaskName(String taskName); // 根据名字查询
    List<Task> selectByParams(Task task); //根据参数查询
    List<Task> selectByKeyword(String keyword); // 模糊查询
    // 插入
    int insert(Task task); // 插入全部
    int insertSelective(Task task); // 选择性插入
    // 删除
    int deleteByTaskId(String taskId); // 根据ID删除
    int deleteByRelatedId(String relatedId); // 根据关联ID删除

    // 更新
    int updateByTaskId(Task task); // 全部更新
    int updateByTaskIdSelective(Task task); // 部分更新
}