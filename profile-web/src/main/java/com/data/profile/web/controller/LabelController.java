package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.LabelConverter;
import com.data.profile.web.dto.LabelParam;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.model.DatasetField;
import com.data.profile.web.model.EntityIdentifier;
import com.data.profile.web.model.Label;
import com.data.profile.web.service.DatasetFieldService;
import com.data.profile.web.service.EntityIdentifierService;
import com.data.profile.web.vo.LabelVO;
import com.data.profile.web.service.LabelService;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 功能：标签
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/label", produces = MediaType.APPLICATION_JSON_VALUE)
public class LabelController {
    @Autowired
    private LabelService labelService;
    @Autowired
    private EntityIdentifierService entityIdentifierService;
    @Autowired
    private DatasetFieldService datasetFieldService;

    @PostMapping(value = "/list")
    public Response<List<LabelVO>> getList(@RequestBody LabelParam param) {
        log.info("请求根据标签信息查询标签: {}", JSONUtils.toJsonString(param));
        Label label = LabelConverter.param2do(param);
        List<Label> labels = labelService.getList(label);
        List<LabelVO> voList = LabelConverter.do2voList(labels);
        populateEntityInfo(voList);
        return Response.success(voList);
    }

    @GetMapping(value = "/detail")
    public Response<LabelVO> getDetail(@RequestParam(name = "label_id") String labelId) {
        log.info("根据标签ID [{}] 请求查询标签信息", labelId);
        Optional<Label> labelOpt = labelService.getDetail(labelId);
        if (!labelOpt.isPresent()) {
            return Response.error("请求的标签不存在", ResponseCode.ERROR);
        }
        LabelVO vo = LabelConverter.do2vo(labelOpt.get());
        populateEntityInfo(vo);
        populateDatasetInfo(vo, labelId);
        return Response.success(vo);
    }

    @RequiresPermission(code = "label:edit", name = "标签-编辑")
    @PostMapping(value = "/save")
    public Response<Integer> save(@RequestBody LabelRequest request) {
        log.info("请求保存/更新标签信息: {}", JSONUtils.toJsonString(request));
        Label label = LabelConverter.request2do(request);
        int result = labelService.save(label, request.getDatasetId(), request.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加标签失败", ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "label:edit", name = "标签-编辑")
    @PostMapping(value = "/update")
    public Response<Integer> update(@RequestBody LabelRequest request) {
        log.info("请求更新标签信息: {}", JSONUtils.toJsonString(request));
        Label label = LabelConverter.request2do(request);
        int result = labelService.save(label, request.getDatasetId(), request.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("更新标签失败", ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "label:delete", name = "标签-删除")
    @DeleteMapping(value = "/delete")
    public Response<Integer> delete(@RequestParam(name = "label_id") String labelId) {
        log.info("请求删除标签: {}", labelId);
        int result = labelService.delete(labelId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除标签失败", ResponseCode.ERROR);
        }
    }

    /**
     * 获取未绑定数据集的标签（数据集创建/编辑场景专用）
     */
    @GetMapping(value = "/unbound")
    public Response<List<LabelVO>> getUnboundLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId,
            @RequestParam(name = "dataset_id", required = false) String datasetId) {
        log.info("请求获取实体 [{}] 下未被 [{}] 之外数据集绑定的标签", entityIdentifierId, datasetId);
        List<Label> labels = labelService.getAvailableList(entityIdentifierId, datasetId);
        return Response.success(LabelConverter.do2voList(labels));
    }

    /**
     * 获取已绑定数据集的线上可用标签（群组规则/分析场景专用）
     */
    @GetMapping(value = "/online")
    public Response<List<LabelVO>> getOnlineLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("请求获取实体 [{}] 下已绑定数据集的线上可用标签", entityIdentifierId);
        List<Label> labels = labelService.getOnlineList(entityIdentifierId);
        return Response.success(LabelConverter.do2voList(labels));
    }

    @GetMapping(value = "/config")
    public Response<Map<String, Object>> getConfig() {
        log.info("请求获取标签配置信息");
        Map<String, Object> config = new HashMap<>();
        config.put("label_type", Stream.of(LabelType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("data_type", Stream.of(LabelDataType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("dist_type", Stream.of(LabelDistType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("organize_type", Stream.of(LabelOrganizeType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("produce_type", Stream.of(LabelProduceType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("time_type", Stream.of(LabelTimeType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        config.put("source_type", Stream.of(LabelSourceType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        return Response.success(config);
    }

    /**
     * 批量填充实体关联信息
     */
    private void populateEntityInfo(List<LabelVO> voList) {
        for (LabelVO vo : voList) {
            populateEntityInfo(vo);
        }
    }

    /**
     * 填充单个标签的实体关联信息
     */
    private void populateEntityInfo(LabelVO vo) {
        if (vo.getEntityIdentifierId() == null) {
            return;
        }
        Optional<EntityIdentifier> eiOp = entityIdentifierService.getDetail(vo.getEntityIdentifierId());
        if (eiOp.isPresent()) {
            EntityIdentifier identifier = eiOp.get();
            vo.setEntityIdentifierName(identifier.getEntityIdentifierName());
            vo.setEntityId(identifier.getEntityId());
            vo.setEntityName(identifier.getEntityName());
        }
    }

    /**
     * 填充单个标签的数据集绑定信息
     */
    private void populateDatasetInfo(LabelVO vo, String labelId) {
        DatasetField datasetField = datasetFieldService.getDetailByRelatedId(labelId);
        if (datasetField != null) {
            vo.setDatasetId(datasetField.getDatasetId());
            vo.setDatasetFieldName(datasetField.getFieldName());
        }
    }
}
