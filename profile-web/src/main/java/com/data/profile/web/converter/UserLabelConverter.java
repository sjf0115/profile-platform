package com.data.profile.web.converter;

import com.data.profile.web.dto.UserLabelDTO;
import com.data.profile.web.model.UserLabel;
import com.data.profile.web.vo.UserLabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：用户标签转换器
 * 作者：SmartSi
 * 日期：2026/3/14
 */
public class UserLabelConverter {

    private UserLabelConverter() {
        // 静态工具类
    }

    // DO -> DTO
    public static UserLabelDTO do2dto(UserLabel userLabel) {
        if (userLabel == null) {
            return null;
        }
        return DO2DTOMapper.INSTANCE.convert(userLabel);
    }

    // DTO -> VO
    public static UserLabelVO dto2vo(UserLabelDTO userLabelDTO) {
        if (userLabelDTO == null) {
            return null;
        }
        return DTO2VOMapper.INSTANCE.convert(userLabelDTO);
    }

    // List<DTO> -> List<VO>
    public static List<UserLabelVO> dto2voList(List<UserLabelDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VOMapper.INSTANCE.convertList(dtos);
    }

    // DO -> VO
    public static UserLabelVO do2vo(UserLabel userLabel) {
        if (userLabel == null) {
            return null;
        }
        return DO2VOMapper.INSTANCE.convert(userLabel);
    }

    // List<DO> -> List<VO>
    public static List<UserLabelVO> do2voList(List<UserLabel> userLabels) {
        if (userLabels == null || userLabels.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VOMapper.INSTANCE.convertList(userLabels);
    }

    // DO -> DTO
    @Mapper
    public interface DO2DTOMapper extends BaseConverter<UserLabel, UserLabelDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        UserLabelDTO convert(UserLabel userLabel);
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VOMapper extends BaseConverter<UserLabelDTO, UserLabelVO> {
        DTO2VOMapper INSTANCE = Mappers.getMapper(DTO2VOMapper.class);
        @Override
        UserLabelVO convert(UserLabelDTO userLabelDTO);
    }

    // DO -> VO
    @Mapper
    public interface DO2VOMapper extends BaseConverter<UserLabel, UserLabelVO> {
        DO2VOMapper INSTANCE = Mappers.getMapper(DO2VOMapper.class);
        @Override
        UserLabelVO convert(UserLabel userLabel);
    }
}
