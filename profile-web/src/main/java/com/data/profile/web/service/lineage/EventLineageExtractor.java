package com.data.profile.web.service.lineage;

import com.data.profile.web.dao.EventMapper;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.Event;
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
 * 事件血缘抽取：event -> dataset
 */
@Slf4j
@Component
public class EventLineageExtractor implements LineageExtractor {

    @Autowired
    private EventMapper eventMapper;

    @Override
    public String assetType() {
        return AssetType.EVENT.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        Event event = eventMapper.selectByEventId(assetId);
        if (event == null || event.getDatasetId() == null) {
            return Collections.emptyList();
        }
        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();
        edges.add(LineageEdge.builder()
                .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                .upstreamType(AssetType.DATASET.getCode())
                .upstreamId(event.getDatasetId())
                .downstreamType(AssetType.EVENT.getCode())
                .downstreamId(assetId)
                .relationType(RelationType.DERIVE.getCode())
                .sourceType(1)
                .creator(operator)
                .modifier(operator)
                .build());
        return edges;
    }
}
