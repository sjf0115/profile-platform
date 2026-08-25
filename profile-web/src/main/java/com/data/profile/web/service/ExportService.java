package com.data.profile.web.service;

import com.data.profile.common.enums.TaskType;
import com.data.profile.web.converter.ExportConverter;
import com.data.profile.web.dao.ExportMapper;
import com.data.profile.web.dto.ExportDTO;
import com.data.profile.web.dto.ExportRequest;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.Task;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 投递服务
 */
@Slf4j
@Service
public class ExportService {
    @Resource
    private ExportMapper exportMapper;
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Resource
    private TaskInstanceService taskInstanceService;
    @Autowired
    private LineageService lineageService;
    @Autowired
    private TaskService taskService;

    /**
     * 根据查询条件获取投递列表
     */
    public List<ExportDTO> getList(Export export) {
        List<Export> exports = exportMapper.selectSimpleByParams(export);
        List<ExportDTO> dtos = ExportConverter.do2dtoList(exports);
        // 填充最新任务实例
        for (int i = 0; i < exports.size(); i++) {
            TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(exports.get(i).getExportId());
            dtos.get(i).setLatestInstance(latestInstance);
        }
        return dtos;
    }

    /**
     * 根据投递ID获取投递详细信息
     */
    public Optional<ExportDTO> getDetail(String exportId) {
        Export export = exportMapper.selectByExportId(exportId);
        if (export == null) {
            return Optional.empty();
        }
        ExportDTO dto = ExportConverter.do2dto(export);
        TaskInstance latestInstance = taskInstanceService.getLatestByRelatedId(exportId);
        dto.setLatestInstance(latestInstance);
        return Optional.of(dto);
    }

    /**
     * 创建投递
     */
    @Transactional
    public ExportDTO create(ExportRequest request) throws RuntimeException {
        String userId = UserContextHolder.currentUserId();

        // 检查投递名称是否重复
        List<Export> existing = exportMapper.selectSimpleByExportName(request.getExportName());
        if (!existing.isEmpty()) {
            throw new RuntimeException("投递任务已经存在，不允许重复投递");
        }

        String exportId = IDGenerator.getInstance().generate(ModelType.EXPORT);
        Export export = ExportConverter.request2do(request);
        export.setExportId(exportId);
        export.setStatus(Status.ENABLE.getCode());
        export.setSourceType(SourceType.CUSTOM.getCode());
        // 负责人：前端传入或默认当前用户
        if (StringUtils.isBlank(export.getOwner())) {
            export.setOwner(userId);
        }
        export.setCreator(userId);
        export.setModifier(userId);

        exportMapper.insertSelective(export);

        // 自动授权 MANAGE 给创建者
        resourceGrantService.grantOwner("10", exportId, userId);
        lineageService.refreshLineage(AssetType.EXPORT.getCode(), exportId);

        // 创建投递任务
        Task task = Task.builder()
                .taskName(export.getExportName())
                .taskType(TaskType.EXPORT.getCode())
                .taskDesc(export.getExportName() + "投递任务")
                .taskRelatedId(exportId)
                .build();
        taskService.createTask(task);
        log.info("为投递 [{}] 创建投递任务", exportId);

        ExportDTO dto = ExportConverter.do2dto(export);
        return dto;
    }

    /**
     * 更新投递
     */
    @Transactional
    public int update(String exportId, ExportRequest request) {
        Export existing = exportMapper.selectSimpleByExportId(exportId);
        if (existing == null) {
            throw new RuntimeException("投递不存在");
        }

        // 检查名称重复（排除自身）
        if (request.getExportName() != null && !request.getExportName().equals(existing.getExportName())) {
            List<Export> dup = exportMapper.selectSimpleByExportName(request.getExportName());
            if (!dup.isEmpty()) {
                throw new RuntimeException("投递名称已存在");
            }
        }

        Export export = ExportConverter.request2do(request);
        export.setExportId(exportId);
        export.setModifier(UserContextHolder.currentUserId());

        int result = exportMapper.updateByExportIdSelective(export);
        lineageService.refreshLineage(AssetType.EXPORT.getCode(), exportId);
        return result;
    }

    /**
     * 更新投递状态（启用/停用）
     */
    public int updateStatus(String exportId, Integer status) {
        Export export = new Export();
        export.setExportId(exportId);
        export.setStatus(status);
        export.setModifier(UserContextHolder.currentUserId());
        return exportMapper.updateByExportIdSelective(export);
    }

    /**
     * 删除投递
     */
    @Transactional
    public int delete(String exportId) {
        Export export = exportMapper.selectSimpleByExportId(exportId);
        if (export == null) {
            throw new RuntimeException("投递不存在");
        }
        if (Objects.equals(export.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置投递不允许删除");
        }
        lineageService.checkDeletable(AssetType.EXPORT.getCode(), exportId);
        lineageService.removeLineage(AssetType.EXPORT.getCode(), exportId);
        return exportMapper.deleteByExportId(exportId);
    }
}
