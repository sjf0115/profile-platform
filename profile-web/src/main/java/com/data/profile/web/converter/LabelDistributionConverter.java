package com.data.profile.web.converter;

import com.data.profile.web.dto.LabelDistributionDTO;
import com.data.profile.web.vo.LabelDistributionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 群组标签分布转换器
 */
public class LabelDistributionConverter {

    private LabelDistributionConverter() {
    }

    // DTO -> VO（嵌套 DistributionItem 由 MapStruct 自动映射）
    public static LabelDistributionVO dto2vo(LabelDistributionDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<LabelDistributionVO> dto2voList(List<LabelDistributionDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<LabelDistributionDTO, LabelDistributionVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        LabelDistributionVO convert(LabelDistributionDTO dto);
    }
}
