package com.data.profile.web.service;

import com.data.engine.api.schema.Column;
import com.data.profile.common.enums.*;
import com.data.profile.web.converter.LabelConverter;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.dto.LabelDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.FileImportLabelConfig;
import com.data.profile.web.model.Label;
import com.data.profile.web.model.LabelConfig;
import com.data.profile.web.enums.AssetType;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.ENGINE_LABEL_TABLE_PREFIX;

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
    @Autowired
    private ResourceGrantService resourceGrantService;
    @Autowired
    private LabelMapper labelMapper;
    @Autowired
    private DatasetFieldService datasetFieldService;
    @Autowired
    private LineageService lineageService;
    @Autowired
    private UserService userService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private AnalysisEngineService analysisEngineService;

    /**
     * 根据查询条件获取标签列表（返回 DO，供内部 Service 使用）
     */
    public List<Label> getList(Label label) {
        List<Label> labels = labelMapper.selectByParams(label);
        log.info("根据查询条件获取 {} 个标签", labels.size());
        return labels;
    }

    /**
     * 根据查询条件获取标签列表（返回 DTO，供 Controller 使用）
     */
    public List<LabelDTO> getListDTO(Label label) {
        List<Label> labels = labelMapper.selectByParams(label);
        log.info("根据查询条件获取 {} 个标签", labels.size());
        List<LabelDTO> dtos = LabelConverter.do2dtoList(labels);
        Map<String, String> userMap = userService.getUserNameMap();
        for (LabelDTO dto : dtos) {
            dto.setOwnerName(userMap.get(dto.getOwner()));
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 根据标签ID获取标签详细信息（返回 DTO，供 Controller 使用）
     */
    public LabelDTO getDetail(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            return null;
        }
        LabelDTO dto = LabelConverter.do2dto(label);
        // 填充用户名
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setOwnerName(userMap.get(dto.getOwner()));
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        // entity info 已由 MyBatis JOIN 查出，无需二次查询
        // 填充数据集&字段
        DatasetField datasetField = datasetFieldService.getDetailByRelatedId(labelId);
        if (datasetField != null) {
            dto.setDatasetId(datasetField.getDatasetId());
            dto.setDatasetFieldName(datasetField.getFieldName());
        }
        return dto;
    }

    /**
     * 返回标签 DO（供内部 Service 使用）
     */
    public Label getDetailDO(String labelId) {
        return labelMapper.selectByLabelId(labelId);
    }

    /**
     * 创建标签
     * @param label 标签信息
     * @param datasetId 绑定的数据集ID（可为 null）
     * @param datasetFieldName 绑定的数据集字段名称（可为 null）
     */
    @Transactional
    public int create(Label label, String datasetId, String datasetFieldName) throws RuntimeException {
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
        label.setLabelStatus(LabelStatus.ENABLED.getCode()); // 默认启用状态
        label.setOwner(UserContextHolder.currentUserId());
        label.setCreator(UserContextHolder.currentUserId());
        label.setModifier(UserContextHolder.currentUserId());

        // 数据集导入方式
        if (Objects.equals(label.getSourceType(), LabelSourceType.DATASET.getCode())) {
            // 是否跳过数据集配置
            if (StringUtils.isEmpty(datasetFieldName)) {
                // 跳过数据集配置
                label.setLabelStatus(LabelStatus.UNBOUND.getCode());
            } else {
                // 数据集配置
                label.setLabelStatus(LabelStatus.ENABLED.getCode());
                // 只能选择已经创建好的数据集
                DatasetField field = datasetFieldService.getListByDatasetIdAndFieldName(datasetId, datasetFieldName);
                field.setRelatedId(labelId);
                field.setGmtModified(new Date());
                field.setModifier(UserContextHolder.currentUserId());
                datasetFieldService.save(field);
            }
        }
        // 文件上传方式：解析 CSV 并写入引擎表
        else if (Objects.equals(label.getSourceType(), LabelSourceType.FILE.getCode())) {
            LabelConfig labelConfig = label.getConfig();
            if (labelConfig == null || StringUtils.isEmpty(labelConfig.getPhysicalPath())) {
                throw new RuntimeException("文件上传标签配置解析失败，请联系管理员");
            }
            reimportFileTable(labelId, labelConfig.getPhysicalPath());
            label.setLabelStatus(LabelStatus.ENABLED.getCode());
        }

        int result;
        try {
            result = labelMapper.insertSelective(label);
        } catch (Exception e) {
            if (Objects.equals(label.getSourceType(), LabelSourceType.FILE.getCode())) {
                analysisEngineService.dropTable(ENGINE_LABEL_TABLE_PREFIX + labelId);
            }
            throw e;
        }
        // 自动授权 MANAGE 给创建者
        resourceGrantService.grantOwner("08", labelId, UserContextHolder.currentUserId());
        // 更新血缘
        lineageService.refreshLineage(AssetType.LABEL.getCode(), labelId);
        log.info("新增标签成功: {}", gson.toJson(label));
        return result;
    }

    /**
     * 更新标签
     * 结构：预处理（按创建方式分支） → DB 更新（含补偿） → 后处理
     * @param labelId 标签ID（从路径参数获取）
     * @param label 标签信息
     * @param datasetId 绑定的数据集ID（可为 null）
     * @param datasetFieldName 绑定的数据集字段名称（可为 null）
     */
    @Transactional
    public int update(String labelId, Label label, String datasetId, String datasetFieldName) throws RuntimeException {
        label.setLabelId(labelId);
        // 校验标签是否存在
        Label existingLabel = labelMapper.selectByLabelId(labelId);
        if (existingLabel == null) {
            log.error("标签 {} 不存在，无法更新", labelId);
            throw new RuntimeException("标签不存在，无法更新");
        }
        label.setModifier(UserContextHolder.currentUserId());

        // ------------------------------------------------------------------
        // 预处理：按存量标签的创建方式分支（创建方式不可变更）
        // ------------------------------------------------------------------
        // 数据集导入方式：绑定/解绑时管理标签状态
        if (Objects.equals(existingLabel.getSourceType(), LabelSourceType.DATASET.getCode())) {
            if (StringUtils.isNotEmpty(datasetId) && StringUtils.isNotEmpty(datasetFieldName)) {
                // 绑定数据集字段 → 已启用
                label.setLabelStatus(LabelStatus.ENABLED.getCode());
                DatasetField field = datasetFieldService.getListByDatasetIdAndFieldName(datasetId, datasetFieldName);
                field.setRelatedId(labelId);
                field.setGmtModified(new Date());
                field.setModifier(UserContextHolder.currentUserId());
                datasetFieldService.save(field);
            } else if (StringUtils.isNotEmpty(datasetId)) {
                // 跳过数据集配置 → 未绑定（解绑回退）
                label.setLabelStatus(LabelStatus.UNBOUND.getCode());
            }
        }
        // 文件上传方式：若替换了文件，先用新文件重建引擎表（失败直接抛异常，事务回滚）
        LabelConfig oldConfig = existingLabel.getConfig();
        String oldPhysicalPath = (oldConfig != null) ? oldConfig.getPhysicalPath() : null;
        boolean fileReplaced = false;
        if (Objects.equals(existingLabel.getSourceType(), LabelSourceType.FILE.getCode())) {
            LabelConfig newConfig = label.getConfig();
            String newPhysicalPath = (newConfig != null) ? newConfig.getPhysicalPath() : null;
            fileReplaced = StringUtils.isNotEmpty(newPhysicalPath) && !Objects.equals(newPhysicalPath, oldPhysicalPath);
            if (fileReplaced) {
                reimportFileTable(labelId, newPhysicalPath);
            }
        }

        // ------------------------------------------------------------------
        // DB 更新（失败补偿：若已重建引擎表，用旧文件恢复）
        // ------------------------------------------------------------------
        int result;
        try {
            result = labelMapper.updateByLabelIdSelective(label);
        } catch (Exception e) {
            if (fileReplaced && StringUtils.isNotEmpty(oldPhysicalPath)) {
                log.error("标签更新失败，补偿重建旧文件引擎表: labelId={}", labelId, e);
                try {
                    reimportFileTable(labelId, oldPhysicalPath);
                } catch (Exception ex) {
                    log.error("补偿重建引擎表失败，引擎表可能处于不一致状态: labelId={}", labelId, ex);
                }
            }
            throw e;
        }

        // ------------------------------------------------------------------
        // 后处理：清理旧文件、更新血缘
        // ------------------------------------------------------------------
        if (fileReplaced && StringUtils.isNotEmpty(oldPhysicalPath)) {
            try {
                minioService.deleteFile(oldPhysicalPath);
            } catch (Exception e) {
                log.warn("标签编辑删除旧 MinIO 文件失败: path={}, {}", oldPhysicalPath, e.getMessage());
            }
        }
        lineageService.refreshLineage(AssetType.LABEL.getCode(), labelId);
        log.info("修改标签成功: {}", gson.toJson(label));
        return result;
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
        if (Objects.equals(label.getSourceType(), LabelSourceType.BUILT_IN.getCode())) {
            log.error("内置标签不允许删除: {}", label.getLabelName());
            throw new RuntimeException("内置标签不允许删除");
        }

        // 删除保护：检查下游依赖
        lineageService.checkDeletable(AssetType.LABEL.getCode(), labelId);

        // 数据集导入方式：标签解除绑定数据集
        DatasetField boundField = datasetFieldService.getDetailByRelatedId(labelId);
        if (boundField != null && Objects.equals(label.getSourceType(), LabelSourceType.DATASET.getCode())) {
            datasetFieldService.deleteByDatasetIdAndFieldName(boundField.getDatasetId(), boundField.getFieldName());
        }

        // 文件上传方式：清理引擎表
        if (Objects.equals(label.getSourceType(), LabelSourceType.FILE.getCode())) {
            analysisEngineService.dropTable(ENGINE_LABEL_TABLE_PREFIX + labelId);
        }

        log.info("删除标签：{}({})", label.getLabelName(), labelId);
        lineageService.removeLineage(AssetType.LABEL.getCode(), labelId);
        return labelMapper.deleteByLabelId(labelId);
    }

    /**
     * 获取未被其他数据集绑定的标签
     * @param entityIdentifierId 实体标识ID
     * @param datasetId 数据集ID（编辑数据集必填，创建数据集为 null）
     * @return 可用标签 Model 列表
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

        // 4. 过滤掉被其他数据集绑定的标签
        List<Label> availableLabels = allLabels.stream()
                .filter(label -> !relatedLabelIds.contains(label.getLabelId()))
                .collect(Collectors.toList());

        log.info("获取实体 [{}] 下未被 [{}] 之外数据集绑定的可用标签：{} 个", entityIdentifierId, datasetId, availableLabels.size());
        return availableLabels;
    }

    /**
     * 获取实体标识下已绑定数据集的线上可用标签
     * @param entityIdentifierId 实体标识ID
     * @return 线上可用标签 Model 列表
     */
    public List<Label> getOnlineList(String entityIdentifierId) {
        // 1. 查询实体标识下的标签
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        List<Label> allLabels = labelMapper.selectByParams(query);

        // 2. 批量查询已绑定数据集的标签ID集合（避免 N+1）
        List<DatasetField> datasetFields = datasetFieldService.getList(new DatasetField());
        Set<String> boundLabelIds = datasetFields.stream()
                .filter(f -> f.getRelatedId() != null && !f.getRelatedId().isEmpty())
                .map(DatasetField::getRelatedId)
                .collect(Collectors.toSet());

        // 3. 过滤出已绑定数据集的标签
        List<Label> onlineLabels = allLabels.stream()
                .filter(label -> boundLabelIds.contains(label.getLabelId()))
                .collect(Collectors.toList());

        log.info("获取实体 [{}] 下已绑定数据集的线上可用标签：{} 个", entityIdentifierId, onlineLabels.size());
        return onlineLabels;
    }

    /**
     * 获取实体标识下全部已绑定数据集的标签（内部 Service 调用，返回 Model）
     * @param entityIdentifierId 实体标识ID
     */
    public List<Label> getBoundedLabels(String entityIdentifierId) {
        // 1. 查询实体标识下的标签
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        List<Label> allLabels = labelMapper.selectByParams(query);

        // 2. 批量查询已绑定数据集的标签ID集合（避免 N+1）
        List<DatasetField> datasetFields = datasetFieldService.getList(new DatasetField());
        Set<String> boundLabelIds = datasetFields.stream()
                .filter(f -> f.getRelatedId() != null && !f.getRelatedId().isEmpty())
                .map(DatasetField::getRelatedId)
                .collect(Collectors.toSet());

        // 3. 过滤出已绑定数据集的标签
        List<Label> labels = allLabels.stream()
                .filter(label -> boundLabelIds.contains(label.getLabelId()))
                .collect(Collectors.toList());

        log.info("获取实体 [{}] 下已绑定数据集的有效标签：{} 个", entityIdentifierId, labels.size());
        return labels;
    }

    /**
     * 上传文件到 MinIO
     * @param file 文件
     * @return 文件上传配置
     */
    public FileImportLabelConfig upload(MultipartFile file) {
        log.info("请求上传标签文件: {}", file.getOriginalFilename());
        // 1. 验证文件
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("仅支持 CSV 格式的文件");
        }
        if (file.getSize() > 200 * 1024 * 1024L) {
            throw new RuntimeException("文件大小不能超过 200M");
        }
        // 2. 上传到 MinIO
        String objectName = minioService.uploadFile(file, "upload_label");
        // 3. 构建返回结果
        FileImportLabelConfig config = new FileImportLabelConfig();
        config.setUuidFileKey(objectName);
        config.setFileName(filename);
        log.info("标签文件上传成功: {}", objectName);
        return config;
    }

    /**
     * 取消上传（删除 MinIO 文件）
     */
    public void cancelUpload(String fileKey) {
        try {
            minioService.deleteFile(fileKey);
        } catch (Exception e) {
            log.error("取消上传删除文件失败: {}", e.getMessage());
            throw new RuntimeException("取消上传删除文件失败");
        }
    }

    /**
     * 刷新文件上传标签：重新解析 CSV 并重建引擎表
     * @param labelId 标签ID
     */
    public void refreshFileUpload(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        if (!Objects.equals(label.getSourceType(), LabelSourceType.FILE.getCode())) {
            throw new RuntimeException("仅文件上传类型标签支持刷新操作");
        }
        LabelConfig labelConfig = label.getConfig();
        if (labelConfig == null || StringUtils.isEmpty(labelConfig.getPhysicalPath())) {
            throw new RuntimeException("标签文件配置无效，无法刷新");
        }
        reimportFileTable(labelId, labelConfig.getPhysicalPath());
        log.info("文件上传标签刷新成功: labelId={}", labelId);
    }

    /**
     * 重建文件上传标签的引擎表：删除旧表 → 解析指定 CSV 文件重新导入
     * 供创建、编辑（换文件）、刷新三个场景复用
     * @param labelId      标签ID
     * @param physicalPath MinIO 中的 CSV 文件路径
     */
    private void reimportFileTable(String labelId, String physicalPath) {
        String tableName = ENGINE_LABEL_TABLE_PREFIX + labelId;
        analysisEngineService.dropTable(tableName);
        List<Column> columns = Arrays.asList(
                Column.builder().name("entity_id").dataType(DataType.STRING_TYPE).comment("实体ID").build(),
                Column.builder().name("label_value").dataType(DataType.STRING_TYPE).comment("标签值").build()
        );
        try (InputStream is = minioService.getFileAsStream(physicalPath)) {
            analysisEngineService.importCsvToTable(tableName, is, columns,
                    Collections.singletonList("entity_id"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件上传标签引擎表重建失败: " + e.getMessage(), e);
        }
    }
}