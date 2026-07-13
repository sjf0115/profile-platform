package com.data.profile.web.service;

import com.data.profile.web.converter.EntityIdentifierConverter;
import com.data.profile.web.dao.EntityIdentifierMapper;
import com.data.profile.web.dto.EntityIdentifierDTO;
import com.data.profile.web.dto.EntityIdentifierRequest;
import com.data.profile.web.model.EntityIdentifier;
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
 * 功能：实体标识服务
 * 作者：SmartSi
 * 日期：2024/7/7 15:44
 */
@Slf4j
@Service
public class EntityIdentifierService {
    @Resource
    private EntityIdentifierMapper entityIdentifierMapper;
    @Resource
    private UserService userService;

    /**
     * 根据查询条件获取实体标识列表
     */
    public List<EntityIdentifierDTO> getList(EntityIdentifier entityIdentifier) {
        List<EntityIdentifier> entityIdentifiers = entityIdentifierMapper.selectByParams(entityIdentifier);
        log.info("根据查询条件获取 {} 个实体标识", entityIdentifiers.size());
        List<EntityIdentifierDTO> dtos = EntityIdentifierConverter.do2dtoList(entityIdentifiers);
        // 填充人员名称
        Map<String, String> userMap = userService.getUserNameMap();
        for (EntityIdentifierDTO dto : dtos) {
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 根据实体标识ID获取实体标识详细信息
     */
    public EntityIdentifierDTO getDetail(String entityIdentifierId) {
        EntityIdentifier entityIdentifier = entityIdentifierMapper.selectByEntityIdentifierId(entityIdentifierId);
        if (entityIdentifier == null) {
            return null;
        }
        EntityIdentifierDTO dto = EntityIdentifierConverter.do2dto(entityIdentifier);
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        return dto;
    }

    /**
     * 创建实体标识
     */
    public EntityIdentifierDTO create(EntityIdentifierRequest request) {
        // 检查名称重复
        List<EntityIdentifier> existing = entityIdentifierMapper.selectSimpleByEntityIdentifierName(request.getEntityIdentifierName());
        if (!existing.isEmpty()) {
            throw new RuntimeException("实体标识已经存在，不允许重复添加");
        }
        String entityIdentifierId = IDGenerator.getInstance().generate(ModelType.ENTITY_IDENTIFIER);
        EntityIdentifier target = entityIdentifierMapper.selectSimpleByEntityIdentifierId(entityIdentifierId);
        if (target != null) {
            throw new RuntimeException("实体标识ID已经存在，不允许重复添加");
        }

        EntityIdentifier entityIdentifier = EntityIdentifierConverter.request2do(request);
        entityIdentifier.setEntityIdentifierId(entityIdentifierId);
        entityIdentifier.setStatus(Status.ENABLE.getCode());
        entityIdentifier.setSourceType(SourceType.CUSTOM.getCode());
        entityIdentifier.setCreator(UserContextHolder.currentUserId());
        entityIdentifier.setModifier(UserContextHolder.currentUserId());
        entityIdentifierMapper.insertSelective(entityIdentifier);

        return EntityIdentifierConverter.do2dto(entityIdentifierMapper.selectByEntityIdentifierId(entityIdentifierId));
    }

    /**
     * 更新实体标识
     */
    public int update(String entityIdentifierId, EntityIdentifierRequest request) {
        EntityIdentifier entityIdentifier = entityIdentifierMapper.selectSimpleByEntityIdentifierId(entityIdentifierId);
        if (entityIdentifier == null) {
            throw new RuntimeException("实体标识不存在");
        }
        EntityIdentifier update = EntityIdentifierConverter.request2do(request);
        update.setEntityIdentifierId(entityIdentifierId);
        update.setModifier(UserContextHolder.currentUserId());
        return entityIdentifierMapper.updateByEntityIdentifierIdSelective(update);
    }

    /**
     * 更新实体标识状态
     */
    public int updateStatus(String entityIdentifierId, Integer status) {
        EntityIdentifier entityIdentifier = new EntityIdentifier();
        entityIdentifier.setEntityIdentifierId(entityIdentifierId);
        entityIdentifier.setStatus(status);
        entityIdentifier.setModifier(UserContextHolder.currentUserId());
        return entityIdentifierMapper.updateByEntityIdentifierIdSelective(entityIdentifier);
    }

    /**
     * 删除实体标识
     */
    public int delete(String entityIdentifierId) {
        EntityIdentifier entityIdentifier = entityIdentifierMapper.selectSimpleByEntityIdentifierId(entityIdentifierId);
        if (entityIdentifier == null) {
            throw new RuntimeException("实体标识不存在，无法删除");
        }
        if (Objects.equals(entityIdentifier.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置实体标识不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        return entityIdentifierMapper.deleteByEntityIdentifierId(entityIdentifierId);
    }
}
