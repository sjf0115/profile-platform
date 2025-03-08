package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DatasetMapper;
import com.data.profile.model.Dataset;
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
 * 功能：数据集服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class DatasetService {
    private static Logger LOG = LoggerFactory.getLogger(DatasetService.class);

    @Resource
    private DatasetMapper datasetMapper;

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
}