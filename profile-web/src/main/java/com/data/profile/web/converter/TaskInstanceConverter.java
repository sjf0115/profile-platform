package com.data.profile.web.converter;

import com.data.profile.web.dto.TaskDTO;
import com.data.profile.web.dto.TaskInstanceDTO;
import com.data.profile.web.dto.TaskInstanceParam;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.vo.TaskInstanceVO;
import com.data.profile.web.vo.TaskVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：任务实例转换器
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/6/28 22:37
 */
public class TaskInstanceConverter {

    private TaskInstanceConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static TaskInstanceVO do2vo(TaskInstance taskInstance) {
        if (taskInstance == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(taskInstance);
    }

    // List<DO> -> List<VO>
    public static List<TaskInstanceVO> do2voList(List<TaskInstance> taskInstances) {
        if (taskInstances == null || taskInstances.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(taskInstances);
    }

    // DO -> DTO
    public static TaskInstanceDTO do2dto(TaskInstance taskInstance) {
        if (taskInstance == null) {
            return null;
        }
        return DO2DTOMapper.INSTANCE.convert(taskInstance);
    }

    // List<DO> -> List<DTO>
    public static List<TaskInstanceDTO> do2dtoList(List<TaskInstance> taskInstances) {
        if (taskInstances == null || taskInstances.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2DTOMapper.INSTANCE.convertList(taskInstances);
    }

    // DTO -> VO
    public static TaskInstanceVO dto2vo(TaskInstanceDTO dto) {
        if (dto == null) {
            return null;
        }
        return DTO2VoConverterMapper.INSTANCE.convert(dto);
    }

    // List<DTO> -> List<VO>
    public static List<TaskInstanceVO> dto2voList(List<TaskInstanceDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return DTO2VoConverterMapper.INSTANCE.convertList(dtos);
    }

    // DTO -> DO
    public static TaskInstance dto2do(TaskInstanceDTO taskInstanceDTO) {
        TaskInstance taskInstance = new TaskInstance();
        if (taskInstanceDTO != null) {
            taskInstance = DTO2DOConverterMapper.INSTANCE.convert(taskInstanceDTO);
            taskInstance.setCreator(UserContextHolder.currentUserId());
            taskInstance.setModifier(UserContextHolder.currentUserId());
        }
        return taskInstance;
    }

    // Param -> DO
    public static TaskInstance param2do(TaskInstanceParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<TaskInstance, TaskInstanceVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        TaskInstanceVO convert(TaskInstance taskInstance);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOConverterMapper extends BaseConverter<TaskInstanceDTO, TaskInstance> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        TaskInstance convert(TaskInstanceDTO taskInstanceDTO);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<TaskInstanceParam, TaskInstance> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        TaskInstance convert(TaskInstanceParam param);
    }

    // DO -> DTO
    @Mapper
    public interface DO2DTOMapper extends BaseConverter<TaskInstance, TaskInstanceDTO> {
        DO2DTOMapper INSTANCE = Mappers.getMapper(DO2DTOMapper.class);
        @Override
        @Mapping(target = "task", ignore = true)
        TaskInstanceDTO convert(TaskInstance taskInstance);
    }

    // DTO -> VO
    @Mapper
    public interface DTO2VoConverterMapper extends BaseConverter<TaskInstanceDTO, TaskInstanceVO> {
        DTO2VoConverterMapper INSTANCE = Mappers.getMapper(DTO2VoConverterMapper.class);
        @Override
        TaskInstanceVO convert(TaskInstanceDTO dto);

        // TaskDTO -> TaskVO 转换（委托给 TaskConverter）
        default TaskVO taskDto2vo(TaskDTO dto) {
            return TaskConverter.dto2vo(dto);
        }
    }
}
