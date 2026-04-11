package com.data.profile.web.dao;

import com.data.profile.web.model.Label;

import java.util.List;

public interface LabelMapper {
    // 查询
    Label selectByLabelId(String labelId); // 根据ID查询

    List<Label> selectByLabelName(String labelName); // 根据名字查询

    List<Label> selectByParams(Label label); //根据参数查询

    List<Label> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Label label);

    int insertSelective(Label label);

    // 删除
    int deleteByLabelId(String labelId);

    // 更新
    int updateByLabelId(Label label);

    int updateByLabelIdSelective(Label label);
}