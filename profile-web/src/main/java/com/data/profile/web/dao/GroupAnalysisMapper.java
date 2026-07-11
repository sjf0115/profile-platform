package com.data.profile.web.dao;

import com.data.profile.web.model.GroupAnalysis;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupAnalysisMapper {
    // 查询
    GroupAnalysis selectByAnalysisId(String analysisId);

    List<GroupAnalysis> selectByAnalysisName(String analysisName);

    List<GroupAnalysis> selectByParams(GroupAnalysis query);

    List<GroupAnalysis> selectByGroupId(String groupId);

    // 插入
    int insertSelective(GroupAnalysis analysis);

    // 删除
    int deleteByAnalysisId(String analysisId);

    // 更新
    int updateByAnalysisIdSelective(GroupAnalysis analysis);
}
