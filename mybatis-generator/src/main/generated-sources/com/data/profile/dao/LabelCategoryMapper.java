package com.data.profile.dao;

import com.data.profile.model.LabelCategory;

public interface LabelCategoryMapper {
    int deleteByPrimaryKey(Long id);

    int insert(LabelCategory row);

    int insertSelective(LabelCategory row);

    LabelCategory selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(LabelCategory row);

    int updateByPrimaryKey(LabelCategory row);
}