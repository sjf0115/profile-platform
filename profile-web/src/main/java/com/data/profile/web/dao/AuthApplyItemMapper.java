package com.data.profile.web.dao;

import com.data.profile.web.model.AuthApplyItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限申请明细 Mapper
 */
@Mapper
public interface AuthApplyItemMapper {

    int insertSelective(AuthApplyItem record);

    int batchInsert(@Param("list") List<AuthApplyItem> list);

    List<AuthApplyItem> selectByApplyId(@Param("applyId") String applyId);
}
