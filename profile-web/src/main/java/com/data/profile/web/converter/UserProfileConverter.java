package com.data.profile.web.converter;

import com.data.profile.web.dto.UserProfileDTO;
import com.data.profile.web.vo.UserProfileVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 功能：用户画像转换器
 * 作者：SmartSi
 * 日期：2026/3/14
 */
public class UserProfileConverter {

    private UserProfileConverter() {
        // 静态工具类
    }

    // DTO -> VO
    public static UserProfileVO toVO(UserProfileDTO dto) {
        UserProfileVO vo = new UserProfileVO();
        if (dto != null) {
            vo = DTO2VOMapper.INSTANCE.convert(dto);
        }
        return vo;
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VOMapper extends BaseConverter<UserProfileDTO, UserProfileVO> {
        DTO2VOMapper INSTANCE = Mappers.getMapper(DTO2VOMapper.class);
        @Override
        UserProfileVO convert(UserProfileDTO dto);
    }
}
