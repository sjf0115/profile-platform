package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.LabelStatus;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.LabelMapper;
import com.data.profile.model.EntityIdentifier;
import com.data.profile.model.FileImportLabelConfig;
import com.data.profile.model.Label;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：标签服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class LabelService {
    private static final Gson gson = new GsonBuilder().create();

    @Resource
    private LabelMapper labelMapper;

    /**
     * 根据查询条件获取标签列表
     * @param label 标签信息
     */
    public List<Label> getList(Label label) {
        List<Label> labels = labelMapper.selectByParams(label);
        log.info("根据查询条件获取 {} 个标签: {}", labels.size(), gson.toJson(labels));
        return labels;
    }

    /**
     * 根据标签ID获取标签详细信息
     * @param labelId 标签ID
     */
    public Optional<Label> getDetail(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            return Optional.empty();
        }
        log.info("根据标签ID {} 获取标签详细信息: {}", labelId, gson.toJson(label));
        return Optional.of(label);
    }

    /**
     * 保存标签 新增/修改
     * @param label 标签信息
     */
    public int save(Label label) throws RuntimeException {
        if (StringUtils.isBlank(label.getLabelId())) {
            // 新增
            List<Label> labels = labelMapper.selectByLabelName(label.getLabelName());
            if (!labels.isEmpty()) {
                log.error("标签名称已经存在，不允许重复添加: {}", label.getLabelName());
                throw new RuntimeException("标签名称已经存在，不允许重复添加");
            }
            String labelId = IDGenerator.getInstance().generate(ModelType.LABEL);
            Label target = labelMapper.selectByLabelId(labelId);
            if (!Objects.equals(target, null)) {
                log.error("标签ID已经存在，不允许重复添加: {}", labelId);
                throw new RuntimeException("标签ID已经存在，不允许重复添加");
            }
            label.setLabelId(labelId);
            label.setIsValid(Status.ENABLE.getCode());
            label.setLabelStatus(LabelStatus.CREATED.getCode());
            label.setOwner(RequestContext.currentUserId());
            label.setCreator(RequestContext.currentUserId());
            label.setModifier(RequestContext.currentUserId());
            log.info("新增标签: {}", gson.toJson(label));
            return labelMapper.insertSelective(label);
        } else {
            // 修改
            label.setModifier(RequestContext.currentUserId());
            log.info("修改标签: {}", gson.toJson(label));
            return labelMapper.updateByLabelIdSelective(label);
        }
    }

    /**
     * 删除标签
     * @param labelId 标签ID
     */
    public int delete(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (Objects.equals(label, null)) {
            log.error("标签 {} 不存在，无法删除", labelId);
            throw new RuntimeException("标签不存在，无法删除");
        }
        if (Objects.equals(label.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置标签不允许删除: {}", label.getLabelName());
            throw new RuntimeException("内置标签不允许删除");
        }
        // TODO 检查依赖确保无下游使用
        log.info("删除标签：{}({})", label.getLabelName(), labelId);
        return labelMapper.deleteByLabelId(labelId);
    }

    /**
     * 文件上传创建标签
     */
    @Deprecated
    private void fileUpload(String labelId, String config) {
        FileImportLabelConfig labelConfig = gson.fromJson(config, FileImportLabelConfig.class);
        String filePath = labelConfig.getFilePath();
        String fileName = labelConfig.getFileName();
        if (StringUtils.isBlank(filePath) || StringUtils.isBlank(fileName)) {
            throw new RuntimeException("上传文件路径不能为空");
        }

    }
}