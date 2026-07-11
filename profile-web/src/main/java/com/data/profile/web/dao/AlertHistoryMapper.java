package com.data.profile.web.dao;

import com.data.profile.web.model.AlertHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertHistoryMapper {

    int insertSelective(AlertHistory history);

    List<AlertHistory> selectByTaskId(@Param("taskId") String taskId);

    List<AlertHistory> selectByParams(@Param("taskId") String taskId,
                                      @Param("instanceId") String instanceId);
}
