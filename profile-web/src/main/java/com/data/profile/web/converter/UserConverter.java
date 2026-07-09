package com.data.profile.web.converter;

import com.data.profile.common.enums.SourceType;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.dto.UserLoginDTO;
import com.data.profile.web.dto.UserParam;
import com.data.profile.web.dto.UserRequest;
import com.data.profile.web.model.User;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.vo.UserLoginVO;
import com.data.profile.web.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：用户转换器
 * 作者：SmartSi
 * 日期：2026/3/14
 */
public class UserConverter {

    private UserConverter() {
        // 静态工具类
    }

    // UserLoginDTO -> UserLoginVO（登录聚合转换：User→UserVO + roles + token）
    public static UserLoginVO convertLogin(UserLoginDTO dto) {
        if (dto == null) {
            return null;
        }
        UserVO userVO = do2vo(dto.getUser());
        if (userVO != null) {
            userVO.setRoles(dto.getRoles());
        }
        UserLoginVO vo = new UserLoginVO();
        vo.setUser(userVO);
        vo.setToken(dto.getToken());
        return vo;
    }

    // DTO -> VO
    public static UserVO dto2vo(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoConverterMapper.INSTANCE.convert(dto);
    }

    // DO -> VO
    public static UserVO do2vo(User user) {
        if (user == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(user);
    }

    // List<DTO> -> List<VO>
    public static List<UserVO> dto2voList(List<UserDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoConverterMapper.INSTANCE.convertList(dtos);
    }

    // List<DO> -> List<VO>
    public static List<UserVO> do2voList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(users);
    }

    // DTO -> DO
    public static User dto2do(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = DTO2DOConverterMapper.INSTANCE.convert(userDTO);
        user.setCreator(UserContextHolder.currentUserId());
        user.setModifier(UserContextHolder.currentUserId());
        user.setSourceType(SourceType.CUSTOM.getCode());
        return user;
    }

    // Request -> DO
    public static User request2do(UserRequest userRequest) {
        if (userRequest == null) {
            return null;
        }
        User user = Request2DOConverterMapper.INSTANCE.convert(userRequest);
        user.setCreator(UserContextHolder.currentUserId());
        user.setModifier(UserContextHolder.currentUserId());
        user.setSourceType(SourceType.CUSTOM.getCode());
        return user;
    }

    // Param -> DO
    public static User param2do(UserParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DTO -> VO
    @Mapper
    public interface DTO2VoConverterMapper extends BaseConverter<UserDTO, UserVO> {
        DTO2VoConverterMapper INSTANCE = Mappers.getMapper(DTO2VoConverterMapper.class);
        @Override
        UserVO convert(UserDTO dto);
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
    public interface DTO2DOConverterMapper extends BaseConverter<UserDTO, User> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        User convert(UserDTO dto);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOConverterMapper extends BaseConverter<UserRequest, User> {
        Request2DOConverterMapper INSTANCE = Mappers.getMapper(Request2DOConverterMapper.class);
        @Override
        User convert(UserRequest request);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<UserParam, User> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        User convert(UserParam param);
    }
}
