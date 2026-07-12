package com.data.profile.web.converter;

import com.data.profile.web.dto.GroupDTO;
import com.data.profile.web.dto.GroupParam;
import com.data.profile.web.dto.GroupRequest;
import com.data.profile.web.model.Group;
import com.data.profile.web.vo.GroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：群组转换器
 * 作者：SmartSi
 * 日期：2026/7/8
 */
public class GroupConverter {

    private GroupConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static GroupVO do2vo(Group group) {
        if (group == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(group);
    }

    // List<DO> -> List<VO>
    public static List<GroupVO> do2voList(List<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(groups);
    }

    // DO -> DTO
    public static GroupDTO do2dto(Group group) {
        if (group == null) {
            return null;
        }
        return DO2DTOConverterMapper.INSTANCE.convert(group);
    }

    // List<DO> -> List<DTO>
    public static List<GroupDTO> do2dtoList(List<Group> groups) {
        if (groups == null || groups.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2DTOConverterMapper.INSTANCE.convertList(groups);
    }

    // DTO -> DO
    public static Group dto2do(GroupDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2DOConverterMapper.INSTANCE.convert(dto);
    }

    // Param -> DO
    public static Group param2do(GroupParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // Request -> DO
    public static Group request2do(GroupRequest request) {
        if (request == null) {
            return null;
        }
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // DTO -> VO
    public static GroupVO dto2vo(GroupDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoConverterMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<GroupVO> dto2voList(List<GroupDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoConverterMapper.INSTANCE.convertList(dtos);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DO -> DTO
    @Mapper
    public interface DO2DTOConverterMapper extends BaseConverter<Group, GroupDTO> {
        DO2DTOConverterMapper INSTANCE = Mappers.getMapper(DO2DTOConverterMapper.class);
        @Override
        GroupDTO convert(Group group);
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VoConverterMapper extends BaseConverter<GroupDTO, GroupVO> {
        DTO2VoConverterMapper INSTANCE = Mappers.getMapper(DTO2VoConverterMapper.class);
        @Override
        GroupVO convert(GroupDTO dto);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Group, GroupVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        GroupVO convert(Group group);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOConverterMapper extends BaseConverter<GroupDTO, Group> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        Group convert(GroupDTO dto);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<GroupParam, Group> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);

        @Override
        Group convert(GroupParam param);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOMapper extends BaseConverter<GroupRequest, Group> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);

        @Override
        Group convert(GroupRequest request);
    }
}
