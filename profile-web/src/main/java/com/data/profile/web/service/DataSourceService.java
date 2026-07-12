package com.data.profile.web.service;

import com.data.connector.api.ConnectorFactory;
import com.data.connector.api.ExportConfigBuilder;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.DataSourceConverter;
import com.data.profile.web.dao.DataSourceMapper;
import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.dto.DataSourceRequest;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.model.User;
import com.data.profile.web.security.UserContextHolder;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
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
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Autowired
    private DataSourceMapper dataSourceMapper;
    @Autowired
    private UserService userService;

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
     */
    // TODO 优化
    public List<DataSource> getListDO(DataSource dataSource) {
        return dataSourceMapper.selectSimpleByParams(dataSource);
    }

    /**
     * 根据查询条件获取数据源列表
     */
    public List<DataSourceDTO> getList(DataSource dataSource) {
        List<DataSource> dataSources = dataSourceMapper.selectSimpleByParams(dataSource);
        log.info("根据查询条件获取 {} 个数据源", dataSources.size());
        List<DataSourceDTO> dtos = DataSourceConverter.do2dtoList(dataSources);
        return dtos;
    }

    /**
     * 根据数据源ID获取数据源详细信息
     */
    public DataSourceDTO getDetail(String dataSourceId) {
        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在");
        }
        Map<String, String> userMap = userService.getUserNameMap();
        DataSourceDTO dto = DataSourceConverter.do2dto(dataSource);
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        log.info("根据数据源ID {} 获取数据源详细信息：{}", dataSourceId, JSONUtils.toJsonString(dto));
        return dto;
    }

    /**
     * 创建数据源
     */
    public DataSourceDTO create(DataSourceRequest request) throws RuntimeException {
        String userId = UserContextHolder.currentUserId();

        // 检查名称重复
        List<DataSource> existing = dataSourceMapper.selectSimpleByDatasourceName(request.getDatasourceName());
        if (!existing.isEmpty()) {
            throw new RuntimeException("数据源已经存在，不允许重复添加");
        }
        // 检查类型
        if (StringUtils.isBlank(request.getDatasourceType())) {
            throw new RuntimeException("数据源类型不能为空");
        }

        String datasourceId = IDGenerator.getInstance().generate(ModelType.DATASOURCE);
        DataSource dataSource = DataSourceConverter.request2do(request);
        dataSource.setDatasourceId(datasourceId);
        dataSource.setStatus(Status.ENABLE.getCode());
        dataSource.setSourceType(SourceType.CUSTOM.getCode());
        dataSource.setOwner(StringUtils.isNotBlank(request.getOwner()) ? request.getOwner() : userId);
        dataSource.setCreator(userId);
        dataSource.setModifier(userId);

        dataSourceMapper.insertSelective(dataSource);
        resourceGrantService.grantOwner("05", datasourceId, userId);

        DataSourceDTO dto = DataSourceConverter.do2dto(dataSource);
        return dto;
    }

    /**
     * 更新数据源
     */
    public int update(String datasourceId, DataSourceRequest request) {
        DataSource existing = dataSourceMapper.selectSimpleByDatasourceId(datasourceId);
        if (existing == null) {
            throw new RuntimeException("数据源不存在");
        }
        // 检查名称重复（排除自身）
        if (request.getDatasourceName() != null && !request.getDatasourceName().equals(existing.getDatasourceName())) {
            List<DataSource> dup = dataSourceMapper.selectSimpleByDatasourceName(request.getDatasourceName());
            if (!dup.isEmpty()) {
                throw new RuntimeException("数据源名称已存在");
            }
        }

        DataSource dataSource = DataSourceConverter.request2do(request);
        dataSource.setDatasourceId(datasourceId);
        dataSource.setModifier(UserContextHolder.currentUserId());
        return dataSourceMapper.updateByDataSourceIdSelective(dataSource);
    }

    /**
     * 更新数据源状态（启用/停用）
     */
    public int updateStatus(String datasourceId, Integer status) {
        DataSource dataSource = new DataSource();
        dataSource.setDatasourceId(datasourceId);
        dataSource.setStatus(status);
        dataSource.setModifier(UserContextHolder.currentUserId());
        return dataSourceMapper.updateByDataSourceIdSelective(dataSource);
    }

    /**
     * 删除数据源
     */
    public int delete(String dataSourceId) {
        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);
        if (dataSource == null) {
            throw new RuntimeException("数据源不存在");
        }
        if (Objects.equals(dataSource.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置数据源不允许删除");
        }
        return dataSourceMapper.deleteByDatasourceId(dataSourceId);
    }

    /**
     * 根据插件类型获取展示配置
     * @param type 数据源类型
     */
    public String getConfigJson(String type) {
        String config = PluginLoader.getPluginLoader(ConnectorFactory.class)
                .getOrCreatePlugin(type).getConfigBuilder().build();
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
        DataSourceDTO dataSource = getDetail(dataSourceId);
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
            log.info("通过数据源 {} 获取数据库: {}", dataSourceId, JSONUtils.toJsonString(databases));
            return databases;
        } catch (SQLException e) {
            log.error("获取数据库失败: {}", dataSource.getDatasourceName(), e);
            throw new ProfileException("获取数据库失败: " + e.getMessage());
        }
    }

    /**
     * 根据数据源ID和数据库查询数据表
     * @param dataSourceId 数据源ID
     * @param database 数据库
     */
    public List<TableInfo> getTableList(String dataSourceId, String database) {
        // 获取数据源信息
        DataSourceDTO dataSource = getDetail(dataSourceId);
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
        DataSourceDTO dataSource = getDetail(dataSourceId);
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
     * 根据数据源ID获取投递配置表单定义
     * @param datasourceId 数据源ID
     */
    public String getExportConfigJson(String datasourceId) {
        DataSource ds = dataSourceMapper.selectByDatasourceId(datasourceId);
        if (ds == null) {
            throw new ProfileException("数据源不存在: " + datasourceId);
        }
        String type = ds.getDatasourceType();
        ConnectorFactory factory = PluginLoader.getPluginLoader(ConnectorFactory.class)
                .getOrCreatePlugin(type);
        ExportConfigBuilder builder = factory.getExportConfigBuilder();
        return builder != null ? builder.build() : "[]";
    }

    /**
     * 简化版：根据数据源ID获取数据表列表（后端自动从 config 中提取 database）
     * @param datasourceId 数据源ID
     */
    // TODO
    public List<TableInfo> getTableListByDatasource(String datasourceId) {
        DataSourceDTO ds = getDetail(datasourceId);
        String database = extractDatabaseFromConfig(ds.getConfig());
        return getTableList(datasourceId, database);
    }

    /**
     * 简化版：根据数据源ID和数据表查询数据列（后端自动从 config 中提取 database）
     * @param datasourceId 数据源ID
     * @param table 数据表名
     */
    // TODO
    public TableColumnInfo getColumnListByDatasource(String datasourceId, String table) {
        DataSourceDTO ds = getDetail(datasourceId);
        String database = extractDatabaseFromConfig(ds.getConfig());
        return getColumnList(datasourceId, database, table);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 从数据源 config JSON 中提取 database 字段
     */
    private String extractDatabaseFromConfig(String configJson) {
        if (StringUtils.isBlank(configJson)) {
            throw new ProfileException("数据源配置为空");
        }
        try {
            Map<String, Object> configMap = gson.fromJson(configJson, Map.class);
            Object database = configMap.get("database");
            if (database == null || StringUtils.isBlank(database.toString())) {
                throw new ProfileException("数据源配置中未包含 database 字段");
            }
            return database.toString();
        } catch (Exception e) {
            throw new ProfileException("解析数据源配置失败: " + e.getMessage());
        }
    }
}