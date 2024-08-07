package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DatasourceMapper;
import com.data.profile.dao.EntityMapper;
import com.data.profile.model.Datasource;
import com.data.profile.model.Entity;
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
 * 功能：实体服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class EntityService {
    private static Logger LOG = LoggerFactory.getLogger(EntityService.class);

    @Resource
    private EntityMapper entityMapper;

    /**
     * 根据查询条件获取实体列表
     * @param entity
     * @return
     */
    public List<Entity> getList(Entity entity) {
        List<Entity> entities = entityMapper.selectByParams(entity);
        return entities;
    }

    /**
     * 根据实体ID获取实体详细信息
     * @param entityId
     * @return
     */
    public Optional<Entity> getDetail(String entityId) {
        Entity entity = entityMapper.selectByEntityId(entityId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    /**
     * 保存实体 新增/修改
     * @param entity
     * @return
     * @throws RuntimeException
     */
    public int save(Entity entity) throws RuntimeException {
        if (StringUtils.isBlank(entity.getEntityId())) {
            // 新增
            List<Entity> entities = entityMapper.selectByEntityName(entity.getEntityName());
            if (entities.size() > 0) {
                throw new RuntimeException("实体已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String entityId = IDGenerator.generate(ModelType.ENTITY);
            Entity target = entityMapper.selectByEntityId(entityId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("实体ID已经存在，不允许重复添加");
            }
            entity.setEntityId(entityId);
            entity.setStatus(Status.ENABLE.getCode());
            entity.setSourceType(SourceType.CUSTOM.getCode());
            entity.setCreator(RequestContext.currentUserId());
            entity.setModifier(RequestContext.currentUserId());
            int result = entityMapper.insertSelective(entity);
            return result;
        } else {
            // 修改
            entity.setModifier(RequestContext.currentUserId());
            int result = entityMapper.updateByEntityIdSelective(entity);
            return result;
        }
    }
}