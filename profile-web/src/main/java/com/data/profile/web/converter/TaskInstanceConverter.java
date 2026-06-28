package com.data.profile.web.converter;

import com.data.profile.web.dto.TaskInstanceDTO;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.security.RequestContext;
import com.data.profile.web.vo.TaskInstanceVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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
    public static TaskInstanceVO convert(TaskInstance taskInstance) {
        TaskInstanceVO taskInstanceVO = new TaskInstanceVO();
        if (taskInstance != null) {
            taskInstanceVO = DO2VoConverterMapper.INSTANCE.convert(taskInstance);
        }
        return taskInstanceVO;
    }

    // DTO -> DO
    public static TaskInstance convert(TaskInstanceDTO taskInstanceDTO) {
        TaskInstance taskInstance = new TaskInstance();
        if (taskInstanceDTO != null) {
            taskInstance = DTO2DOConverterMapper.INSTANCE.convert(taskInstanceDTO);
            taskInstance.setCreator(RequestContext.currentUserId());
            taskInstance.setModifier(RequestContext.currentUserId());
        }
        return taskInstance;
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
}
