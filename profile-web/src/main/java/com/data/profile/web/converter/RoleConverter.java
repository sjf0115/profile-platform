package com.data.profile.web.converter;

import com.data.profile.web.dto.RoleParam;
import com.data.profile.web.dto.RoleRequest;
import com.data.profile.web.model.Role;
import com.data.profile.web.vo.RoleVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：角色转换器
 */
public class RoleConverter {

    private RoleConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static RoleVO do2vo(Role role) {
        if (role == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(role);
    }

    // List<DO> -> List<VO>
    public static List<RoleVO> do2voList(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(roles);
    }

    // Param -> DO
    public static Role param2do(RoleParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // Request -> DO
    public static Role request2do(RoleRequest request) {
        if (request == null) {
            return null;
        }
        return Request2DOMapper.INSTANCE.convert(request);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Role, RoleVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        RoleVO convert(Role role);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<RoleParam, Role> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Role convert(RoleParam param);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOMapper extends BaseConverter<RoleRequest, Role> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Role convert(RoleRequest request);
    }
}
