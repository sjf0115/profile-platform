package com.data.profile.web.converter;

import com.data.profile.web.dto.DataSourceDTO;
import com.data.profile.web.dto.DataSourceParam;
import com.data.profile.web.dto.DataSourceRequest;
import com.data.profile.web.model.DataSource;
import com.data.profile.web.vo.DataSourceVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 数据源转换器
 */
public class DataSourceConverter {

    private DataSourceConverter() {
    }

    // DO -> DTO
    public static DataSourceDTO do2dto(DataSource dataSource) {
        if (dataSource == null) return null;
        return DO2DTOMapper.INSTANCE.convert(dataSource);
    }

    // List<DO> -> List<DTO>
    public static List<DataSourceDTO> do2dtoList(List<DataSource> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(list);
    }

    // DTO -> VO
    public static DataSourceVO dto2vo(DataSourceDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<DataSourceVO> dto2voList(List<DataSourceDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // DTO -> DO
    public static DataSource dto2do(DataSourceDTO dataSourceDTO) {
        if (dataSourceDTO == null) return null;
        return DTO2DOMapper.INSTANCE.convert(dataSourceDTO);
    }

    // Request -> DO
    public static DataSource request2do(DataSourceRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static DataSource param2do(DataSourceParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO
    public static DataSourceVO do2vo(DataSource dataSource) {
        if (dataSource == null) return null;
        return DO2VoMapper.INSTANCE.convert(dataSource);
    }

    // List<DO> -> List<VO>
    public static List<DataSourceVO> do2voList(List<DataSource> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2VoMapper.INSTANCE.convertList(list);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<DataSource, DataSourceDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        DataSourceDTO convert(DataSource dataSource);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<DataSourceDTO, DataSourceVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        DataSourceVO convert(DataSourceDTO dto);
    }

    @Mapper
    public interface DTO2DOMapper extends BaseConverter<DataSourceDTO, DataSource> {
        DTO2DOMapper INSTANCE = Mappers.getMapper(DTO2DOMapper.class);
        @Override
        DataSource convert(DataSourceDTO dataSourceDTO);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<DataSourceRequest, DataSource> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        DataSource convert(DataSourceRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<DataSourceParam, DataSource> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        DataSource convert(DataSourceParam param);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoMapper extends BaseConverter<DataSource, DataSourceVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        DataSourceVO convert(DataSource dataSource);
    }
}
