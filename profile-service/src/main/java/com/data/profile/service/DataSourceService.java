package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DataSourceMapper;
import com.data.profile.model.DataSource;
import com.data.profile.model.DataSourceType;
import com.google.common.collect.Lists;
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
 * 功能：数据源服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DataSourceService {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceService.class);

    @Resource
    private DataSourceMapper dataSourceMapper;
    @Resource
    private DataSourceTypeService dataSourceTypeService;

    /**
     * 根据查询条件获取数据源列表
     * @param dataSource
     * @return
     */
    public List<DataSource> getList(DataSource dataSource) {
        List<DataSource> dataSources = dataSourceMapper.selectByParams(dataSource);
        return dataSources;
    }

    /**
     * 根据数据源ID获取数据源详细信息
     * @param dataSourceId
     * @return
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
        if (StringUtils.isBlank(datasource.getDataSourceId())) {
            // 新增
            List<DataSource> dataSources = dataSourceMapper.selectSimpleByDatasourceName(datasource.getDataSourceName());
            if (dataSources.size() > 0) {
                throw new RuntimeException("数据源已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String datasourceId = IDGenerator.generate(ModelType.DATASOURCE);
            DataSource source = dataSourceMapper.selectSimpleByDatasourceId(datasourceId);
            if (!Objects.equals(source, null)) {
                throw new RuntimeException("数据源ID已经存在，不允许重复添加");
            }
            // 数据源类型
            String dataSourceTypeId = datasource.getDataSourceTypeId();
            Optional<DataSourceType> dataSourceType = dataSourceTypeService.getDetail(dataSourceTypeId);
            if (!dataSourceType.isPresent()) {
                throw new RuntimeException("指定的数据源类型不存在");
            }
            datasource.setStatus(Status.ENABLE.getCode());
            datasource.setDataSourceId(datasourceId);
            datasource.setSourceType(SourceType.CUSTOM.getCode());
            datasource.setOwner(RequestContext.currentUserId());
            datasource.setCreator(RequestContext.currentUserId());
            datasource.setModifier(RequestContext.currentUserId());
            int result = dataSourceMapper.insertSelective(datasource);
            return result;
        } else {
            // 修改
            datasource.setModifier(RequestContext.currentUserId());
            int result = dataSourceMapper.updateByDataSourceIdSelective(datasource);
            return result;
        }
    }

    /**
     * 根据数据源ID获取数据表
     * @param dataSourceId 只有 source 数据源支持
     * @return
     */
    public List<String> getTables(String dataSourceId) {
        List<String> tables = Lists.newArrayList();

        DataSource dataSource = dataSourceMapper.selectByDatasourceId(dataSourceId);

        return tables;
    }

}