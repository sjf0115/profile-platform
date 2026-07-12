package com.data.profile.web.converter;

import com.data.profile.web.dto.ExportDTO;
import com.data.profile.web.dto.ExportParam;
import com.data.profile.web.dto.ExportRequest;
import com.data.profile.web.model.Export;
import com.data.profile.web.vo.ExportVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 投递转换器
 */
public class ExportConverter {

    private ExportConverter() {
    }

    // DO -> DTO
    public static ExportDTO do2dto(Export export) {
        if (export == null) return null;
        return DO2DTOMapper.INSTANCE.convert(export);
    }

    // List<DO> -> List<DTO>
    public static List<ExportDTO> do2dtoList(List<Export> exports) {
        if (exports == null || exports.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(exports);
    }

    // DTO -> VO
    public static ExportVO dto2vo(ExportDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<ExportVO> dto2voList(List<ExportDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // Request -> DO
    public static Export request2do(ExportRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static Export param2do(ExportParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<Export, ExportDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        ExportDTO convert(Export export);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<ExportDTO, ExportVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        ExportVO convert(ExportDTO dto);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<ExportRequest, Export> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Export convert(ExportRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<ExportParam, Export> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Export convert(ExportParam param);
    }
}
