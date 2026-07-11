package com.data.profile.web.service.lineage;

import com.data.profile.web.dao.GroupMapper;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.enums.RelationType;
import com.data.profile.web.model.*;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.common.enums.ModelType;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 群组血缘抽取：group -> label / group
 * 复用 GroupService.collectMetadata 的遍历逻辑
 */
@Slf4j
@Component
public class GroupLineageExtractor implements LineageExtractor {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public String assetType() {
        return AssetType.GROUP.getCode();
    }

    @Override
    public List<LineageEdge> extractUpstreams(String assetId) {
        Group group = groupMapper.selectByGroupId(assetId);
        if (group == null || group.getGroupRule() == null) {
            return Collections.emptyList();
        }

        GroupRule rule = group.getGroupRule();
        RuleExpression expression = rule.getExpression();
        if (expression == null || expression.getRuleGroups() == null) {
            return Collections.emptyList();
        }

        Set<String> labelIds = new LinkedHashSet<>();
        Set<String> groupIds = new LinkedHashSet<>();

        // 遍历规则树，收集 label 和 group 引用（复用 collectMetadata 逻辑）
        for (RuleGroup ruleGroup : expression.getRuleGroups()) {
            if (ruleGroup.getRules() == null) continue;
            for (Rule r : ruleGroup.getRules()) {
                collectReferences(r, labelIds, groupIds);
            }
        }

        String operator = UserContextHolder.currentUserId();
        List<LineageEdge> edges = new ArrayList<>();

        // label 引用边
        for (String labelId : labelIds) {
            edges.add(LineageEdge.builder()
                    .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                    .upstreamType(AssetType.LABEL.getCode())
                    .upstreamId(labelId)
                    .downstreamType(AssetType.GROUP.getCode())
                    .downstreamId(assetId)
                    .relationType(RelationType.REFERENCE.getCode())
                    .sourceType(1)
                    .creator(operator)
                    .modifier(operator)
                    .build());
        }

        // group 引用边（排除自身）
        for (String gid : groupIds) {
            if (gid.equals(assetId)) continue; // 排除自引用
            edges.add(LineageEdge.builder()
                    .lineageId(IDGenerator.getInstance().generate(ModelType.LINEAGE))
                    .upstreamType(AssetType.GROUP.getCode())
                    .upstreamId(gid)
                    .downstreamType(AssetType.GROUP.getCode())
                    .downstreamId(assetId)
                    .relationType(RelationType.REFERENCE.getCode())
                    .sourceType(1)
                    .creator(operator)
                    .modifier(operator)
                    .build());
        }

        return edges;
    }

    /**
     * 从单条 Rule 的 filterExpression 中收集标签/群组引用
     * （抽取自 GroupService.collectMetadata）
     */
    private void collectReferences(Rule rule, Set<String> labelIds, Set<String> groupIds) {
        RuleFilterExpression filterExpression = rule.getFilterExpression();
        if (filterExpression == null || filterExpression.getFilterGroups() == null) return;
        for (RuleFilterGroup filterGroup : filterExpression.getFilterGroups()) {
            if (filterGroup.getFilters() == null) continue;
            for (RuleFilter filter : filterGroup.getFilters()) {
                if (filter.getType() == 1 && filter.getId() != null) {
                    labelIds.add(filter.getId());
                } else if (filter.getType() == 2 && filter.getId() != null) {
                    groupIds.add(filter.getId());
                }
            }
        }
    }
}
