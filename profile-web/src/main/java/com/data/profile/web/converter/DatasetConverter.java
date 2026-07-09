package com.data.profile.web.converter;

import com.data.profile.web.dto.DatasetDTO;
import com.data.profile.web.dto.DatasetParam;
import com.data.profile.web.dto.DatasetRequest;
import com.data.profile.web.model.Dataset;
import com.data.profile.web.vo.DatasetVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：数据集转换器
 * 作者：SmartSi
 * 日期：2026/7/8
 */
public class DatasetConverter {

    private DatasetConverter() {
        // 静态工具类
    }

    // DTO -> VO
    public static DatasetVO dto2vo(DatasetDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoConverterMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<DatasetVO> dto2voList(List<DatasetDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoConverterMapper.INSTANCE.convertList(dtos);
    }

    // DO -> VO
    public static DatasetVO do2vo(Dataset dataset) {
        if (dataset == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(dataset);
    }

    // List<DO> -> List<VO>
    public static List<DatasetVO> do2voList(List<Dataset> datasets) {
        if (datasets == null || datasets.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(datasets);
    }

    // Param -> DO
    public static Dataset param2do(DatasetParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // Request -> DO
    public static Dataset request2do(DatasetRequest request) {
        if (request == null) {
            return null;
        }
        return Request2DOMapper.INSTANCE.convert(request);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DTO -> VO
    @Mapper
    public interface DTO2VoConverterMapper extends BaseConverter<DatasetDTO, DatasetVO> {
        DTO2VoConverterMapper INSTANCE = Mappers.getMapper(DTO2VoConverterMapper.class);
        @Override
        DatasetVO convert(DatasetDTO dto);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Dataset, DatasetVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        DatasetVO convert(Dataset dataset);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<DatasetParam, Dataset> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);

        @Override
        Dataset convert(DatasetParam param);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOMapper extends BaseConverter<DatasetRequest, Dataset> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);

        @Override
        Dataset convert(DatasetRequest request);
    }
}
