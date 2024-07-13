package com.data.profile.dao;

import com.data.profile.model.LabelCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelCategoryMapper {
    int deleteByCategoryId(String categoryId);

    int insert(LabelCategory row);

    int insertSelective(LabelCategory row);

    LabelCategory selectByPrimaryKey(Long id);

    LabelCategory selectByCategoryId(String categoryId);

    List<LabelCategory> selectByCategoryName(String categoryName);

    List<LabelCategory> selectByParams(LabelCategory category);

    int updateByCategoryIdSelective(LabelCategory row);

    int updateByCategoryId(LabelCategory row);

    Integer selectMaxSeqByParentId(String parentCategoryId);

    Integer selectLevelByCategoryId(String categoryId);

    int selectMaxCategoryId();
}