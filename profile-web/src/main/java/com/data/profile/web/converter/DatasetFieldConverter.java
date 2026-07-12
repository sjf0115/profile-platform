package com.data.profile.web.converter;

import com.data.profile.web.dto.DatasetFieldDTO;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.vo.DatasetFieldVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：数据集字段转换器
 * 作者：SmartSi
 * 日期：2026/7/11
 */
public class DatasetFieldConverter {

    private DatasetFieldConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static DatasetFieldVO do2vo(DatasetField field) {
        if (field == null) {
            return null;
        }
        return DO2VoMapper.INSTANCE.convert(field);
    }

    // List<DO> -> List<VO>
    public static List<DatasetFieldVO> do2voList(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoMapper.INSTANCE.convertList(fields);
    }

    // DO -> DTO
    public static DatasetFieldDTO do2dto(DatasetField field) {
        if (field == null) {
            return null;
        }
        return DO2DTOMapper.INSTANCE.convert(field);
    }

    // List<DO> -> List<DTO>
    public static List<DatasetFieldDTO> do2dtoList(List<DatasetField> fields) {
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2DTOMapper.INSTANCE.convertList(fields);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DO -> VO
    @Mapper
    public interface DO2VoMapper extends BaseConverter<DatasetField, DatasetFieldVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        DatasetFieldVO convert(DatasetField field);
    }

    // DO -> DTO
    @Mapper
    public interface DO2DTOMapper extends BaseConverter<DatasetField, DatasetFieldDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        DatasetFieldDTO convert(DatasetField field);
    }
}
