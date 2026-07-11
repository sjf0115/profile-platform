package com.data.profile.web.service.lineage;

import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.LineageEdge;
import com.data.profile.web.service.DatasetFieldService;
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
 * 标签血缘抽取：label -> dataset（通过 dataset_field.related_id 反查）
 */
@Slf4j
@Component
public class LabelLineageExtractor implements LineageExtractor {

    @Autowired
    private DatasetFieldService datasetFieldService;

    @Override
    public String assetType() {
        return AssetType.LABEL.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        DatasetField field = datasetFieldService.getDetailByRelatedId(assetId);
        if (field == null || field.getDatasetId() == null) {
            return Collections.emptyList();
        }
        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();
        edges.add(LineageEdge.builder()
                .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                .upstreamType(AssetType.DATASET.getCode())
                .upstreamId(field.getDatasetId())
                .downstreamType(AssetType.LABEL.getCode())
                .downstreamId(assetId)
                .relationType(RelationType.DERIVE.getCode())
                .sourceType(1)
                .creator(operator)
                .modifier(operator)
                .build());
        return edges;
    }
}
