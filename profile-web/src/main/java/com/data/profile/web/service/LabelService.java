package com.data.profile.web.service;

import com.data.engine.api.schema.Column;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.CommonUtil;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.LabelConverter;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.dto.DatasetDTO;
import com.data.profile.web.dto.EntityIdentifierDTO;
import com.data.profile.web.dto.LabelDTO;
import com.data.profile.web.dto.LabelValueDistributionDTO;
import com.data.profile.web.engine.AnalysisEngineService;
import com.data.profile.web.engine.SqlTemplateEngine;
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

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.data.profile.common.domain.Constant.*;

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
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private EntityIdentifierService entityIdentifierService;
    @Autowired
    private SqlTemplateEngine sqlTemplateEngine;

    /**
     * 根据查询条件获取标签列表
     */
    public List<Label> getLabels(Label label) {
        List<Label> labels = labelMapper.selectByParams(label);
        log.info("根据查询条件获取标签列表：{}", JSONUtils.toJsonString(labels));
        return labels;
    }

    /**
     * 根据查询条件获取标签列表
     */
    public List<LabelDTO> getList(Label label) {
        List<Label> labels = getLabels(label);
        List<LabelDTO> dtos = LabelConverter.do2dtoList(labels);
        // 补充用户信息
        Map<String, String> userMap = userService.getUserNameMap();
        for (LabelDTO dto : dtos) {
            dto.setOwnerName(userMap.get(dto.getOwner()));
            dto.setCreatorName(userMap.get(dto.getCreator()));
            dto.setModifierName(userMap.get(dto.getModifier()));
        }
        return dtos;
    }

    /**
     * 返回标签
     */
    public Label getLabel(String labelId) {
        return labelMapper.selectByLabelId(labelId);
    }

    /**
     * 根据标签ID获取标签详细信息（返回 DTO，供 Controller 使用）
     */
    public LabelDTO getDetail(String labelId) {
        Label label = getLabel(labelId);
        if (label == null) {
            return null;
        }
        LabelDTO dto = LabelConverter.do2dto(label);
        // 填充用户名
        Map<String, String> userMap = userService.getUserNameMap();
        dto.setOwnerName(userMap.get(dto.getOwner()));
        dto.setCreatorName(userMap.get(dto.getCreator()));
        dto.setModifierName(userMap.get(dto.getModifier()));
        // 填充数据集&字段
        DatasetField datasetField = datasetFieldService.getDetailByRelatedId(labelId);
        if (datasetField != null) {
            dto.setDatasetId(datasetField.getDatasetId());
            dto.setDatasetFieldName(datasetField.getFieldName());
            DatasetDTO datasetDTO = datasetService.getDetail(datasetField.getDatasetId());
            if (datasetDTO != null) {
                dto.setDatasetName(datasetDTO.getDatasetName());
            }
        }
        // 填充实体标识信息
        if (StringUtils.isNotBlank(label.getEntityIdentifierId())) {
            EntityIdentifierDTO identifier = entityIdentifierService.getDetail(label.getEntityIdentifierId());
            if (identifier != null) {
                dto.setEntityIdentifierName(identifier.getEntityIdentifierName());
                dto.setEntityId(identifier.getEntityId());
                dto.setEntityName(identifier.getEntityName());
            }
        }
        return dto;
    }

    /**
     * 获取标签取值分布与覆盖量（标签详情页维度，全量数据 Top10）。
     *
     * <p>标签不存在/未绑定/引擎表未就绪时返回 hasData=false 的空结果，不抛异常。</p>
     */
    // TODO
    public LabelValueDistributionDTO getLabelValueDistribution(String labelId) throws Exception {
        if (StringUtils.isEmpty(labelId)) {
            log.error("标签 {} 不存在", labelId);
            throw new RuntimeException("标签不存在,请联系管理员");
        }

        Label label = getLabel(labelId);
        Integer labelStatus = label.getLabelStatus();
        Integer sourceType = label.getSourceType();

        LabelValueDistributionDTO result = new LabelValueDistributionDTO();
        result.setLabelId(labelId);
        result.setHasData(false);
        result.setValues(Collections.emptyList());
        if (!Objects.equals(labelStatus, LabelStatus.ENABLED.getCode())) {
            log.info("标签 {} 未启用，返回空分布", labelId);
            return result;
        }

        // 标签存储引擎表 - 表名与标签列名
        String tableName;
        String fieldName;
        if (Objects.equals(sourceType, LabelSourceType.DATASET.getCode())) {
            // 数据集导入方式
            DatasetField field = datasetFieldService.getDetailByRelatedId(labelId);
            if (field == null) {
                log.warn("标签 {} 未关联数据集字段，返回空分布", labelId);
                return result;
            }
            fieldName = field.getFieldName();
            tableName = ENGINE_DATASET_TABLE_PREFIX + field.getDatasetId();
            if (!analysisEngineService.tableExists(tableName)) {
                log.warn("标签 {} 对应引擎表 {} 不存在，返回空分布", labelId, tableName);
                return result;
            }
        } else if (Objects.equals(sourceType, LabelSourceType.FILE.getCode())) {
            // 文件上传方式
            tableName = ENGINE_LABEL_TABLE_PREFIX + labelId;
            fieldName = ENGINE_LABEL_TABLE_VALUE_COLUMN;
        } else {
            log.warn("标签 {} 创建方式未实现，返回空分布", labelId);
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("fieldName", fieldName);
        params.put("limit", 10);

        // 覆盖量统计
        String coverageSql = sqlTemplateEngine.render("label_coverage.ftl", params);
        Map<String, Object> coverageRows = analysisEngineService.executeQuery(coverageSql);
        long totalCount = 0L;
        long coverCount = 0L;
        if (!coverageRows.isEmpty()) {
            totalCount = CommonUtil.toLong(coverageRows.get("total_count"));
            coverCount = CommonUtil.toLong(coverageRows.get("cover_count"));
        }
        result.setTotalCount(totalCount);
        result.setCoverCount(coverCount);
        // TODO 覆盖率计算公式优化
        result.setCoverRate(totalCount > 0 ? CommonUtil.round1(coverCount * 100.0 / totalCount) : 0.0);

        // 取值分布 Top10
        String distributionSql = sqlTemplateEngine.render("label_value_distribution.ftl", params);
        List<Map<String, Object>> rows = analysisEngineService.executeQueryList(distributionSql);
        long distributionTotal = rows.stream().mapToLong(row -> CommonUtil.toLong(row.get("cnt"))).sum();
        List<LabelValueDistributionDTO.Item> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            LabelValueDistributionDTO.Item item = new LabelValueDistributionDTO.Item();
            Object value = row.get("label_value");
            item.setValue(value == null ? "-" : String.valueOf(value));
            long count = CommonUtil.toLong(row.get("cnt"));
            item.setCount(count);
            item.setPercent(distributionTotal > 0 ? CommonUtil.round1(count * 100.0 / distributionTotal) : 0.0);
            items.add(item);
        }
        result.setValues(items);
        if (!items.isEmpty()) {
            result.setSampleValue(items.get(0).getValue());
        }
        result.setHasData(true);
        return result;
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
            // 文件上传方式需要创建标签引擎表
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
            label.setLabelStatus(LabelStatus.ENABLED.getCode());
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
        // 1. 删除基本检查
        Label label = labelMapper.selectByLabelId(labelId);
        if (Objects.equals(label, null)) {
            log.error("标签 {} 不存在，无法删除", labelId);
            throw new RuntimeException("标签不存在，无法删除");
        }
        if (Objects.equals(label.getSourceType(), LabelSourceType.BUILT_IN.getCode())) {
            log.error("内置标签不允许删除: {}", label.getLabelName());
            throw new RuntimeException("内置标签不允许删除");
        }

        // 2. 删除下游依赖检查
        lineageService.checkDeletable(AssetType.LABEL.getCode(), labelId);

        // 3. 删除后续逻辑
        Integer sourceType = label.getSourceType();
        if (Objects.equals(sourceType, LabelSourceType.DATASET.getCode())) {
            // 数据集导入方式：标签解除绑定数据集
            DatasetField boundField = datasetFieldService.getDetailByRelatedId(labelId);
            if (boundField != null) {
                datasetFieldService.deleteByDatasetIdAndFieldName(boundField.getDatasetId(), boundField.getFieldName());
            }
        } else if (Objects.equals(label.getSourceType(), LabelSourceType.FILE.getCode())) {
            // 文件上传方式：清理引擎表
            analysisEngineService.dropTable(ENGINE_LABEL_TABLE_PREFIX + labelId);
        }

        // 4. 删除标签
        log.info("删除标签：{}({})", label.getLabelName(), labelId);

        // 5. 更新血缘
        lineageService.removeLineage(AssetType.LABEL.getCode(), labelId);
        return labelMapper.deleteByLabelId(labelId);
    }

    /**
     * 获取可绑定到数据集的标签（数据集创建/编辑场景）
     * <ul>
     *   <li>仅数据集导入(2)类型标签参与绑定：文件上传(3)等其他创建方式自带数据来源，不可绑定</li>
     *   <li>以 label_status 判定绑定状态（UNBOUND=未绑定），不再全表扫描 dataset_field</li>
     *   <li>datasetId 非空表示编辑数据集：被本数据集绑定的标签照常返回，仅排除被其他数据集绑定的</li>
     * </ul>
     * @param entityIdentifierId 实体标识ID
     * @param datasetId 数据集ID（编辑数据集必填，创建数据集为 null）
     * @return 可绑定标签 Model 列表
     */
    public List<Label> getUnboundLabels(String entityIdentifierId, String datasetId) {
        // 1. 查询该实体标识下的全部数据集导入标签
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        query.setSourceType(LabelSourceType.DATASET.getCode());
        List<Label> labels = labelMapper.selectByParams(query);

        if (StringUtils.isEmpty(datasetId)) {
            // 2. 创建数据集场景：未绑定即可用，直接按状态过滤
            List<Label> availableLabels = labels.stream()
                    .filter(label -> Objects.equals(label.getLabelStatus(), LabelStatus.UNBOUND.getCode()))
                    .collect(Collectors.toList());
            log.info("获取实体 [{}] 下未绑定标签：{} 个", entityIdentifierId, availableLabels.size());
            return availableLabels;
        } else {
            // 3. 编辑数据集场景：仅查询本数据集的字段（而非全表），得到本数据集已绑定的标签ID集合
            Set<String> selfBoundLabelIds = datasetFieldService.getListByDatasetId(datasetId).stream()
                    .filter(f -> f.getRelatedId() != null && !f.getRelatedId().isEmpty())
                    .map(DatasetField::getRelatedId)
                    .collect(Collectors.toSet());

            // 4. 未绑定 ∪ 被本数据集绑定（即排除被其他数据集绑定的）
            List<Label> availableLabels = labels.stream()
                    .filter(label -> Objects.equals(label.getLabelStatus(), LabelStatus.UNBOUND.getCode())
                            || selfBoundLabelIds.contains(label.getLabelId()))
                    .collect(Collectors.toList());

            log.info("获取实体 [{}] 下可绑定标签（数据集={}）：{} 个", entityIdentifierId, datasetId, availableLabels.size());
            return availableLabels;
        }
    }

    /**
     * 查询实体标识下已启用的标签
     * @param entityIdentifierId 实体标识ID
     * @return 已启用标签
     */
    public List<Label> getOnlineLabels(String entityIdentifierId) {
        Label query = new Label();
        query.setEntityIdentifierId(entityIdentifierId);
        query.setLabelStatus(LabelStatus.ENABLED.getCode());
        List<Label> labels = labelMapper.selectByParams(query);
        log.info("获取实体 [{}] 下已启用标签：{} 个", entityIdentifierId, labels.size());
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

    //------------------------------------------------------------------------------------------------------------------

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
                Column.builder().name(ENGINE_LABEL_TABLE_ENTITY_COLUMN).dataType(DataType.STRING_TYPE).comment("实体ID").build(),
                Column.builder().name(ENGINE_LABEL_TABLE_VALUE_COLUMN).dataType(DataType.STRING_TYPE).comment("标签值").build()
        );
        try (InputStream is = minioService.getFileAsStream(physicalPath)) {
            analysisEngineService.importCsvToTable(tableName, is, columns,
                    Collections.singletonList(ENGINE_LABEL_TABLE_ENTITY_COLUMN));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件上传标签引擎表重建失败: " + e.getMessage(), e);
        }
    }
}