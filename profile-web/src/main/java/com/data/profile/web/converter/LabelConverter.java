package com.data.profile.web.converter;

import com.data.profile.web.dto.LabelDTO;
import com.data.profile.web.dto.LabelParam;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.model.Label;
import com.data.profile.web.vo.LabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 标签转换器
 */
public class LabelConverter {

    private LabelConverter() {
    }

    // DO -> DTO
    public static LabelDTO do2dto(Label label) {
        if (label == null) return null;
        return DO2DTOMapper.INSTANCE.convert(label);
    }

    // List<DO> -> List<DTO>
    public static List<LabelDTO> do2dtoList(List<Label> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(list);
    }

    // DTO -> VO
    public static LabelVO dto2vo(LabelDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<LabelVO> dto2voList(List<LabelDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // Request -> DO
    public static Label request2do(LabelRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static Label param2do(LabelParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO（供 unbound/online 等内部场景使用，不填充名称）
    public static LabelVO do2vo(Label label) {
        if (label == null) return null;
        return DO2VoMapper.INSTANCE.convert(label);
    }

    // List<DO> -> List<VO>
    public static List<LabelVO> do2voList(List<Label> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2VoMapper.INSTANCE.convertList(list);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<Label, LabelDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        LabelDTO convert(Label label);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<LabelDTO, LabelVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        LabelVO convert(LabelDTO dto);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<LabelRequest, Label> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Label convert(LabelRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<LabelParam, Label> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Label convert(LabelParam param);
    }

    @Mapper
    public interface DO2VoMapper extends BaseConverter<Label, LabelVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        LabelVO convert(Label label);
    }
}
