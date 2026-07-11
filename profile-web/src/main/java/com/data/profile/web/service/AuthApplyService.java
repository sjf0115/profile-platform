package com.data.profile.web.service;

import com.data.profile.common.enums.ApplyStatus;
import com.data.profile.common.enums.GranteeType;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.web.dao.AuthApplyItemMapper;
import com.data.profile.web.dao.AuthApplyMapper;
import com.data.profile.web.model.AuthApply;
import com.data.profile.web.model.AuthApplyItem;
import com.data.profile.web.security.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限申请审批 Service
 */
@Slf4j
@Service
public class AuthApplyService {

    @Autowired
    private AuthApplyMapper authApplyMapper;

    @Autowired
    private AuthApplyItemMapper authApplyItemMapper;

    @Autowired
    private ResourceGrantService resourceGrantService;

    /**
     * 提交权限申请
     */
    @Transactional(rollbackFor = Exception.class)
    public String submit(String applyReason, List<AuthApplyItem> items) {
        String userId = UserContextHolder.currentUserId();
        String applyId = IDGenerator.getInstance().generate(ModelType.AUTH_APPLY);

        AuthApply apply = AuthApply.builder()
                .applyId(applyId)
                .applicant(userId)
                .applyReason(applyReason)
                .status(ApplyStatus.PENDING.getCode())
                .creator(userId)
                .build();
        authApplyMapper.insertSelective(apply);

        if (!CollectionUtils.isEmpty(items)) {
            List<AuthApplyItem> saveItems = new ArrayList<>();
            for (AuthApplyItem item : items) {
                item.setItemId(IDGenerator.getInstance().generate(ModelType.AUTH_APPLY));
                item.setApplyId(applyId);
                item.setCreator(userId);
                saveItems.add(item);
            }
            authApplyItemMapper.batchInsert(saveItems);
        }

        log.info("权限申请提交: applyId={}, applicant={}, items={}", applyId, userId,
                items == null ? 0 : items.size());
        return applyId;
    }

    /**
     * 我的申请列表
     */
    public List<AuthApply> mine() {
        String userId = UserContextHolder.currentUserId();
        return authApplyMapper.selectByApplicant(userId);
    }

    /**
     * 待审批列表
     */
    public List<AuthApply> todo() {
        return authApplyMapper.selectPendingList();
    }

    /**
     * 已审批列表（我审批过的）
     */
    public List<AuthApply> approved() {
        String userId = UserContextHolder.currentUserId();
        return authApplyMapper.selectApprovedList(userId);
    }

    /**
     * 申请明细
     */
    public List<AuthApplyItem> items(String applyId) {
        return authApplyItemMapper.selectByApplyId(applyId);
    }

    /**
     * 审批通过（事务内写 resource_grant）
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(String applyId, String approveRemark) {
        String userId = UserContextHolder.currentUserId();

        AuthApply apply = authApplyMapper.selectByApplyId(applyId);
        if (apply == null) {
            throw new RuntimeException("申请单不存在: " + applyId);
        }
        if (!ApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw new RuntimeException("申请单状态不允许审批: " + apply.getStatus());
        }

        // 更新状态
        authApplyMapper.updateStatus(applyId, ApplyStatus.APPROVED.getCode(), userId, approveRemark);

        // 写 resource_grant
        List<AuthApplyItem> items = authApplyItemMapper.selectByApplyId(applyId);
        for (AuthApplyItem item : items) {
            List<Integer> actions = new ArrayList<>();
            actions.add(item.getAction());
            resourceGrantService.grant(
                    item.getResourceType(),
                    java.util.Collections.singletonList(item.getResourceId()),
                    GranteeType.USER.getCode(),
                    apply.getApplicant(),
                    actions,
                    item.getExpireTime()
            );
        }

        log.info("权限申请审批通过: applyId={}, approver={}", applyId, userId);
    }

    /**
     * 审批拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(String applyId, String approveRemark) {
        String userId = UserContextHolder.currentUserId();

        AuthApply apply = authApplyMapper.selectByApplyId(applyId);
        if (apply == null) {
            throw new RuntimeException("申请单不存在: " + applyId);
        }
        if (!ApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw new RuntimeException("申请单状态不允许审批: " + apply.getStatus());
        }

        authApplyMapper.updateStatus(applyId, ApplyStatus.REJECTED.getCode(), userId, approveRemark);
        log.info("权限申请拒绝: applyId={}, approver={}", applyId, userId);
    }

    /**
     * 撤销申请（仅申请人可撤销，且状态为待审批时）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String applyId) {
        String userId = UserContextHolder.currentUserId();

        AuthApply apply = authApplyMapper.selectByApplyId(applyId);
        if (apply == null) {
            throw new RuntimeException("申请单不存在: " + applyId);
        }
        if (!apply.getApplicant().equals(userId)) {
            throw new RuntimeException("无权撤销他人申请");
        }
        if (!ApplyStatus.PENDING.getCode().equals(apply.getStatus())) {
            throw new RuntimeException("只有待审批状态可以撤销");
        }

        authApplyMapper.updateStatus(applyId, ApplyStatus.CANCELLED.getCode(), userId, "申请人撤销");
        log.info("权限申请撤销: applyId={}, applicant={}", applyId, userId);
    }
}
