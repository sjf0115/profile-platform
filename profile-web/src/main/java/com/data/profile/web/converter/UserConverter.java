package com.data.profile.web.converter;

import com.data.profile.common.enums.SourceType;
import com.data.profile.web.dto.UserRequest;
import com.data.profile.web.model.User;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 功能：用户转换器
 * 作者：SmartSi
 * 日期：2026/3/14
 */
public class UserConverter {

    private UserConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static UserVO convert(User user) {
        UserVO userVO = new UserVO();
        if (user != null) {
            userVO = DO2VoConverterMapper.INSTANCE.convert(user);
        }
        return userVO;
    }

    // DTO -> DO
    public static User convert(UserRequest userRequest) {
        User user = new User();
        if (userRequest != null) {
            user = DTO2DOConverterMapper.INSTANCE.convert(userRequest);
            user.setCreator(UserContextHolder.currentUserId());
            user.setModifier(UserContextHolder.currentUserId());
            user.setSourceType(SourceType.CUSTOM.getCode());
        }
        return user;
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<User, UserVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        UserVO convert(User user);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOConverterMapper extends BaseConverter<UserRequest, User> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        User convert(UserRequest request);
    }
}
