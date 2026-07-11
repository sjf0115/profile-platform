package com.data.profile.web.dao;

import com.data.profile.web.model.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApplicationMapper {
    // 查询
    Application selectByAppKey(String appKey); // 根据 appKey 查询

    List<Application> selectByParams(Application application); // 根据参数查询

    List<Application> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Application application);

    int insertSelective(Application application);

    // 删除
    int deleteById(Long id);

    int deleteByAppKey(String appKey);

    // 更新
    int updateByIdSelective(Application application);

    int updateById(Application application);

    int updateByAppKeySelective(Application application);

    // 检查应用名称是否重复（排除自身）
    int countByAppName(@Param("appName") String appName, @Param("excludeAppKey") String excludeAppKey);
}
