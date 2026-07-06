package com.data.profile.web.service;

import com.data.profile.web.dao.EntityMapper;
import com.data.profile.web.model.Entity;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：实体类型服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class EntityService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private EntityMapper entityMapper;

    /**
     * 根据查询条件获取实体列表
     * @param entity 实体
     */
    public List<Entity> getList(Entity entity) {
        List<Entity> entities = entityMapper.selectByParams(entity);
        log.info("根据查询条件获取 {} 个实体: {}", entities.size(), gson.toJson(entities));
        return entities;
    }

    /**
     * 根据实体ID获取实体详细信息
     * @param entityId 实体ID
     */
    public Optional<Entity> getDetail(String entityId) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        log.info("根据实体ID获取实体详细信息: {}", gson.toJson(entity));
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    /**
     * 保存实体 新增/修改
     * @param entity 实体
     */
    public int save(Entity entity) throws RuntimeException {
        if (StringUtils.isBlank(entity.getEntityId())) {
            // 新增
            List<Entity> entities = entityMapper.selectByEntityName(entity.getEntityName());
            if (!entities.isEmpty()) {
                log.error("实体 {} 已经存在，不允许重复添加", entity.getEntityName());
                throw new RuntimeException("实体已经存在，不允许重复添加");
            }
            String entityId = IDGenerator.getInstance().generate(ModelType.ENTITY);
            Entity target = entityMapper.selectByEntityId(entityId);
            if (!Objects.equals(target, null)) {
                log.error("实体ID {} 已经存在，不允许重复添加", entityId);
                throw new RuntimeException("实体ID已经存在，不允许重复添加");
            }
            entity.setStatus(Status.ENABLE.getCode());
            entity.setEntityId(entityId);
            entity.setSourceType(SourceType.CUSTOM.getCode());
            entity.setCreator(UserContextHolder.currentUserId());
            entity.setModifier(UserContextHolder.currentUserId());
            log.info("新增实体: {}", gson.toJson(entity));
            return entityMapper.insertSelective(entity);
        } else {
            // 修改
            entity.setModifier(UserContextHolder.currentUserId());
            log.info("更新实体: {}", gson.toJson(entity));
            return entityMapper.updateByEntityIdSelective(entity);
        }
    }

    /**
     * 删除实体
     * @param entityId 实体ID
     */
    public int delete(String entityId) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        if (Objects.equals(entity, null)) {
            log.error("实体 {} 不存在，无法删除", entityId);
            throw new RuntimeException("实体不存在，无法删除");
        }
        if (Objects.equals(entity.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置实体 {} 不允许删除", entityId);
            throw new RuntimeException("内置实体不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        log.info("删除实体: {}", entityId);
        return entityMapper.deleteByEntityId(entityId);
    }
}