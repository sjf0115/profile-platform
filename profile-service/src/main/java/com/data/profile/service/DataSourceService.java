package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DataSourceMapper;
import com.data.profile.model.DataSource;
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
    private DataSourceMapper datasourceMapper;

    // 根据查询条件获取数据源列表
    public List<DataSource> getList(DataSource dataSource) {
        List<DataSource> datasources = datasourceMapper.selectByParams(dataSource);
        return datasources;
    }

    // 根据数据源ID获取数据源详细信息
    public Optional<DataSource> getDetail(String dataSourceId) {
        DataSource dataSource = datasourceMapper.selectByDatasourceId(dataSourceId);
        if (dataSource == null) {
            return Optional.empty();
        }
        return Optional.of(dataSource);
    }

    // 保存数据源 新增/修改
    public int save(DataSource datasource) throws RuntimeException {
        if (StringUtils.isBlank(datasource.getDataSourceId())) {
            // 新增
            List<DataSource> datasources = datasourceMapper.selectSimpleByDatasourceName(datasource.getDataSourceName());
            if (datasources.size() > 0) {
                throw new RuntimeException("数据源已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String datasourceId = IDGenerator.generate(ModelType.DATASOURCE);
            DataSource source = datasourceMapper.selectSimpleByDatasourceId(datasourceId);
            if (!Objects.equals(source, null)) {
                throw new RuntimeException("数据源ID已经存在，不允许重复添加");
            }
            datasource.setDataSourceId(datasourceId);
            datasource.setSourceType(SourceType.CUSTOM.getCode());
            datasource.setCreator(RequestContext.currentUserId());
            datasource.setModifier(RequestContext.currentUserId());
            int result = datasourceMapper.insertSelective(datasource);
            return result;
        } else {
            // 修改
            datasource.setModifier(RequestContext.currentUserId());
            int result = datasourceMapper.updateByDataSourceIdSelective(datasource);
            return result;
        }
    }
}