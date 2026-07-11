package com.data.profile.web.service.lineage;

import com.data.profile.web.dao.GroupAnalysisMapper;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.GroupAnalysis;
import com.data.profile.web.model.LineageEdge;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.enums.ModelType;
import com.data.profile.web.security.UserContextHolder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 群组分析血缘抽取：analysis -> group / label
 */
@Slf4j
@Component
public class AnalysisLineageExtractor implements LineageExtractor {

    @Autowired
    private GroupAnalysisMapper groupAnalysisMapper;

    private final Gson gson = new Gson();

    @Override
    public String assetType() {
        return AssetType.ANALYSIS.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        GroupAnalysis analysis = groupAnalysisMapper.selectByAnalysisId(assetId);
        if (analysis == null) {
            return Collections.emptyList();
        }

        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();

        // group_id -> 上游群组
        if (analysis.getGroupId() != null && !analysis.getGroupId().isEmpty()) {
            edges.add(buildEdge(assetId, AssetType.GROUP.getCode(), analysis.getGroupId(),
                    RelationType.CONSUME.getCode(), operator));
        }

        // compare_group_ids -> 对比群组
        for (String gid : parseJsonArray(analysis.getCompareGroupIds())) {
            edges.add(buildEdge(assetId, AssetType.GROUP.getCode(), gid,
                    RelationType.CONSUME.getCode(), operator));
        }

        // label_ids -> 标签
        for (String lid : parseJsonArray(analysis.getLabelIds())) {
            edges.add(buildEdge(assetId, AssetType.LABEL.getCode(), lid,
                    RelationType.REFERENCE.getCode(), operator));
        }

        return edges;
    }

    private LineageEdge buildEdge(String analysisId, String upstreamType, String upstreamId,
                                   String relationType, String operator) {
        return LineageEdge.builder()
                .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                .upstreamType(upstreamType)
                .upstreamId(upstreamId)
                .downstreamType(AssetType.ANALYSIS.getCode())
                .downstreamId(analysisId)
                .relationType(relationType)
                .sourceType(1)
                .creator(operator)
                .modifier(operator)
                .build();
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            log.warn("解析 JSON 数组失败: {}", json);
            return Collections.emptyList();
        }
    }
}
