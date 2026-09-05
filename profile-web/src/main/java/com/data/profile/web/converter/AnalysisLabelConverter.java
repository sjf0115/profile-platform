package com.data.profile.web.converter;

import com.data.profile.web.dto.AnalysisLabelDTO;
import com.data.profile.web.vo.AnalysisLabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 可分析标签转换器
 */
public class AnalysisLabelConverter {

    private AnalysisLabelConverter() {
    }

    // DTO -> VO
    public static AnalysisLabelVO dto2vo(AnalysisLabelDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<AnalysisLabelVO> dto2voList(List<AnalysisLabelDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<AnalysisLabelDTO, AnalysisLabelVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        AnalysisLabelVO convert(AnalysisLabelDTO dto);
    }
}
