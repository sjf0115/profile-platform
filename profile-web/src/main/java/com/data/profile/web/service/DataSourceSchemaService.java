package com.data.profile.web.service;

import com.data.profile.web.dao.DataSourceSchemaMapper;
import com.data.profile.web.model.DataSourceCategory;
import com.data.profile.web.model.DataSourceSchema;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.DataSourceSchemaType;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private static Gson gson = new Gson().newBuilder().create();
    private static Logger LOG = LoggerFactory.getLogger(DataSourceSchemaService.class);

    @Resource
    private DataSourceSchemaMapper schemaMapper;

    /**
     * 根据查询条件获取数据源Schema列表
     * @param schema
     * @return
     */
    public List<DataSourceSchema> getList(DataSourceSchema schema) {
        return schemaMapper.selectByParams(schema);
    }

    /**
     * 根据数据源SchemaID获取数据源Schema详细信息
     * @param schemaId
     * @return
     */
    public Optional<DataSourceSchema> getDetail(String schemaId) {
        DataSourceSchema schema = schemaMapper.selectByDataSourceSchemaId(schemaId);
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
            List<DataSourceSchema> schemas = schemaMapper.selectByDataSourceSchemaName(schema.getSchemaName());
            if (schemas.size() > 0) {
                throw new RuntimeException("该数据源Schema已经存在，不允许重复添加");
            }
            String schemaId = IDGenerator.getInstance().generate(ModelType.DATASOURCE_SCHEMA);
            DataSourceSchema target = schemaMapper.selectByDataSourceSchemaId(schemaId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("数据源SchemaID已经存在，不允许重复添加");
            }
            schema.setStatus(Status.ENABLE.getCode());
            schema.setSchemaId(schemaId);
            schema.setSourceType(SourceType.CUSTOM.getCode());
            schema.setCreator(RequestContext.currentUserId());
            schema.setModifier(RequestContext.currentUserId());
            int result = schemaMapper.insertSelective(schema);
            return result;
        } else {
            // 修改
            schema.setModifier(RequestContext.currentUserId());
            int result = schemaMapper.updateByDataSourceSchemaIdSelective(schema);
            return result;
        }
    }

    /**
     * 删除数据源Schema
     * @param schemaId
     * @return
     */
    public int delete(String schemaId) {
        DataSourceSchema schema = schemaMapper.selectByDataSourceSchemaId(schemaId);
        if (Objects.equals(schema.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置数据源Schema不允许删除");
        }
        int result = schemaMapper.deleteByDataSourceSchemaId(schemaId);
        return result;
    }

    /**
     * 获取数据源分类列表
     * @return
     */
    public List<DataSourceCategory> getCategory() {
        List<DataSourceCategory> categories = schemaMapper.selectCategory();
        List<DataSourceCategory> result = categories.stream()
                .peek(category -> category.setName(DataSourceSchemaType.codeOf(category.getId())))
                .collect(Collectors.toList());
        LOG.info("成功获取{}类数据源: {}", categories.size(), gson.toJson(result));
        return result;
    }
}