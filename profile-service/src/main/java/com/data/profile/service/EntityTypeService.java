package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.EntityTypeMapper;
import com.data.profile.model.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class EntityTypeService {
    private static Logger LOG = LoggerFactory.getLogger(EntityTypeService.class);

    @Resource
    private EntityTypeMapper entityTypeMapper;

    /**
     * 根据查询条件获取实体类型列表
     * @param entityType
     * @return
     */
    public List<EntityType> getList(EntityType entityType) {
        List<EntityType> entityTypes = entityTypeMapper.selectByParams(entityType);
        return entityTypes;
    }

    /**
     * 根据实体ID获取实体类型详细信息
     * @param entityTypeId
     * @return
     */
    public Optional<EntityType> getDetail(String entityTypeId) {
        EntityType entityType = entityTypeMapper.selectByEntityTypeId(entityTypeId);
        if (entityType == null) {
            return Optional.empty();
        }
        return Optional.of(entityType);
    }

    /**
     * 保存实体类型 新增/修改
     * @param entityType
     * @return
     * @throws RuntimeException
     */
    public int save(EntityType entityType) throws RuntimeException {
        if (StringUtils.isBlank(entityType.getEntityTypeId())) {
            // 新增
            List<EntityType> entityTypes = entityTypeMapper.selectByEntityTypeName(entityType.getEntityTypeName());
            if (entityTypes.size() > 0) {
                throw new RuntimeException("实体类型已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String entityTypeId = IDGenerator.generate(ModelType.ENTITY_TYPE);
            EntityType target = entityTypeMapper.selectByEntityTypeId(entityTypeId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("实体类型ID已经存在，不允许重复添加");
            }
            entityType.setStatus(Status.ENABLE.getCode());
            entityType.setEntityTypeId(entityTypeId);
            entityType.setSourceType(SourceType.CUSTOM.getCode());
            entityType.setCreator(RequestContext.currentUserId());
            entityType.setModifier(RequestContext.currentUserId());
            int result = entityTypeMapper.insertSelective(entityType);
            return result;
        } else {
            // 修改
            entityType.setModifier(RequestContext.currentUserId());
            int result = entityTypeMapper.updateByEntityTypeIdSelective(entityType);
            return result;
        }
    }
}