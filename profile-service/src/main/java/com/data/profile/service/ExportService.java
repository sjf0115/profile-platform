package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.ExportMapper;
import com.data.profile.model.Export;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：投递服务
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 11:47
 */
@Slf4j
@Service
public class ExportService {
    @Resource
    private ExportMapper exportMapper;

    /**
     * 根据查询条件获取投递列表
     * @param export
     * @return
     */
    public List<Export> getList(Export export) {
        return exportMapper.selectByParams(export);
    }

    /**
     * 根据名字查询
     * @param exportName
     * @return
     */
    public List<Export> getByName(String exportName) {
        return exportMapper.selectByExportName(exportName);
    }

    /**
     * 模糊查询
     * @param keyword
     * @return
     */
    public List<Export> getByKeyword(String keyword) {
        return exportMapper.selectByKeyword(keyword);
    }

    /**
     * 根据投递ID获取投递详细信息
     * @param exportId
     * @return
     */
    public Optional<Export> getDetail(String exportId) {
        Export export = exportMapper.selectByExportId(exportId);
        if (export == null) {
            return Optional.empty();
        }
        return Optional.of(export);
    }

    /**
     * 保存投递 新增/修改
     * @param export
     * @return
     * @throws RuntimeException
     */
    public int save(Export export) throws RuntimeException {
        if (StringUtils.isBlank(export.getExportId())) {
            // 新增
            List<Export> exports = exportMapper.selectSimpleByExportName(export.getExportName());
            if (!exports.isEmpty()) {
                throw new RuntimeException("投递任务已经存在，不允许重复投递");
            }
            String exportId = IDGenerator.getInstance().generate(ModelType.EXPORT);
            Export target = exportMapper.selectSimpleByExportId(exportId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("投递ID已经存在，不允许重复添加");
            }
            export.setExportId(exportId);
            export.setStatus(Status.ENABLE.getCode());
            export.setSourceType(SourceType.CUSTOM.getCode());
            String userId = RequestContext.currentUserId();
            export.setOwner(userId); // 创建者即为负责人
            export.setCreator(userId);
            export.setModifier(userId);
            return exportMapper.insertSelective(export);
        } else {
            // 修改
            export.setModifier(RequestContext.currentUserId());
            return exportMapper.updateByExportIdSelective(export);
        }
    }

    /**
     * 删除投递
     * @param exportId
     * @return
     */
    public int delete(String exportId) {
        Export export = exportMapper.selectSimpleByExportId(exportId);
        if (Objects.equals(export.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("内置投递不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        return exportMapper.deleteByExportId(exportId);
    }
}
