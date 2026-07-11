package com.data.profile.web.service;

import com.data.profile.common.enums.GranteeType;
import com.data.profile.common.enums.GrantAction;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.dao.ResourceGrantMapper;
import com.data.profile.web.model.ResourceGrant;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 资源授权管理 Service
 */
@Slf4j
@Service
public class ResourceGrantService {

    @Autowired
    private ResourceGrantMapper resourceGrantMapper;

    /**
     * 批量授权
     */
    public int grant(String resourceType, List<String> resourceIds,
                     int granteeType, String granteeId,
                     List<Integer> actions, Date expireTime) {
        String creator = UserContextHolder.currentUserId();
        int count = 0;
        for (String resourceId : resourceIds) {
            for (Integer action : actions) {
                ResourceGrant grant = ResourceGrant.builder()
                        .grantId(IDGenerator.getInstance().generate(ModelType.GRANT))
                        .resourceType(resourceType)
                        .resourceId(resourceId)
                        .granteeType(granteeType)
                        .granteeId(granteeId)
                        .action(action)
                        .expireTime(expireTime)
                        .creator(creator)
                        .build();
                count += resourceGrantMapper.insertSelective(grant);
            }
        }
        return count;
    }

    /**
     * 创建者自动授 MANAGE 权限（资源创建时调用）
     */
    public void grantOwner(String resourceType, String resourceId, String userId) {
        ResourceGrant grant = ResourceGrant.builder()
                .grantId(IDGenerator.getInstance().generate(ModelType.GRANT))
                .resourceType(resourceType)
                .resourceId(resourceId)
                .granteeType(GranteeType.USER.getCode())
                .granteeId(userId)
                .action(GrantAction.MANAGE.getCode())
                .creator(userId)
                .build();
        resourceGrantMapper.insertSelective(grant);
        log.info("自动授权: user={} -> resource={}/{} action=MANAGE", userId, resourceType, resourceId);
    }

    /**
     * 回收授权
     */
    public int revoke(String grantId) {
        return resourceGrantMapper.deleteByGrantId(grantId);
    }

    /**
     * 授权台账查询
     */
    public List<ResourceGrant> listByResource(String resourceType, String resourceId) {
        return resourceGrantMapper.selectByResource(resourceType, resourceId);
    }

    /**
     * 按受权者查询
     */
    public List<ResourceGrant> listByGrantee(int granteeType, String granteeId) {
        return resourceGrantMapper.selectByGrantee(granteeType, granteeId);
    }
}
