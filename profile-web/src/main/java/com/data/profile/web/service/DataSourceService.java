package com.data.profile.web.service;

import com.data.connector.api.ConnectorFactory;
import com.data.profile.web.dao.DataSourceMapper;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.domain.connector.jdbc.DatabaseInfo;
import com.data.profile.common.domain.connector.jdbc.TableColumnInfo;
import com.data.profile.common.domain.connector.jdbc.TableInfo;
import com.data.profile.common.domain.connector.request.*;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.exception.ProfileException;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.vo.Item;
import com.data.spi.PluginLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;

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

    /**
     * 测试连通性
     * @param param 参数
     */
    public ConnectorResponse testConnect(TestConnectionRequestParam param) {
        ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
        return connectorFactory.getConnector().testConnect(param);
    }

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
    public DataSource getDetail(String dataSourceId) {
        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);
        log.info("根据数据源ID {} 获取数据源详细信息: {}", dataSourceId, gson.toJson(dataSource));
        return dataSource;
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
            // 检查数据源类型
            String datasourceType = datasource.getDatasourceType();
            if (StringUtils.isBlank(datasourceType)) {
                throw new RuntimeException("数据源类型不能为空");
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
     * 根据插件类型获取展示配置
     * @param type 数据源类型
     */
    public String getConfigJson(String type) {
        String config = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(type).getConfigBuilder().build(false);
        log.info("根据插件类型 {} 获取展示配置: {}", type, config);
        return config;
    }

    /**
     * 获取所有支持的插件类型
     */
    public List<Item> getConnectorTypeList() {
        Set<String> connectorList = PluginLoader.getPluginLoader(ConnectorFactory.class).getSupportedPlugins();
        List<Item> items = new ArrayList<>();
        connectorList.forEach(it -> {
            ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(it);
            if (connectorFactory.showInFrontend()) {
                Item item = new Item(it, it);
                items.add(item);
            }
        });
        log.info("获取所有支持的插件类型: {}", gson.toJson(items));
        return items;
    }


    /**
     * 根据数据源ID获取数据库
     * @param dataSourceId 数据源ID
     */
    public List<DatabaseInfo> getDatabaseList(String dataSourceId) {
        // 获取数据源信息
        DataSource dataSource = getDetail(dataSourceId);
        String datasourceType = dataSource.getDatasourceType();
        String config = dataSource.getConfig();

        // 查询数据库
        try {
            GetDatabasesRequestParam param = new GetDatabasesRequestParam();
            param.setType(datasourceType);
            param.setDataSourceParam(config);
            ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
            ConnectorResponse response = connectorFactory.getConnector().getDatabases(param);
            List<DatabaseInfo> databases = (List<DatabaseInfo>)response.getResult();
            log.info("获取数据库列表成功: {}", gson.toJson(databases));
            return databases;
        } catch (SQLException e) {
            log.error("获取数据库列表失败: {}", dataSource.getDatasourceName(), e);
            throw new ProfileException("获取数据库列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据数据源ID和数据库查询数据表
     * @param dataSourceId 数据源ID
     * @param database 数据库
     */
    public List<TableInfo> getTableList(String dataSourceId, String database) {
        // 获取数据源信息
        DataSource dataSource = getDetail(dataSourceId);
        String dataSourceType = dataSource.getDatasourceType();
        String config = dataSource.getConfig();

        // 查询数据表
        try {
            GetTablesRequestParam param = new GetTablesRequestParam();
            param.setType(dataSourceType);
            param.setDataSourceParam(config);
            param.setDatabase(database);

            ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
            ConnectorResponse response = connectorFactory.getConnector().getTables(param);
            List<TableInfo> tables = (List<TableInfo>)response.getResult();
            log.info("获取数据表列表成功: {}", gson.toJson(tables));
            return tables;
        } catch (SQLException e) {
            log.error("获取数据源 {} 下指定数据库 {} 数据表列表失败", dataSource.getDatasourceName(), database, e);
            throw new ProfileException("获取数据表列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据数据源ID、数据库和数据表查询数据列
     * @param dataSourceId 数据源ID
     * @param database 数据库
     * @param table 数据表
     */
    public TableColumnInfo getColumnList(String dataSourceId, String database, String table) {
        // 获取数据源信息
        DataSource dataSource = getDetail(dataSourceId);
        String dataSourceType = dataSource.getDatasourceType();
        String config = dataSource.getConfig();

        // 查询数据表列
        try {
            GetColumnsRequestParam param = new GetColumnsRequestParam();
            param.setType(dataSourceType);
            param.setDataSourceParam(config);
            param.setDataBase(database);
            param.setTable(table);

            ConnectorFactory connectorFactory = PluginLoader.getPluginLoader(ConnectorFactory.class).getOrCreatePlugin(param.getType());
            ConnectorResponse response = connectorFactory.getConnector().getColumns(param);
            TableColumnInfo columnInfo = (TableColumnInfo)response.getResult();
            log.info("获取数据列成功: {}", gson.toJson(columnInfo));
            return columnInfo;
        } catch (SQLException e) {
            log.error("获取数据源 {} 下指定数据库 {} 特定表 {} 的数据列失败", dataSource.getDatasourceName(), database, table, e);
            throw new ProfileException("获取数据表列失败: " + e.getMessage());
        }
    }




    /**
     * 根据数据源ID获取数据表
     * @param datasourceId 数据源ID
     */
    /*public List<Table> getTables(String datasourceId) {
        List<Table> tables = Lists.newArrayList();
        if (StringUtils.isBlank(datasourceId)) {
            return tables;
        }

        try {
            *//* ConnectionParam connectionParam = getConnectionParam(datasourceId);
            tables = metaService.getTables(connectionParam);*//*
            List<Column> columns = Lists.newArrayList(
                    Column.builder().columnName("dt").columnComment("日期").columnType("string").build(),
                    Column.builder().columnName("uid").columnComment("用户ID").columnType("string").build(),
                    Column.builder().columnName("age").columnComment("年龄").columnType("int").build(),
                    Column.builder().columnName("sex").columnComment("性别").columnType("string").build()
            );

            List<Column> columns2 = Lists.newArrayList(
                    Column.builder().columnName("dt").columnComment("日期").columnType("string").build(),
                    Column.builder().columnName("item_id").columnComment("内容ID").columnType("string").build(),
                    Column.builder().columnName("item_type").columnComment("内容类型").columnType("string").build(),
                    Column.builder().columnName("show_source").columnComment("展示来源").columnType("string").build()
            );

            tables = Lists.newArrayList(
                    Table.builder().tableName("dws_app_user_base_1d").tableComment("用户基础表").isPartitionTable(true).columns(columns).build(),
                    Table.builder().tableName("dws_app_item_base_1d").tableComment("内容基础表").isPartitionTable(true).columns(columns2).build()
            );

        } catch (Exception e) {
            throw new RuntimeException("获取数据表失败: [" + e.getMessage() + "]");
        }
        return tables;
    }*/
}