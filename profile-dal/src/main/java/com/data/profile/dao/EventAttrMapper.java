package com.data.profile.dao;

import com.data.profile.model.EventAttr;

public interface EventAttrMapper {
    int deleteByPrimaryKey(Long id);

    int insert(EventAttr row);

    int insertSelective(EventAttr row);

    EventAttr selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(EventAttr row);

    int updateByPrimaryKey(EventAttr row);
}