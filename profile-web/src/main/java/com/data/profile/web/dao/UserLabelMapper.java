package com.data.profile.web.dao;

import com.data.profile.web.dto.UserLabelDTO;
import com.data.profile.web.model.UserLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 功能：用户标签Mapper
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Mapper
public interface UserLabelMapper {

    /**
     * 查询用户标签及关联信息（JOIN Label + LabelCategory）
     */
    List<UserLabelDTO> selectUserLabelsWithCategory(@Param("userId") String userId);

    /**
     * 根据用户ID查询用户标签列表
     */
    List<UserLabel> selectByUserId(@Param("userId") String userId);

    /**
     * 新增用户标签
     */
    int insertSelective(UserLabel userLabel);

    /**
     * 更新标签值
     */
    int updateLabelValue(UserLabel userLabel);

    /**
     * 批量更新类目排序
     */
    int batchUpdateSortOrder(@Param("userId") String userId,
                             @Param("categoryIds") List<String> categoryIds);

    /**
     * 删除用户某个类目下的所有标签
     */
    int deleteByUserAndCategory(@Param("userId") String userId,
                                @Param("categoryId") String categoryId);

    /**
     * 删除用户单个标签
     */
    int deleteByUserAndLabel(@Param("userId") String userId,
                             @Param("labelId") String labelId);
}
