package com.data.profile.web.service.lineage;

import com.data.profile.web.dao.ExportMapper;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.ExportConfig;
import com.data.profile.web.model.LineageEdge;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.enums.ModelType;
import com.data.profile.web.security.UserContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 投递血缘抽取：export -> group / datasource(mode=1) / application(mode=2)
 */
@Slf4j
@Component
public class ExportLineageExtractor implements LineageExtractor {

    @Autowired
    private ExportMapper exportMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String assetType() {
        return AssetType.EXPORT.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        Export export = exportMapper.selectByExportId(assetId);
        if (export == null) {
            return Collections.emptyList();
        }

        ExportConfig config = parseExportConfig(export.getExportConfig());
        if (config == null) {
            return Collections.emptyList();
        }

        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();

        // group 引用边（数据来源）
        if (StringUtils.isNotBlank(config.getGroupId())) {
            edges.add(buildEdge(assetId, AssetType.GROUP.getCode(), config.getGroupId(),
                    RelationType.CONSUME.getCode(), operator));
        }

        // 按投递模式区分目标
        if (export.getExportMode() != null && export.getExportMode() == 2) {
            // 应用投递 -> application（applicationId 存储的就是 appKey）
            if (StringUtils.isNotBlank(config.getApplicationId())) {
                edges.add(buildEdge(assetId, AssetType.APPLICATION.getCode(), config.getApplicationId(),
                        RelationType.EXPORT.getCode(), operator));
            }
        } else {
            // 数据源投递 -> datasource
            if (StringUtils.isNotBlank(config.getDatasourceId())) {
                edges.add(buildEdge(assetId, AssetType.DATASOURCE.getCode(), config.getDatasourceId(),
                        RelationType.EXPORT.getCode(), operator));
            }
        }

        return edges;
    }

    private LineageEdge buildEdge(String exportId, String upstreamType, String upstreamId,
                                   String relationType, String operator) {
        return LineageEdge.builder()
                .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                .upstreamType(upstreamType)
                .upstreamId(upstreamId)
                .downstreamType(AssetType.EXPORT.getCode())
                .downstreamId(exportId)
                .relationType(relationType)
                .sourceType(1)
                .creator(operator)
                .modifier(operator)
                .build();
    }

    private ExportConfig parseExportConfig(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ExportConfig.class);
        } catch (Exception e) {
            log.warn("解析投递配置失败: {}", e.getMessage());
            return null;
        }
    }
}
