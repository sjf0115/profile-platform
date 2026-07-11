package com.data.profile.web.dao;

import com.data.profile.web.model.AuthApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限申请 Mapper
 */
@Mapper
public interface AuthApplyMapper {

    int insertSelective(AuthApply record);

    AuthApply selectByApplyId(@Param("applyId") String applyId);

    /** 我的申请（按申请人） */
    List<AuthApply> selectByApplicant(@Param("applicant") String applicant);

    /** 待审批列表（按资源 owner/MANAGE 授权人过滤，简化实现：查所有 PENDING） */
    List<AuthApply> selectPendingList();

    /** 已审批列表 */
    List<AuthApply> selectApprovedList(@Param("approver") String approver);

    int updateStatus(@Param("applyId") String applyId,
                     @Param("status") Integer status,
                     @Param("approver") String approver,
                     @Param("approveRemark") String approveRemark);
}
