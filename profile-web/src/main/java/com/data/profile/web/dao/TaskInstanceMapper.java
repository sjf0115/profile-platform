package com.data.profile.web.dao;

import com.data.profile.web.model.TaskInstance;

import java.util.List;

public interface TaskInstanceMapper {
    // 查询
    TaskInstance selectByInstanceId(String instanceId); // 根据ID查询
    List<TaskInstance> selectByInstanceName(String instanceName); // 根据名字查询
    List<TaskInstance> selectByParams(TaskInstance instance); //根据参数查询
    List<TaskInstance> selectByKeyword(String keyword); // 模糊查询
    TaskInstance selectLatestByRelatedId(String instanceRelatedId); // 查询最新实例
    // 插入
    int insert(TaskInstance instance); // 插入全部
    int insertSelective(TaskInstance instance); // 选择性插入
    // 删除
    int deleteByInstanceId(String instanceId); // 根据ID删除
    // 更新
    int updateByInstanceId(TaskInstance instance); // 全部更新
    int updateByInstanceIdSelective(TaskInstance instance); // 部分更新
}