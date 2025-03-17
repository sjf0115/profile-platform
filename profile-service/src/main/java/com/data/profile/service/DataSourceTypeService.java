package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.DefaultIDGenerator;
import com.data.profile.dao.DataSourceTypeMapper;
import com.data.profile.model.DataSourceType;
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
 * 功能：数据源类型服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DataSourceTypeService {
    private static Logger LOG = LoggerFactory.getLogger(DataSourceTypeService.class);

    @Resource
    private DataSourceTypeMapper dataSourceTypeMapper;

    /**
     * 根据查询条件获取数据源类型列表
     * @param dataSourceType
     * @return
     */
    public List<DataSourceType> getList(DataSourceType dataSourceType) {
        List<DataSourceType> dataSourceTypeList = dataSourceTypeMapper.selectByParams(dataSourceType);
        return dataSourceTypeList;
    }

    /**
     * 根据数据源类型ID获取数据源类型详细信息
     * @param dataSourceTypeId
     * @return
     */
    public Optional<DataSourceType> getDetail(String dataSourceTypeId) {
        DataSourceType dataSourceType = dataSourceTypeMapper.selectByDataSourceTypeId(dataSourceTypeId);
        if (dataSourceType == null) {
            return Optional.empty();
        }
        return Optional.of(dataSourceType);
    }

    /**
     * 保存数据源 新增/修改
     * @param dataSourceType
     * @return
     * @throws RuntimeException
     */
    public int save(DataSourceType dataSourceType) throws RuntimeException {
        if (StringUtils.isBlank(dataSourceType.getDataSourceTypeId())) {
            // 新增
            List<DataSourceType> dataSourceTypes = dataSourceTypeMapper.selectByDataSourceTypeName(dataSourceType.getDataSourceTypeName());
            if (dataSourceTypes.size() > 0) {
                throw new RuntimeException("该数据源类型已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String datasourceTypeId = DefaultIDGenerator.generate(ModelType.DATASOURCE_TYPE);
            DataSourceType sourceType = dataSourceTypeMapper.selectByDataSourceTypeId(datasourceTypeId);
            if (!Objects.equals(sourceType, null)) {
                throw new RuntimeException("数据源类型ID已经存在，不允许重复添加");
            }
            dataSourceType.setStatus(Status.ENABLE.getCode());
            dataSourceType.setDataSourceTypeId(datasourceTypeId);
            dataSourceType.setSourceType(SourceType.CUSTOM.getCode());
            dataSourceType.setCreator(RequestContext.currentUserId());
            dataSourceType.setModifier(RequestContext.currentUserId());
            int result = dataSourceTypeMapper.insertSelective(dataSourceType);
            return result;
        } else {
            // 修改
            dataSourceType.setModifier(RequestContext.currentUserId());
            int result = dataSourceTypeMapper.updateByDataSourceTypeIdSelective(dataSourceType);
            return result;
        }
    }
}