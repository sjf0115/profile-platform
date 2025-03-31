package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.DatasetMapper;
import com.data.profile.model.Dataset;
import com.data.profile.model.Event;
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
}