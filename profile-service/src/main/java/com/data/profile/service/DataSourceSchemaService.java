package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DataSourceSchemaMapper;
import com.data.profile.model.DataSourceSchema;
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
 * 功能：数据源Schema服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DataSourceSchemaService {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceSchemaService.class);

    @Resource
    private DataSourceSchemaMapper dataSourceSchemaMapper;

    /**
     * 根据查询条件获取数据源Schema列表
     * @param schema
     * @return
     */
    public List<DataSourceSchema> getList(DataSourceSchema schema) {
        List<DataSourceSchema> schemas = dataSourceSchemaMapper.selectByParams(schema);
        return schemas;
    }

    /**
     * 根据数据源SchemaID获取数据源Schema详细信息
     * @param schemaId
     * @return
     */
    public Optional<DataSourceSchema> getDetail(String schemaId) {
        DataSourceSchema schema = dataSourceSchemaMapper.selectByDataSourceSchemaId(schemaId);
        if (schema == null) {
            return Optional.empty();
        }
        return Optional.of(schema);
    }

    /**
     * 保存数据源Schema 新增/修改
     * @param schema
     * @return
     */
    public int save(DataSourceSchema schema) {
        if (StringUtils.isBlank(schema.getSchemaId())) {
            // 新增
            List<DataSourceSchema> schemas = dataSourceSchemaMapper.selectByDataSourceSchemaName(schema.getSchemaName());
            if (schemas.size() > 0) {
                throw new RuntimeException("该数据源Schema已经存在，不允许重复添加");
            }
            String schemaId = IDGenerator.getInstance().generate(ModelType.DATASOURCE_SCHEMA);
            DataSourceSchema target = dataSourceSchemaMapper.selectByDataSourceSchemaId(schemaId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("数据源SchemaID已经存在，不允许重复添加");
            }
            schema.setStatus(Status.ENABLE.getCode());
            schema.setSchemaId(schemaId);
            schema.setSourceType(SourceType.CUSTOM.getCode());
            schema.setCreator(RequestContext.currentUserId());
            schema.setModifier(RequestContext.currentUserId());
            int result = dataSourceSchemaMapper.insertSelective(schema);
            return result;
        } else {
            // 修改
            schema.setModifier(RequestContext.currentUserId());
            int result = dataSourceSchemaMapper.updateByDataSourceSchemaIdSelective(schema);
            return result;
        }
    }
}