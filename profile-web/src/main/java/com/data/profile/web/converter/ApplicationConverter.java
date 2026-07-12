package com.data.profile.web.converter;

import com.data.profile.web.dto.ApplicationDTO;
import com.data.profile.web.dto.ApplicationParam;
import com.data.profile.web.dto.ApplicationRequest;
import com.data.profile.web.model.Application;
import com.data.profile.web.vo.ApplicationVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 应用转换器
 */
public class ApplicationConverter {

    private ApplicationConverter() {
        // 静态工具类
    }

    // DO -> DTO
    public static ApplicationDTO do2dto(Application application) {
        if (application == null) {
            return null;
        }
        return DO2DTOConverterMapper.INSTANCE.convert(application);
    }

    // List<DO> -> List<DTO>
    public static List<ApplicationDTO> do2dtoList(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2DTOConverterMapper.INSTANCE.convertList(applications);
    }

    // DTO -> VO
    public static ApplicationVO dto2vo(ApplicationDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoConverterMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<ApplicationVO> dto2voList(List<ApplicationDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoConverterMapper.INSTANCE.convertList(dtos);
    }

    // DO -> VO
    public static ApplicationVO do2vo(Application application) {
        if (application == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(application);
    }

    // List<DO> -> List<VO>
    public static List<ApplicationVO> do2voList(List<Application> applications) {
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(applications);
    }

    // DTO -> DO
    public static Application dto2do(ApplicationDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2DOConverterMapper.INSTANCE.convert(dto);
    }

    // Request -> DO
    public static Application request2do(ApplicationRequest request) {
        if (request == null) {
            return null;
        }
        return Request2DOConverterMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static Application param2do(ApplicationParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    //------------------------------------------------------------------------------------------------------------------

    // DO -> DTO
    @Mapper
    public interface DO2DTOConverterMapper extends BaseConverter<Application, ApplicationDTO> {
        DO2DTOConverterMapper INSTANCE = Mappers.getMapper(DO2DTOConverterMapper.class);
        @Override
        ApplicationDTO convert(Application application);
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VoConverterMapper extends BaseConverter<ApplicationDTO, ApplicationVO> {
        DTO2VoConverterMapper INSTANCE = Mappers.getMapper(DTO2VoConverterMapper.class);
        @Override
        ApplicationVO convert(ApplicationDTO dto);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Application, ApplicationVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        ApplicationVO convert(Application application);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOConverterMapper extends BaseConverter<ApplicationRequest, Application> {
        Request2DOConverterMapper INSTANCE = Mappers.getMapper(Request2DOConverterMapper.class);
        @Override
        Application convert(ApplicationRequest request);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<ApplicationParam, Application> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Application convert(ApplicationParam param);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOConverterMapper extends BaseConverter<ApplicationDTO, Application> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        Application convert(ApplicationDTO dto);
    }
}
