package com.data.profile.web.dao;

import com.data.profile.web.model.ResourceGrant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源授权 Mapper
 */
@Mapper
public interface ResourceGrantMapper {

    /**
     * 按资源查询授权记录
     */
    List<ResourceGrant> selectByResource(@Param("resourceType") String resourceType,
                                         @Param("resourceId") String resourceId);

    /**
     * 按受权者查询授权记录
     */
    List<ResourceGrant> selectByGrantee(@Param("granteeType") Integer granteeType,
                                        @Param("granteeId") String granteeId);

    /**
     * 插入
     */
    int insertSelective(ResourceGrant grant);

    /**
     * 按 grantId 删除
     */
    int deleteByGrantId(@Param("grantId") String grantId);

    /**
     * 按资源删除全部授权
     */
    int deleteByResource(@Param("resourceType") String resourceType,
                          @Param("resourceId") String resourceId);

    /**
     * 查询用户在指定资源类型下被授权的资源ID列表
     */
    List<String> selectAuthorizedResourceIds(@Param("resourceType") String resourceType,
                                              @Param("granteeType") Integer granteeType,
                                              @Param("granteeId") String granteeId,
                                              @Param("action") Integer action);

    /**
     * 批量查询用户在指定资源ID列表中有权访问的ID
     */
    List<String> selectAuthorizedIdsFromList(@Param("resourceType") String resourceType,
                                              @Param("resourceIds") List<String> resourceIds,
                                              @Param("granteeType") Integer granteeType,
                                              @Param("granteeId") String granteeId,
                                              @Param("action") Integer action);
}
