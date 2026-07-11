package com.data.profile.web.service.lineage;

import com.data.profile.web.dao.DatasetMapper;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.model.LineageEdge;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.enums.ModelType;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 数据集血缘抽取：dataset -> datasource
 */
@Slf4j
@Component
public class DatasetLineageExtractor implements LineageExtractor {

    @Autowired
    private DatasetMapper datasetMapper;

    @Override
    public String assetType() {
        return AssetType.DATASET.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        Dataset dataset = datasetMapper.selectByDatasetId(assetId);
        if (dataset == null || dataset.getDatasourceId() == null) {
            return Collections.emptyList();
        }
        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();
        edges.add(LineageEdge.builder()
                .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                .upstreamType(AssetType.DATASOURCE.getCode())
                .upstreamId(dataset.getDatasourceId())
                .downstreamType(AssetType.DATASET.getCode())
                .downstreamId(assetId)
                .relationType(RelationType.DERIVE.getCode())
                .sourceType(1)
                .creator(operator)
                .modifier(operator)
                .build());
        return edges;
    }
}
