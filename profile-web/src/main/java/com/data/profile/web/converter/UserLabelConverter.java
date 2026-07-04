package com.data.profile.web.converter;

import com.data.profile.web.dto.UserLabelDTO;
import com.data.profile.web.model.UserLabel;
import com.data.profile.web.vo.UserLabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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
    public static UserLabelDTO toDTO(UserLabel userLabel) {
        UserLabelDTO dto = new UserLabelDTO();
        if (userLabel != null) {
            dto = DO2DTOMapper.INSTANCE.convert(userLabel);
        }
        return dto;
    }

    // DTO -> VO
    public static UserLabelVO toVO(UserLabelDTO userLabelDTO) {
        UserLabelVO vo = new UserLabelVO();
        if (userLabelDTO != null) {
            vo = DTO2VOMapper.INSTANCE.convert(userLabelDTO);
        }
        return vo;
    }

    // DO -> VO
    public static UserLabelVO toVO(UserLabel userLabel) {
        UserLabelVO vo = new UserLabelVO();
        if (userLabel != null) {
            vo = DO2VOMapper.INSTANCE.convert(userLabel);
        }
        return vo;
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
