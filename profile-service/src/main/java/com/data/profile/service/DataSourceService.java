package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DataSourceMapper;
import com.data.profile.manager.domain.Column;
import com.data.profile.manager.domain.Table;
import com.data.profile.model.DataSource;
import com.data.profile.model.DataSourceSchema;
import com.google.common.collect.Lists;
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
 * 功能：数据源服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DataSourceService {
    private static Gson gson = new GsonBuilder().create();

    @Resource
    private DataSourceMapper dataSourceMapper;
    @Resource
    private DataSourceSchemaService schemaService;

    /**
     * 根据查询条件获取数据源列表
     * @param dataSource 查询条件
     */
    public List<DataSource> getList(DataSource dataSource) {
        List<DataSource> dataSources = dataSourceMapper.selectByParams(dataSource);
        log.info("根据查询条件获取 {} 个数据源: {}", dataSources.size(), gson.toJson(dataSources));
        return dataSources;
    }

    /**
     * 根据数据源ID获取数据源详细信息
     * @param dataSourceId 数据源ID
     */
    public Optional<DataSource> getDetail(String dataSourceId) {
        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);
        if (dataSource == null) {
            return Optional.empty();
        }
        return Optional.of(dataSource);
    }

    /**
     * 保存数据源 新增/修改
     * @param datasource
     * @return
     * @throws RuntimeException
     */
    public int save(DataSource datasource) throws RuntimeException {
        if (StringUtils.isBlank(datasource.getDatasourceId())) {
            // 新增
            List<DataSource> dataSources = dataSourceMapper.selectSimpleByDatasourceName(datasource.getDatasourceName());
            if (!dataSources.isEmpty()) {
                throw new RuntimeException("数据源已经存在，不允许重复添加");
            }
            String datasourceId = IDGenerator.getInstance().generate(ModelType.DATASOURCE);
            DataSource source = dataSourceMapper.selectSimpleByDatasourceId(datasourceId);
            if (!Objects.equals(source, null)) {
                throw new RuntimeException("数据源ID已经存在，不允许重复添加");
            }
            // 数据源类型
            String schemaId = datasource.getSchemaId();
            Optional<DataSourceSchema> schema = schemaService.getDetail(schemaId);
            if (!schema.isPresent()) {
                throw new RuntimeException("指定的数据源类型不存在");
            }
            datasource.setStatus(Status.ENABLE.getCode());
            datasource.setDatasourceId(datasourceId);
            datasource.setSourceType(SourceType.CUSTOM.getCode());
            datasource.setOwner(RequestContext.currentUserId());
            datasource.setCreator(RequestContext.currentUserId());
            datasource.setModifier(RequestContext.currentUserId());
            return dataSourceMapper.insertSelective(datasource);
        } else {
            // 修改
            datasource.setModifier(RequestContext.currentUserId());
            return dataSourceMapper.updateByDataSourceIdSelective(datasource);
        }
    }

    /**
     * 删除数据源
     * @param dataSourceId 数据源ID
     */
    public int delete(String dataSourceId) {
        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);
        if (Objects.equals(dataSource.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置数据源不允许删除");
        }
        // TODO: 逻辑删除
        return dataSourceMapper.deleteByDatasourceId(dataSourceId);
    }

    /**
     * 根据数据源ID获取数据表
     * @param datasourceId 数据源ID
     * @return
     */
    public List<Table> getTables(String datasourceId) {
        List<Table> tables = Lists.newArrayList();
        if (StringUtils.isBlank(datasourceId)) {
            return tables;
        }

        try {
            /* ConnectionParam connectionParam = getConnectionParam(datasourceId);
            tables = metaService.getTables(connectionParam);*/
            List<Column> columns = Lists.newArrayList(
                    Column.builder().columnName("dt").columnComment("日期").columnType("string").build(),
                    Column.builder().columnName("uid").columnComment("用户ID").columnType("string").build(),
                    Column.builder().columnName("age").columnComment("年龄").columnType("int").build(),
                    Column.builder().columnName("sex").columnComment("性别").columnType("string").build()
            );

            tables = Lists.newArrayList(
                    Table.builder().tableName("dws_app_user_base_1d").tableComment("用户基础表").isPartitionTable(true).columns(columns).build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取数据表失败: [" + e.getMessage() + "]");
        }
        return tables;
    }
}