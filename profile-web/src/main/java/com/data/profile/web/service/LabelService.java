package com.data.profile.web.service;

import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.FileImportLabelConfig;
import com.data.profile.web.model.Label;
import com.data.profile.web.security.RequestContext;
import com.data.profile.common.enums.LabelStatus;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private DatasetFieldService datasetFieldService;

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
        // 查询标签详细信息
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            return Optional.empty();
        }

        // 查询绑定的数据集和字段
        DatasetField datasetField = datasetFieldService.getDetailByRelatedId(labelId);
        if (!Objects.equals(datasetField, null)) {
            label.setDatasetId(datasetField.getDatasetId());
            label.setDatasetFieldName(datasetField.getFieldName());
        }

        log.info("根据标签ID {} 获取标签详细信息: {}", labelId, gson.toJson(label));
        return Optional.of(label);
    }

    /**
     * 保存标签 新增/修改
     * @param label 标签信息
     */
    @Transactional
    public int save(Label label) throws RuntimeException {
        // isValid 废弃
        label.setIsValid(1);
        // 保存/修改标签
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

            // 是否跳过数据集配置
            String fieldName = label.getDatasetFieldName();
            if (StringUtils.isNotEmpty(fieldName) && Objects.equals(label.getSourceType(), 2)) {
                // 只能选择已经创建好的数据集
                String datasetId = label.getDatasetId();
                DatasetField field = datasetFieldService.getListByDatasetIdAndFieldName(datasetId, fieldName);
                field.setRelatedId(labelId);
                field.setGmtModified(new Date());
                field.setModifier(RequestContext.currentUserId());
                datasetFieldService.save(field);
            }
            return labelMapper.insertSelective(label);
        } else {
            // 是否跳过数据集配置
            String fieldName = label.getDatasetFieldName();
            // TODO 是否能取消配置数据集
            if (StringUtils.isNotEmpty(fieldName) && Objects.equals(label.getSourceType(), 2)) {
                String datasetId = label.getDatasetId();
                String labelId = label.getLabelId();
                DatasetField field = datasetFieldService.getListByDatasetIdAndFieldName(datasetId, fieldName);
                field.setRelatedId(labelId);
                field.setGmtModified(new Date());
                field.setModifier(RequestContext.currentUserId());
                datasetFieldService.save(field);
            }

            // 修改
            label.setModifier(RequestContext.currentUserId());
            log.info("修改标签: {}", gson.toJson(label));
            return labelMapper.updateByLabelId(label);
        }
    }

    /**
     * 删除标签
     * @param labelId 标签ID
     */
    @Transactional
    public int delete(String labelId) {
        // 删除标签
        Label label = labelMapper.selectByLabelId(labelId);
        if (Objects.equals(label, null)) {
            log.error("标签 {} 不存在，无法删除", labelId);
            throw new RuntimeException("标签不存在，无法删除");
        }
        if (Objects.equals(label.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("内置标签不允许删除: {}", label.getLabelName());
            throw new RuntimeException("内置标签不允许删除");
        }

        // 通过数据集绑定字段方式：标签解除绑定数据集
        String fieldName = label.getDatasetFieldName();
        if (StringUtils.isNotEmpty(fieldName) && Objects.equals(label.getSourceType(), 2)) {
            // 已绑定数据集字段需要解除绑定
            String datasetId = label.getDatasetId();
            // 只有选择数据集字段才可以绑定
            datasetFieldService.deleteByDatasetIdAndFieldName(datasetId, fieldName);
        }

        // TODO 检查依赖确保无下游使用
        log.info("删除标签：{}({})", label.getLabelName(), labelId);
        return labelMapper.deleteByLabelId(labelId);
    }


    /**
     * 获取可用标签列表，未被其他数据集绑定的标签
     * @param entityIdentifierId 实体标识ID
     * @param datasetId 数据集ID（编辑数据集必填，创建数据集为 null）
     */
    public List<Label> getAvailableList(String entityIdentifierId, String datasetId) {
        // 如果有 datasetId 表示是编辑数据集获取可用标签，则获取未被其他数据集绑定的标签，本数据集绑定的标签可以返回
        // 如果没有 datasetId 表示创建数据集获取可用标签，则获取所有未被绑定的标签

        // 1. 查询该实体标识下的所有标签
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        List<Label> allLabels = labelMapper.selectByParams(query);

        // 2. 查询所有已绑定标签的数据集字段
        List<DatasetField> datasetFields = datasetFieldService.getList(new DatasetField());

        // 3. 被其他数据集绑定的标签ID集合
        Set<String> relatedLabelIds = datasetFields.stream()
                .filter(f -> f.getRelatedId() != null && !f.getRelatedId().isEmpty())
                .filter(f -> datasetId == null || !datasetId.equals(f.getDatasetId()))
                .map(DatasetField::getRelatedId)
                .collect(Collectors.toSet());

        // 4. 过滤掉被其他数据集绑定的标签的标签
        List<Label> availableLabels = allLabels.stream()
                .filter(label -> !relatedLabelIds.contains(label.getLabelId()))
                .collect(Collectors.toList());

        log.info("获取实体 [{}] 下未被 [{}] 之外数据集绑定的可用标签：{}", entityIdentifierId, datasetId, gson.toJson(availableLabels));
        return availableLabels;
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