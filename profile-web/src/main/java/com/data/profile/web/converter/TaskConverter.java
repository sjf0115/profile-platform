package com.data.profile.web.converter;

import com.data.profile.web.dto.TaskDTO;
import com.data.profile.web.dto.TaskParam;
import com.data.profile.web.dto.TaskRequest;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.Task;
import com.data.profile.web.vo.DatasetFieldVO;
import com.data.profile.web.vo.TaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 任务转换器
 */
public class TaskConverter {

    private TaskConverter() {
    }

    // DO -> DTO
    public static TaskDTO do2dto(Task task) {
        if (task == null) return null;
        return DO2DTOMapper.INSTANCE.convert(task);
    }

    // List<DO> -> List<DTO>
    public static List<TaskDTO> do2dtoList(List<Task> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return DO2DTOMapper.INSTANCE.convertList(list);
    }

    // DTO -> VO
    public static TaskVO dto2vo(TaskDTO dto) {
        if (dto == null) return null;
        return DTO2VoMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<TaskVO> dto2voList(List<TaskDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
        return DTO2VoMapper.INSTANCE.convertList(dtos);
    }

    // DTO -> VO
    public static TaskVO do2vo(Task task) {
        if (task == null) return null;
        return DO2VoMapper.INSTANCE.convert(task);
    }

    // DTO -> DO
    public static Task dto2do(TaskDTO dto) {
        if (dto == null) return null;
        return DTO2DOMapper.INSTANCE.convert(dto);
    }

    // Request -> DO
    public static Task request2do(TaskRequest request) {
        if (request == null) return null;
        return Request2DOMapper.INSTANCE.convert(request);
    }

    // Param -> DO
    public static Task param2do(TaskParam param) {
        if (param == null) return null;
        return Param2DOMapper.INSTANCE.convert(param);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Mapper
    public interface DO2DTOMapper extends BaseConverter<Task, TaskDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        TaskDTO convert(Task task);
    }

    @Mapper
    public interface DTO2VoMapper extends BaseConverter<TaskDTO, TaskVO> {
        DTO2VoMapper INSTANCE = Mappers.getMapper(DTO2VoMapper.class);
        @Override
        TaskVO convert(TaskDTO dto);
    }

    @Mapper
    public interface DTO2DOMapper extends BaseConverter<TaskDTO, Task> {
        DTO2DOMapper INSTANCE = Mappers.getMapper(DTO2DOMapper.class);
        @Override
        Task convert(TaskDTO dto);
    }

    @Mapper
    public interface Request2DOMapper extends BaseConverter<TaskRequest, Task> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Task convert(TaskRequest request);
    }

    @Mapper
    public interface Param2DOMapper extends BaseConverter<TaskParam, Task> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Task convert(TaskParam param);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoMapper extends BaseConverter<Task, TaskVO> {
        DO2VoMapper INSTANCE = Mappers.getMapper(DO2VoMapper.class);
        @Override
        TaskVO convert(Task task);
    }
}
