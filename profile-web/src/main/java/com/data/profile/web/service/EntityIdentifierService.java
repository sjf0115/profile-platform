package com.data.profile.web.service;

import com.data.profile.web.dao.EntityIdentifierMapper;
import com.data.profile.web.model.EntityIdentifier;
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
 * 功能：实体标识服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class EntityIdentifierService {
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private EntityIdentifierMapper entityIdentifierMapper;

    /**
     * 根据查询条件获取实体标识列表
     * @param entityIdentifier 实体标识
     */
    public List<EntityIdentifier> getList(EntityIdentifier entityIdentifier) {
        List<EntityIdentifier> entityIdentifiers = entityIdentifierMapper.selectByParams(entityIdentifier);
        log.info("根据查询条件获取 {} 个实体标识: {}", entityIdentifiers.size(), gson.toJson(entityIdentifiers));
        return entityIdentifiers;
    }

    /**
     * 根据实体标识ID获取实体标识详细信息
     * @param entityIdentifierId 实体标识ID
     */
    public Optional<EntityIdentifier> getDetail(String entityIdentifierId) {
        EntityIdentifier entityIdentifier = entityIdentifierMapper.selectByEntityIdentifierId(entityIdentifierId);
        log.info("根据实体标识ID获取实体标识详细信息: {}", gson.toJson(entityIdentifier));
        if (entityIdentifier == null) {
            return Optional.empty();
        }
        return Optional.of(entityIdentifier);
    }

    /**
     * 保存实体标识 新增/修改
     * @param entityIdentifier 实体标识
     */
    public int save(EntityIdentifier entityIdentifier) throws RuntimeException {
        if (StringUtils.isBlank(entityIdentifier.getEntityIdentifierId())) {
            // 新增
            List<EntityIdentifier> entityIdentifiers = entityIdentifierMapper.selectSimpleByEntityIdentifierName(entityIdentifier.getEntityIdentifierName());
            if (!entityIdentifiers.isEmpty()) {
                log.error("实体标识 {} 已经存在，不允许重复添加", entityIdentifier.getEntityName());
                throw new RuntimeException("实体标识已经存在，不允许重复添加");
            }
            String entityIdentifierId = IDGenerator.getInstance().generate(ModelType.ENTITY_IDENTIFIER);
            EntityIdentifier target = entityIdentifierMapper.selectSimpleByEntityIdentifierId(entityIdentifierId);
            if (!Objects.equals(target, null)) {
                log.error("实体标识ID {} 已经存在，不允许重复添加", entityIdentifierId);
                throw new RuntimeException("实体标识ID已经存在，不允许重复添加");
            }
            entityIdentifier.setEntityIdentifierId(entityIdentifierId);
            entityIdentifier.setStatus(Status.ENABLE.getCode());
            entityIdentifier.setSourceType(SourceType.CUSTOM.getCode());
            entityIdentifier.setCreator(UserContextHolder.currentUserId());
            entityIdentifier.setModifier(UserContextHolder.currentUserId());
            log.info("新增实体标识: {}", gson.toJson(entityIdentifier));
            return entityIdentifierMapper.insertSelective(entityIdentifier);
        } else {
            // 修改
            entityIdentifier.setModifier(UserContextHolder.currentUserId());
            log.info("更新实体标识: {}", gson.toJson(entityIdentifier));
            return entityIdentifierMapper.updateByEntityIdentifierIdSelective(entityIdentifier);
        }
    }

    /**
     * 删除实体标识
     * @param entityIdentifierId 实体标识ID
     */
    public int delete(String entityIdentifierId) {
        EntityIdentifier entityIdentifier = entityIdentifierMapper.selectSimpleByEntityIdentifierId(entityIdentifierId);
        if (Objects.equals(entityIdentifier, null)) {
            log.error("实体标识 {} 不存在，无法删除", entityIdentifierId);
            throw new RuntimeException("实体标识不存在，无法删除");
        }
        if (Objects.equals(entityIdentifier.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置实体标识 {} 不允许删除", entityIdentifierId);
            throw new RuntimeException("内置实体标识不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        log.info("删除实体标识: {}", entityIdentifierId);
        return entityIdentifierMapper.deleteByEntityIdentifierId(entityIdentifierId);
    }
}