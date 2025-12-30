package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.manager.domain.Column;
import com.data.profile.manager.domain.ColumnBuilder;
import com.data.profile.manager.service.JdbcMetaService;
import com.data.profile.manager.utils.TemplateBuilder;
import com.data.profile.manager.utils.JdbcUtil;
import com.data.profile.dao.DatasetMapper;
import com.data.profile.manager.domain.ConnectionParam;
import com.data.profile.manager.domain.Table;
import com.data.profile.model.DataSource;
import com.data.profile.model.DataSourceSchema;
import com.data.profile.model.Dataset;
import com.data.profile.model.DatasetField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：数据集服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */
@Slf4j
@Service
public class DatasetService {
    private static final Gson gson = new GsonBuilder().create();
    private static Logger LOG = LoggerFactory.getLogger(DatasetService.class);

    @Resource
    private DatasetMapper datasetMapper;

    @Resource
    private DataSourceService dataSourceService;

    @Resource
    private DataSourceSchemaService schemaService;

    @Resource
    private JdbcMetaService metaService;

    /**
     * 根据查询条件获取数据集列表
     * @param dataset
     * @return
     */
    public List<Dataset> getList(Dataset dataset) {
        List<Dataset> datasets = datasetMapper.selectByParams(dataset);
        return datasets;
    }

    /**
     * 根据数据集ID获取数据集详细信息
     * @param datasetId
     * @return
     */
    public Optional<Dataset> getDetail(String datasetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
        if (dataset == null) {
            return Optional.empty();
        }
        return Optional.of(dataset);
    }

    /**
     * 保存数据集 创建/修改
     * @param dataset
     * @return
     */
    public int save(Dataset dataset) {
        // 数据集字段判断
        List<DatasetField> fields = dataset.getFields();
        for (DatasetField field : fields) {
            int status = field.getStatus();
            if (Objects.equals(status, FieldStatus.DELETE_FIELD.getCode())) {
                throw new RuntimeException("数据集字段[" + field.getName() + "]在原始表中已经被删除，请尽快联系原始表Owner处理");
            }
        }
        if (StringUtils.isBlank(dataset.getDatasetId())) {
            // 创建数据集
            return createDataset(dataset);
        } else {
            // 修改数据集
            return updateDataset(dataset);
        }
    }

    /**
     * 根据数据源ID获取数据表
     * @param datasourceId
     * @return
     */
    public List<Table> getTables(String datasourceId) {
        List<Table> tables = Lists.newArrayList();
        if (StringUtils.isBlank(datasourceId)) {
            return tables;
        }

        try {
            ConnectionParam connectionParam = getConnectionParam(datasourceId);
            tables = metaService.getTables(connectionParam);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据表失败: [" + e.getMessage() + "]");
        }
        return tables;
    }

    /**
     * 获取数据集字段
     * @param datasourceId
     * @param tableName
     * @param datasetId
     * @return
     */
    public List<DatasetField> getDatasetField(String datasourceId, String tableName, String datasetId) {
        // 原始表列
        List<Column> columns = getTableColumns(datasourceId, tableName);
        Set<String> columnNames = columns.stream().map(c -> c.getColumnName()).collect(Collectors.toSet()); // 原始列名称集合
        // 数据集字段
        List<DatasetField> fields = Lists.newArrayList();
        if (StringUtils.isNotBlank(datasetId)) {
            Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
            fields = dataset.getFields();
        }

        // 修改字段、删除字段
        for (DatasetField field : fields) {
            if (columnNames.contains(field.getName())) {
                // 修改字段(数据集字段在原始表中还存在)
                field.setStatus(FieldStatus.UPDATE_FIELD.getCode());
                // 移除数据集字段(最后剩下是原始表新增字段)
                columnNames.remove(field.getName());
            } else {
                // 删除字段(数据集字段在原始表中已经删除)
                field.setStatus(FieldStatus.DELETE_FIELD.getCode());
            }
        }

        // 新增字段
        for (Column column : columns) {
            if (columnNames.contains(column.getColumnName())) {
                // 新增字段 Column -> DatasetField 均是默认值
                DatasetField datasetField = new DatasetField();
                datasetField.setName(column.getColumnName());
                datasetField.setAlias(column.getColumnComment());
                datasetField.setStatus(FieldStatus.ADD_FIELD.getCode());
                fields.add(datasetField);
            }
        }
        return fields;
    }

