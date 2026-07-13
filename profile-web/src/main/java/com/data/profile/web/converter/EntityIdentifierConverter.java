package com.data.profile.web.converter;

import com.data.profile.web.dto.EntityIdentifierDTO;
import com.data.profile.web.dto.EntityIdentifierParam;
import com.data.profile.web.dto.EntityIdentifierRequest;
import com.data.profile.web.model.EntityIdentifier;
import com.data.profile.web.vo.EntityIdentifierVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 实体标识转换器
 */
public class EntityIdentifierConverter {

    private EntityIdentifierConverter() {
    }

    // DO -> DTO
    public static EntityIdentifierDTO do2dto(EntityIdentifier entityIdentifier) {
        if (entityIdentifier == null) return null;
        return DO2DTOMapper.INSTANCE.convert(entityIdentifier);
    }

    // List<DO> -> List<DTO>
    public static List<EntityIdentifierDTO> do2dtoList(List<EntityIdentifier> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(list);
    }

    // DTO -> VO
    public static EntityIdentifierVO dto2vo(EntityIdentifierDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<EntityIdentifierVO> dto2voList(List<EntityIdentifierDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // Request -> DO
    public static EntityIdentifier request2do(EntityIdentifierRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static EntityIdentifier param2do(EntityIdentifierParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO
    public static EntityIdentifierVO do2vo(EntityIdentifier entityIdentifier) {
        if (entityIdentifier == null) return null;
        return DO2VoMapper.INSTANCE.convert(entityIdentifier);
    }

    // List<DO> -> List<VO>
    public static List<EntityIdentifierVO> do2voList(List<EntityIdentifier> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2VoMapper.INSTANCE.convertList(list);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<EntityIdentifier, EntityIdentifierDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        EntityIdentifierDTO convert(EntityIdentifier entityIdentifier);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<EntityIdentifierDTO, EntityIdentifierVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        EntityIdentifierVO convert(EntityIdentifierDTO dto);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<EntityIdentifierRequest, EntityIdentifier> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        EntityIdentifier convert(EntityIdentifierRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<EntityIdentifierParam, EntityIdentifier> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        EntityIdentifier convert(EntityIdentifierParam param);
    }

    @Mapper
    public interface DO2VoMapper extends BaseConverter<EntityIdentifier, EntityIdentifierVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        EntityIdentifierVO convert(EntityIdentifier entityIdentifier);
    }
}
