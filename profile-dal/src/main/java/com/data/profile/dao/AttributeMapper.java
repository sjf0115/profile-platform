package com.data.profile.dao;

import com.data.profile.model.Attribute;

import java.util.List;

public interface AttributeMapper {
    // 查询
    Attribute selectByAttrId(String attrId); // 根据ID查询

    List<Attribute> selectByAttrName(String attrName); // 根据名字查询

    List<Attribute> selectByParams(Attribute attr); //根据参数查询

    List<Attribute> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Attribute attr);

    int insertSelective(Attribute attr);

    // 删除
    int deleteByAttrId(String attrId);

    // 更新
    int updateByAttrId(Attribute attr);

    int updateByAttrIdSelective(Attribute attr);
}