    /**
     * 创建数据集
     * @param dataset
     * @return
     */
    private int createDataset(Dataset dataset) {
        // 数据集名称是否唯一
        List<Dataset> datasets = datasetMapper.selectByDatasetName(dataset.getDatasetName());
        if (datasets.size() > 0) {
            throw new RuntimeException("数据集已经存在，不允许重复添加");
        }
        // 数据集ID是否唯一
        String datasetId = IDGenerator.getInstance().generate(ModelType.DATASET);
        Dataset target = datasetMapper.selectByDatasetId(datasetId);
        if (!Objects.equals(target, null)) {
            throw new RuntimeException("数据集ID已经存在，不允许重复添加");
        }
        dataset.setDatasetId(datasetId);
        dataset.setStatus(Status.ENABLE.getCode());
        dataset.setSourceType(SourceType.CUSTOM.getCode());
        dataset.setOwner(RequestContext.currentUserId());
        dataset.setCreator(RequestContext.currentUserId());
        dataset.setModifier(RequestContext.currentUserId());
        int result = datasetMapper.insertSelective(dataset);
        // TODO 创建数据集表 在引擎中创建数据集表
        String datasetTable = createDatasetTable(dataset);
        LOG.info("创建表语句: " + datasetTable);
        return result;
    }

    /**
     * 修改数据集
     * @param dataset
     * @return
     */
    private int updateDataset(Dataset dataset) {
        dataset.setModifier(RequestContext.currentUserId());
        int result = datasetMapper.updateByDatasetIdSelective(dataset);
        return result;
    }

    /**
     * 获取表列信息
     * @param datasourceId
     * @param tableName
     * @return
     */
    private List<Column> getTableColumns(String datasourceId, String tableName) {
        List<Column> columns = Lists.newArrayList();
        if (StringUtils.isBlank(tableName)) {
            return columns;
        }
        try {
            ConnectionParam connectionParam = getConnectionParam(datasourceId);
            columns = metaService.getColumns(connectionParam, tableName);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据表失败: [" + e.getMessage() + "]");
        }
        return columns;
    }

    /**
     * 获取数据源链接信息
     * @param datasourceId
     * @return
     */
    private ConnectionParam getConnectionParam(String datasourceId) {
        // 数据源
        Optional<DataSource> dataSourceOptional = dataSourceService.getDetail(datasourceId);
        if (!dataSourceOptional.isPresent()) {
            throw new RuntimeException("数据源[" + datasourceId + "]不存在，请联系管理员");
        }
        DataSource dataSource = dataSourceOptional.get();

        // 只支持 Source 类型数据源
        String schemaName = dataSource.getSchemaName();
        Integer schemaType = dataSource.getSchemaType();
        if (Objects.equals(schemaType, DataSourceSchemaType.SINK)) {
            throw new RuntimeException("不支持数据源类型[" + schemaName + "]，请重新选择");
        }

        // 获取 JDBC 协议
        String schemaId = dataSource.getSchemaId();
        Optional<DataSourceSchema> schemaOptional = schemaService.getDetail(schemaId);
        if (!schemaOptional.isPresent()) {
            throw new RuntimeException("数据源Schema[" + schemaId + "]不存在，请联系管理员");
        }
        DataSourceSchema schema = schemaOptional.get();
        String jdbcProtocol = schema.getJdbcProtocol();

        // 生成 ConnectionParam
        // Todo 不同类型解析不一样
        String config = dataSource.getConfig();
        ConnectionParam connectionParam = gson.fromJson(config, ConnectionParam.class);
        connectionParam.setProtocol(jdbcProtocol);

        // JDBC URL
        String url;
        try {
            url = JdbcUtil.buildUrl(connectionParam);
        } catch (URISyntaxException e) {
            throw new RuntimeException("获取数据表构建连接失败: [" + e.getMessage() + "]");
        }
        connectionParam.setUrl(url);

        return connectionParam;
    }

    /**
     * 创建数据集对应的引擎表
     * @param dataset
     * @return
     */
    private String createDatasetTable(Dataset dataset) {
        String tableName = dataset.getDatasetId();
        String tableComment = dataset.getDatasetName();
        List<DatasetField> fields = dataset.getFields();

        List<Column> columns = fields.stream().map(field -> new ColumnBuilder()
                .setColumnName(field.getName())
                .setColumnType(field.getColumnType())
                .setColumnComment(field.getAlias())
                .build()
        ).collect(Collectors.toList());

        Map<String, Object> params = Maps.newHashMap();
        params.put("tableName", tableName);
        params.put("columns", columns);
        params.put("orderBy", "dt");

        String createTableSQL = new TemplateBuilder()
                .setName("create_table.ftl")
                .setPath("clickhouse")
                .setParams(params)
                .build();
        return createTableSQL;
    }
}