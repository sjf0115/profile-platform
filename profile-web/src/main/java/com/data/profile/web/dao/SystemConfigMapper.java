package com.data.profile.web.dao;

import com.data.profile.web.model.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemConfigMapper {

    /**
     * 查询指定分组所有配置
     */
    List<SystemConfig> selectByGroup(@Param("configGroup") String configGroup);

    /**
     * 查询全部配置
     */
    List<SystemConfig> selectAll();

    /**
     * 查询单个配置
     */
    SystemConfig selectByKey(@Param("configGroup") String configGroup, @Param("configKey") String configKey);

    /**
     * 存在则更新，不存在则插入
     */
    int upsert(SystemConfig config);

    /**
     * 批量 upsert
     */
    int batchUpsert(@Param("list") List<SystemConfig> configs);

    /**
     * 删除整组配置
     */
    int deleteByGroup(@Param("configGroup") String configGroup);
}
