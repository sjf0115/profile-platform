package com.data.profile.dao;

import com.data.profile.model.TaskInstance;

public interface TaskInstanceMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TaskInstance row);

    int insertSelective(TaskInstance row);

    TaskInstance selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TaskInstance row);

    int updateByPrimaryKey(TaskInstance row);
}