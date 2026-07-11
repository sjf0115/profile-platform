package com.data.profile.web.service;

import com.data.profile.common.enums.GrantAction;
import com.data.profile.common.enums.GranteeType;
import com.data.profile.web.dao.*;
import com.data.profile.web.exception.PermissionDeniedException;
import com.data.profile.web.model.*;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据权限校验 Service
 */
@Slf4j
@Service
public class DataAuthService {

    @Autowired
    private ResourceGrantMapper resourceGrantMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private DataSourceMapper dataSourceMapper;
    @Autowired
    private ExportMapper exportMapper;
    @Autowired
    private GroupAnalysisMapper groupAnalysisMapper;
    @Autowired
    private ApplicationMapper applicationMapper;
    @Autowired
    private EventMapper eventMapper;

    /**
     * 校验当前用户对指定资源的数据权限，无权限抛 PermissionDeniedException
     */
    public void check(String resourceType, String resourceId, int action) {
        String userId = UserContextHolder.currentUserId();

        // 1. 超管放行
        if (UserContextHolder.isSuperAdmin()) {
            return;
        }

        // 2. owner/creator 放行
        if (isOwner(resourceType, resourceId, userId)) {
            return;
        }

        // 3. 查 ResourceGrant（用户直接授权 + 角色授权）
        if (hasGrant(resourceType, resourceId, userId, action)) {
            return;
        }

        // 4. READ 操作对内置资源放行（可扩展）
        if (action == GrantAction.READ.getCode()) {
            // 暂时不做内置资源判断，后续可扩展
        }

        throw new PermissionDeniedException("无数据权限：资源 " + resourceType + "/" + resourceId);
    }

    /**
     * 返回当前用户在指定资源类型下有权访问的资源ID集合。
     * 当结果超过 500 条时返回 null，调用方应改用 EXISTS 子查询。
     */
    public Set<String> listAuthorizedIds(String resourceType, int action) {
        String userId = UserContextHolder.currentUserId();

        if (UserContextHolder.isSuperAdmin()) {
            // 超管返回 null 表示不限制
            return null;
        }

        Set<String> result = new HashSet<>();

        // 用户直接授权
        List<String> userGrants = resourceGrantMapper.selectAuthorizedResourceIds(
                resourceType, GranteeType.USER.getCode(), userId, action);
        result.addAll(userGrants);

        // 角色授权（遍历用户所有角色）
        Set<String> roleIds = UserContextHolder.currentRoleIds();
        for (String roleId : roleIds) {
            List<String> roleGrants = resourceGrantMapper.selectAuthorizedResourceIds(
                    resourceType, GranteeType.ROLE.getCode(), roleId, action);
            result.addAll(roleGrants);
        }

        // 超过 500 条返回 null，由 Service 层改 EXISTS 子查询
        if (result.size() > 500) {
            return null;
        }
        return result;
    }

    /**
     * 判断用户是否资源 owner/creator
     */
    public boolean isOwner(String resourceType, String resourceId, String userId) {
        String owner = getOwner(resourceType, resourceId);
        String creator = getCreator(resourceType, resourceId);
        return userId.equals(owner) || userId.equals(creator);
    }

    // ===================== private =====================

    private boolean hasGrant(String resourceType, String resourceId, String userId, int action) {
        // 查用户直接授权
        List<ResourceGrant> userGrants = resourceGrantMapper.selectByResource(resourceType, resourceId);
        for (ResourceGrant g : userGrants) {
            if (g.getGranteeType() == GranteeType.USER.getCode()
                    && g.getGranteeId().equals(userId)
                    && (GrantAction.implies(g.getAction(), action))
                    && (g.getExpireTime() == null || g.getExpireTime().after(new Date()))) {
                return true;
            }
        }

        // 查角色授权
        Set<String> roleIds = UserContextHolder.currentRoleIds();
        for (ResourceGrant g : userGrants) {
            if (g.getGranteeType() == GranteeType.ROLE.getCode()
                    && roleIds.contains(g.getGranteeId())
                    && (GrantAction.implies(g.getAction(), action))
                    && (g.getExpireTime() == null || g.getExpireTime().after(new Date()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取资源 owner（根据资源类型分发）
     */
    private String getOwner(String resourceType, String resourceId) {
        switch (resourceType) {
            case "08": { Label m = labelMapper.selectByLabelId(resourceId); return m != null ? m.getOwner() : null; }
            case "09": { Group m = groupMapper.selectByGroupId(resourceId); return m != null ? m.getOwner() : null; }
            case "06": { Dataset m = datasetMapper.selectByDatasetId(resourceId); return m != null ? m.getOwner() : null; }
            case "05": { DataSource m = dataSourceMapper.selectByDatasourceId(resourceId); return m != null ? m.getOwner() : null; }
            case "10": { Export m = exportMapper.selectByExportId(resourceId); return m != null ? m.getOwner() : null; }
            case "22": { GroupAnalysis m = groupAnalysisMapper.selectByAnalysisId(resourceId); return m != null ? m.getOwner() : null; }
            case "21": { Application m = applicationMapper.selectByAppKey(resourceId); return m != null ? m.getOwner() : null; }
            case "12": { Event m = eventMapper.selectByEventId(resourceId); return m != null ? m.getOwner() : null; }
            default: return null;
        }
    }

    /**
     * 获取资源 creator（根据资源类型分发）
     */
    private String getCreator(String resourceType, String resourceId) {
        switch (resourceType) {
            case "08": { Label m = labelMapper.selectByLabelId(resourceId); return m != null ? m.getCreator() : null; }
            case "09": { Group m = groupMapper.selectByGroupId(resourceId); return m != null ? m.getCreator() : null; }
            case "06": { Dataset m = datasetMapper.selectByDatasetId(resourceId); return m != null ? m.getCreator() : null; }
            case "05": { DataSource m = dataSourceMapper.selectByDatasourceId(resourceId); return m != null ? m.getCreator() : null; }
            case "10": { Export m = exportMapper.selectByExportId(resourceId); return m != null ? m.getCreator() : null; }
            case "22": { GroupAnalysis m = groupAnalysisMapper.selectByAnalysisId(resourceId); return m != null ? m.getCreator() : null; }
            case "21": { Application m = applicationMapper.selectByAppKey(resourceId); return m != null ? m.getCreator() : null; }
            case "12": { Event m = eventMapper.selectByEventId(resourceId); return m != null ? m.getCreator() : null; }
            default: return null;
        }
    }
}
