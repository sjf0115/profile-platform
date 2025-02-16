package com.data.profile.dao;

import com.data.profile.model.LabelCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelCategoryMapper {

    LabelCategory selectByCategoryId(String categoryId);

    List<LabelCategory> selectByCategoryName(String categoryName);

    List<LabelCategory> selectByParams(LabelCategory category);

    List<LabelCategory> selectByParentId(String parentCategoryId);

    Integer selectMaxSeqByParentId(String parentCategoryId);

    // 查询标签类目的层级
    Integer selectLevelByCategoryId(String categoryId);

    //LabelCategory selectDefaultByCategoryLevel(int categoryLevel);

    int selectMaxCategoryId();

    int insert(LabelCategory row);

    int insertSelective(LabelCategory row);

    int updateByCategoryIdSelective(LabelCategory row);

    int updateByCategoryId(LabelCategory row);

    int deleteByCategoryId(String categoryId);
}