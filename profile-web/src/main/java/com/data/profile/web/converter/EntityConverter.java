package com.data.profile.web.converter;

import com.data.profile.web.dto.EntityDTO;
import com.data.profile.web.dto.EntityParam;
import com.data.profile.web.dto.EntityRequest;
import com.data.profile.web.model.Entity;
import com.data.profile.web.vo.EntityVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 实体转换器
 */
public class EntityConverter {

    private EntityConverter() {
    }

    // DO -> DTO
    public static EntityDTO do2dto(Entity entity) {
        if (entity == null) return null;
        return DO2DTOMapper.INSTANCE.convert(entity);
    }

    // List<DO> -> List<DTO>
    public static List<EntityDTO> do2dtoList(List<Entity> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(list);
    }

    // DTO -> VO
    public static EntityVO dto2vo(EntityDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<EntityVO> dto2voList(List<EntityDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // Request -> DO
    public static Entity request2do(EntityRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static Entity param2do(EntityParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO
    public static EntityVO do2vo(Entity entity) {
        if (entity == null) return null;
        return DO2VoMapper.INSTANCE.convert(entity);
    }

    // List<DO> -> List<VO>
    public static List<EntityVO> do2voList(List<Entity> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2VoMapper.INSTANCE.convertList(list);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<Entity, EntityDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        EntityDTO convert(Entity entity);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<EntityDTO, EntityVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        EntityVO convert(EntityDTO dto);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<EntityRequest, Entity> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Entity convert(EntityRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<EntityParam, Entity> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Entity convert(EntityParam param);
    }

    @Mapper
    public interface DO2VoMapper extends BaseConverter<Entity, EntityVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        EntityVO convert(Entity entity);
    }
}
