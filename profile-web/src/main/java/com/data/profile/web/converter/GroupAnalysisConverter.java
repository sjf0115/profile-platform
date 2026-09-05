package com.data.profile.web.converter;

import com.data.profile.web.dto.GroupAnalysisDTO;
import com.data.profile.web.model.GroupAnalysis;
import com.data.profile.web.vo.GroupAnalysisVO;
import com.data.profile.common.utils.JSONUtils;
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

    // DO -> VO（含 JSON 数组字段解析：compareGroupIds/labelIds String -> List）
    public static GroupAnalysisVO do2vo(GroupAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        GroupAnalysisVO vo = DO2AnalysisVOMapper.INSTANCE.convert(analysis);
        vo.setCompareGroupIds(parseJsonArray(analysis.getCompareGroupIds()));
        vo.setLabelIds(parseJsonArray(analysis.getLabelIds()));
        return vo;
    }

    // List<DO> -> List<VO>
    public static List<GroupAnalysisVO> do2voList(List<GroupAnalysis> analyses) {
        if (analyses == null || analyses.isEmpty()) {
            return Collections.emptyList();
        }
        return analyses.stream().map(GroupAnalysisConverter::do2vo).collect(java.util.stream.Collectors.toList());
    }

    // DO -> DTO（含 JSON 数组字段解析：compareGroupIds/labelIds String -> List）
    public static GroupAnalysisDTO do2dto(GroupAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        GroupAnalysisDTO dto = DO2DTOMapper.INSTANCE.convert(analysis);
        dto.setCompareGroupIds(parseJsonArray(analysis.getCompareGroupIds()));
        dto.setLabelIds(parseJsonArray(analysis.getLabelIds()));
        return dto;
    }

    // List<DO> -> List<DTO>
    public static List<GroupAnalysisDTO> do2dtoList(List<GroupAnalysis> analyses) {
        if (analyses == null || analyses.isEmpty()) {
            return Collections.emptyList();
        }
        return analyses.stream().map(GroupAnalysisConverter::do2dto).collect(java.util.stream.Collectors.toList());
    }

    // DTO -> DO
    public static GroupAnalysis dto2do(GroupAnalysisDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2DOMapper.INSTANCE.convert(dto);
    }

    // DTO -> VO
    public static GroupAnalysisVO dto2vo(GroupAnalysisDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<GroupAnalysisVO> dto2voList(List<GroupAnalysisDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // 解析 JSON 数组字符串为 List<String>（容错返回空列表）
    private static List<String> parseJsonArray(String json) {
        return JSONUtils.toList(json, String.class);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DO -> DTO
    @Mapper
    public interface DO2DTOMapper extends BaseConverter<GroupAnalysis, GroupAnalysisDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        @Mapping(target = "compareGroupIds", ignore = true)
        @Mapping(target = "labelIds", ignore = true)
        GroupAnalysisDTO convert(GroupAnalysis analysis);
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VoMapper extends BaseConverter<GroupAnalysisDTO, GroupAnalysisVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        GroupAnalysisVO convert(GroupAnalysisDTO dto);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOMapper extends BaseConverter<GroupAnalysisDTO, GroupAnalysis> {
        DTO2DOMapper INSTANCE = Mappers.getMapper(DTO2DOMapper.class);
        @Override
        @Mapping(target = "compareGroupIds", ignore = true)
        @Mapping(target = "labelIds", ignore = true)
        GroupAnalysis convert(GroupAnalysisDTO dto);
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
}
