package com.data.profile.web.dao;

import com.data.profile.web.model.Engine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EngineMapper {
    //---------------------------------------------------------------------
    // 1. 引擎信息

    // 查询
    Engine selectSimpleByEngineId(String engineId); // 根据ID查询

    List<Engine> selectSimpleByEngineName(String engineName); // 根据名字查询

    List<Engine> selectSimpleByParams(Engine engine); // 根据参数查询

    List<Engine> selectSimpleByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Engine engine); // 插入全部

    int insertSelective(Engine engine); // 选择性插入

    // 删除
    int deleteByEngineId(String engineId); // 根据ID删除

    // 更新
    int updateByEngineIdSelective(Engine engine);

    int updateByEngineId(Engine engine);

    //---------------------------------------------------------------------
    // 2. 默认引擎相关

    // 查询默认引擎
    Engine selectDefaultEngine();

    // 清除所有默认引擎标志
    int clearAllDefault();

    // 设置默认引擎
    int setDefaultByEngineId(@Param("engineId") String engineId);

    //---------------------------------------------------------------------
    // 3. 关联信息（包含完整字段）

    Engine selectByEngineId(String engineId); // 根据ID查询

    List<Engine> selectByEngineName(String engineName); // 根据名字查询

    List<Engine> selectByParams(Engine engine); // 根据参数查询

    List<Engine> selectByKeyword(String keyword); // 模糊查询
}
