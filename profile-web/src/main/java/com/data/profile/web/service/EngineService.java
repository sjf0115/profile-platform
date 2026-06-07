package com.data.profile.web.service;

import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.utils.StringUtils;
import com.data.profile.web.dao.EngineMapper;
import com.data.profile.web.model.Engine;
import com.data.profile.web.security.RequestContext;
import com.data.profile.web.vo.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 功能：计算引擎服务
 * 作者：@Smartsi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@Service
public class EngineService {
    private static final Gson gson = new GsonBuilder().create();

    @Resource
    private EngineMapper engineMapper;

    /**
     * 根据查询条件获取引擎列表
     * @param engine 查询条件
     */
    public List<Engine> getList(Engine engine) {
        List<Engine> engines = engineMapper.selectByParams(engine);
        log.info("根据查询条件获取 {} 个引擎: {}", engines.size(), gson.toJson(engines));
        return engines;
    }

    /**
     * 根据引擎ID获取引擎详细信息
     * @param engineId 引擎ID
     */
    public Engine getDetail(String engineId) {
        Engine engine = engineMapper.selectByEngineId(engineId);
        log.info("根据引擎ID {} 获取引擎详细信息: {}", engineId, gson.toJson(engine));
        return engine;
    }

    /**
     * 获取默认引擎（全表唯一，兼容旧逻辑）
     */
    public Engine getDefaultEngine() {
        Engine engine = engineMapper.selectDefaultEngine();
        log.info("获取默认引擎: {}", gson.toJson(engine));
        return engine;
    }

    /**
     * 按 category 获取默认引擎。
     * @param engineCategory analysis | di
     */
    public Engine getDefaultEngineByCategory(String engineCategory) {
        Engine engine = engineMapper.selectDefaultEngineByCategory(engineCategory);
        log.info("按 category={} 获取默认引擎: {}", engineCategory, gson.toJson(engine));
        return engine;
    }

    /**
     * 根据引擎ID获取引擎，如果为空则返回默认引擎
     * @param engineId 引擎ID（可为空）
     */
    public Engine getEngineOrDefault(String engineId) {
        if (StringUtils.isEmpty(engineId)) {
            return getDefaultEngine();
        }
        Engine engine = engineMapper.selectByEngineId(engineId);
        return engine != null ? engine : getDefaultEngine();
    }

    /**
     * 根据引擎ID获取引擎，为空则返回指定 category 下的默认引擎。
     */
    public Engine getEngineOrDefault(String engineId, String fallbackCategory) {
        if (StringUtils.isEmpty(engineId)) {
            return getDefaultEngineByCategory(fallbackCategory);
        }
        Engine engine = engineMapper.selectByEngineId(engineId);
        return engine != null ? engine : getDefaultEngineByCategory(fallbackCategory);
    }

    /**
     * 保存引擎 新增/修改
     * @param engine 引擎
     */
    @Transactional
    public int save(Engine engine) throws RuntimeException {
        if (StringUtils.isEmpty(engine.getEngineId())) {
            // 新增
            List<Engine> engines = engineMapper.selectSimpleByEngineName(engine.getEngineName());
            if (!engines.isEmpty()) {
                throw new RuntimeException("引擎名称已经存在，不允许重复添加");
            }

            String engineId = IDGenerator.getInstance().generate(ModelType.ENGINE);
            Engine target = engineMapper.selectSimpleByEngineId(engineId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("引擎ID已经存在，不允许重复添加");
            }

            // 检查引擎类型
            String engineType = engine.getEngineType();
            if (StringUtils.isEmpty(engineType)) {
                throw new RuntimeException("引擎类型不能为空");
            }

            engine.setStatus(Status.ENABLE.getCode());
            engine.setEngineId(engineId);
            engine.setSourceType(SourceType.CUSTOM.getCode());

            // 如果设置为默认，先将同 category 下其他引擎的默认标志取消
            if (engine.getIsDefault() != null && engine.getIsDefault() == 1) {
                clearDefaultRespectingCategory(engine.getEngineCategory());
            } else {
                engine.setIsDefault(0);
            }

            engine.setCreator(RequestContext.currentUserId());
            engine.setModifier(RequestContext.currentUserId());

            log.info("新增引擎: {}", gson.toJson(engine));
            return engineMapper.insertSelective(engine);
        } else {
            // 修改
            // 如果设置为默认，先将同 category 下其他引擎的默认标志取消
            if (engine.getIsDefault() != null && engine.getIsDefault() == 1) {
                clearDefaultRespectingCategory(engine.getEngineCategory());
            }

            engine.setModifier(RequestContext.currentUserId());
            log.info("更新引擎: {}", gson.toJson(engine));
            return engineMapper.updateByEngineIdSelective(engine);
        }
    }

    /**
     * 删除引擎
     * @param engineId 引擎ID
     */
    public int delete(String engineId) {
        Engine engine = engineMapper.selectByEngineId(engineId);
        if (Objects.equals(engine, null)) {
            log.error("引擎 {} 不存在，无法删除", engineId);
            throw new RuntimeException("引擎不存在，无法删除");
        }

        if (Objects.equals(engine.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置引擎 {} 不允许删除", engineId);
            throw new RuntimeException("内置引擎不允许删除");
        }

        // TODO: 检查是否有数据集使用该引擎

        log.info("删除引擎: {}", engineId);
        return engineMapper.deleteByEngineId(engineId);
    }

    /**
     * 设置默认引擎
     * @param engineId 引擎ID
     */
    @Transactional
    public void setDefaultEngine(String engineId) {
        Engine engine = engineMapper.selectByEngineId(engineId);
        if (Objects.equals(engine, null)) {
            throw new RuntimeException("引擎不存在");
        }

        // 清除同 category 下的默认标志
        clearDefaultRespectingCategory(engine.getEngineCategory());

        // 设置新的默认引擎
        engineMapper.setDefaultByEngineId(engineId);

        log.info("设置默认引擎: {} (category={})", engineId, engine.getEngineCategory());
    }

    /**
     * 清除默认标志：category 不空时按 category 清理，否则全表清理（兼容旧数据）。
     */
    private void clearDefaultRespectingCategory(String engineCategory) {
        if (StringUtils.isEmpty(engineCategory)) {
            engineMapper.clearAllDefault();
        } else {
            engineMapper.clearAllDefaultByCategory(engineCategory);
        }
    }

    /**
     * 获取所有支持的引擎类型
     * 当前只支持 ClickHouse
     */
    public List<Item> getEngineTypeList() {
        List<Item> items = new ArrayList<>();
        // 目前只支持 ClickHouse
        items.add(new Item("clickhouse", "ClickHouse"));
        log.info("获取所有支持的引擎类型: {}", gson.toJson(items));
        return items;
    }
}