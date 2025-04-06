package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.DataSourceSchemaType;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.manager.jdbc.JdbcMetaService;
import com.data.profile.manager.utils.JdbcUtil;
import com.data.profile.dao.DatasetMapper;
import com.data.profile.manager.domain.ConnectionParam;
import com.data.profile.manager.domain.Table;
import com.data.profile.model.DataSource;
import com.data.profile.model.DataSourceSchema;
import com.data.profile.model.Dataset;
import com.data.profile.model.DatasetField;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        List<Table> tables;
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

        // 获取表
        String schemaId = dataSource.getSchemaId();
        Optional<DataSourceSchema> schemaOptional = schemaService.getDetail(schemaId);
        if (!schemaOptional.isPresent()) {
            throw new RuntimeException("数据源Schema[" + schemaId + "]不存在，请联系管理员");
        }
        DataSourceSchema schema = schemaOptional.get();
        String jdbcProtocol = schema.getJdbcProtocol();

        String config = dataSource.getConfig();
        // Todo 不同类型解析不一样
        ConnectionParam connectionParam = gson.fromJson(config, ConnectionParam.class);
        connectionParam.setProtocol(jdbcProtocol);

        String url;
        try {
            url = JdbcUtil.buildUrl(connectionParam);
        } catch (URISyntaxException e) {
            throw new RuntimeException("获取数据表构建连接失败: [" + e.getMessage() + "]");
        }
        connectionParam.setUrl(url);

        try {
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
        // 表列信息
        List<DatasetField> columns = getTableColumns(datasourceId, tableName);
        // 数据集字段
        List<DatasetField> fields = Lists.newArrayList();
        if (StringUtils.isNotBlank(datasetId)) {
            Dataset dataset = datasetMapper.selectByDatasetId(datasetId);
            fields = dataset.getFields();
        }
        // 表列删除字段无删除
        //
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
    private List<DatasetField> getTableColumns(String datasourceId, String tableName) {
        // 数据源
        Optional<DataSource> dataSourceOptional = dataSourceService.getDetail(datasourceId);
        if (!dataSourceOptional.isPresent()) {
            throw new RuntimeException("数据源[" + datasourceId + "]不存在，请联系管理员");
        }
        DataSource dataSource = dataSourceOptional.get();

        return null;
    }
}