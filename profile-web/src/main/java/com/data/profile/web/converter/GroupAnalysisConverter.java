package com.data.profile.web.converter;

import com.data.profile.web.model.GroupAnalysis;
import com.data.profile.web.model.Label;
import com.data.profile.web.vo.AnalysisLabelVO;
import com.data.profile.web.vo.GroupAnalysisVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：群组分析转换器
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class GroupAnalysisConverter {

    private GroupAnalysisConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static GroupAnalysisVO do2vo(GroupAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        return DO2AnalysisVOMapper.INSTANCE.convert(analysis);
    }

    // List<DO> -> List<VO>
    public static List<GroupAnalysisVO> do2voList(List<GroupAnalysis> analyses) {
        if (analyses == null || analyses.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2AnalysisVOMapper.INSTANCE.convertList(analyses);
    }

    // Label DO -> AnalysisLabelVO
    public static AnalysisLabelVO labelDo2vo(Label label) {
        if (label == null) {
            return null;
        }
        return DO2VOMapper.INSTANCE.convert(label);
    }

    // GroupAnalysis DO -> GroupAnalysisVO
    @Mapper
    public interface DO2AnalysisVOMapper extends BaseConverter<GroupAnalysis, GroupAnalysisVO> {
        DO2AnalysisVOMapper INSTANCE = Mappers.getMapper(DO2AnalysisVOMapper.class);

        @Override
        @Mapping(target = "compareGroupIds", ignore = true)
        @Mapping(target = "labelIds", ignore = true)
        GroupAnalysisVO convert(GroupAnalysis analysis);
    }

    // Label DO -> AnalysisLabelVO
    @Mapper
    public interface DO2VOMapper extends BaseConverter<Label, AnalysisLabelVO> {
        DO2VOMapper INSTANCE = Mappers.getMapper(DO2VOMapper.class);

        @Override
        AnalysisLabelVO convert(Label label);
    }
}
