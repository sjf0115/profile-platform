package com.data.profile.web.service;

import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.EntityConverter;
import com.data.profile.web.dao.EntityMapper;
import com.data.profile.web.dto.EntityDTO;
import com.data.profile.web.dto.EntityRequest;
import com.data.profile.web.model.Entity;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 功能：实体服务
 * 作者：SmartSi
 * 日期：2024/7/7 15:44
 */
@Slf4j
@Service
public class EntityService {
    @Resource
    private EntityMapper entityMapper;
    @Resource
    private UserService userService;

    /**
     * 根据查询条件获取实体列表
     */
    public List<EntityDTO> getList(Entity entity) {
        List<Entity> entities = entityMapper.selectByParams(entity);
        log.info("根据查询条件获取 {} 个实体：{}", entities.size(), JSONUtils.toJsonString(entities));
        List<EntityDTO> dtos = EntityConverter.do2dtoList(entities);
        // 填充人员名称
        Map<String, String> userMap = userService.getUserNameMap();
        for (EntityDTO dto : dtos) {
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 根据实体ID获取实体详细信息
     */
    public EntityDTO getDetail(String entityId) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        if (entity == null) {
            return null;
        }
        EntityDTO dto = EntityConverter.do2dto(entity);
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        return dto;
    }

    /**
     * 创建实体
     */
    public EntityDTO create(EntityRequest request) {
        // 检查名称重复
        List<Entity> entities = entityMapper.selectByEntityName(request.getEntityName());
        if (!entities.isEmpty()) {
            throw new RuntimeException("实体已经存在，不允许重复添加");
        }
        String entityId = IDGenerator.getInstance().generate(ModelType.ENTITY);
        Entity target = entityMapper.selectByEntityId(entityId);
        if (target != null) {
            throw new RuntimeException("实体ID已经存在，不允许重复添加");
        }

        Entity entity = EntityConverter.request2do(request);
        entity.setEntityId(entityId);
        entity.setStatus(Status.ENABLE.getCode());
        entity.setSourceType(SourceType.CUSTOM.getCode());
        entity.setCreator(UserContextHolder.currentUserId());
        entity.setModifier(UserContextHolder.currentUserId());
        entityMapper.insertSelective(entity);

        return EntityConverter.do2dto(entityMapper.selectByEntityId(entityId));
    }

    /**
     * 更新实体
     */
    public int update(String entityId, EntityRequest request) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        if (entity == null) {
            throw new RuntimeException("实体不存在");
        }
        Entity update = EntityConverter.request2do(request);
        update.setEntityId(entityId);
        update.setModifier(UserContextHolder.currentUserId());
        return entityMapper.updateByEntityIdSelective(update);
    }

    /**
     * 更新实体状态
     */
    public int updateStatus(String entityId, Integer status) {
        Entity entity = new Entity();
        entity.setEntityId(entityId);
        entity.setStatus(status);
        entity.setModifier(UserContextHolder.currentUserId());
        return entityMapper.updateByEntityIdSelective(entity);
    }

    /**
     * 删除实体
     */
    public int delete(String entityId) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        if (entity == null) {
            throw new RuntimeException("实体不存在，无法删除");
        }
        if (Objects.equals(entity.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置实体不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        return entityMapper.deleteByEntityId(entityId);
    }
}
