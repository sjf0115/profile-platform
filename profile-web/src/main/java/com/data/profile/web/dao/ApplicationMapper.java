package com.data.profile.web.dao;

import com.data.profile.web.model.Application;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApplicationMapper {
    // 查询
    Application selectByAppKey(String appKey); // 根据 appKey 查询

    Application selectById(Long id); // 根据 ID 查询

    List<Application> selectByParams(Application application); // 根据参数查询

    List<Application> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Application application);

    int insertSelective(Application application);

    // 删除
    int deleteById(Long id);

    // 更新
    int updateByIdSelective(Application application);

    int updateById(Application application);
}
