package com.data.profile.web.converter;

import com.data.profile.web.dto.LabelValueDistributionDTO;
import com.data.profile.web.vo.LabelValueDistributionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 标签取值分布转换器
 */
public class LabelValueDistributionConverter {

    private LabelValueDistributionConverter() {
    }

    // DTO -> VO（嵌套 Item 由 MapStruct 自动映射）
    public static LabelValueDistributionVO dto2vo(LabelValueDistributionDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<LabelValueDistributionVO> dto2voList(List<LabelValueDistributionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<LabelValueDistributionDTO, LabelValueDistributionVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        LabelValueDistributionVO convert(LabelValueDistributionDTO dto);
    }
}
