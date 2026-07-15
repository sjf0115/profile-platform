package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.LabelConverter;
import com.data.profile.web.dto.LabelDTO;
import com.data.profile.web.dto.LabelParam;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.model.Label;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 标签管理
 */
@Slf4j
@RestController
@RequestMapping(value = "/label", produces = MediaType.APPLICATION_JSON_VALUE)
public class LabelController {
    @Autowired
    private LabelService labelService;

    // 标签列表
    @PostMapping(value = "/list")
    public Response<List<LabelVO>> getList(@RequestBody LabelParam param) {
        log.info("查询标签列表: {}", JSONUtils.toJsonString(param));
        Label label = LabelConverter.param2do(param);
        List<LabelDTO> dtos = labelService.getListDTO(label);
        List<LabelVO> voList = LabelConverter.dto2voList(dtos);
        return Response.success(voList);
    }

    // 标签详情
    @GetMapping(value = "/{labelId}/detail")
    public Response<LabelVO> getDetail(@PathVariable("labelId") String labelId) {
        log.info("查询标签详情: labelId={}", labelId);
        LabelDTO dto = labelService.getDetail(labelId);
        if (dto == null) {
            return Response.error("请求的标签不存在", ResponseCode.ERROR);
        }
        LabelVO vo = LabelConverter.dto2vo(dto);
        return Response.success(vo);
    }

    // 创建标签
    @RequiresPermission(code = "label:edit", name = "标签-编辑")
    @PostMapping
    public Response<Integer> create(@RequestBody LabelRequest request) {
        log.info("创建标签: {}", JSONUtils.toJsonString(request));
        Label label = LabelConverter.request2do(request);
        int result = labelService.create(label, request.getDatasetId(), request.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("创建标签失败", ResponseCode.ERROR);
        }
    }

    // 更新标签
    @RequiresPermission(code = "label:edit", name = "标签-编辑")
    @PutMapping(value = "/{labelId}")
    public Response<Integer> update(@PathVariable("labelId") String labelId, @RequestBody LabelRequest request) {
        log.info("更新标签: labelId={}, {}", labelId, JSONUtils.toJsonString(request));
        Label label = LabelConverter.request2do(request);
        int result = labelService.update(labelId, label, request.getDatasetId(), request.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("更新标签失败", ResponseCode.ERROR);
        }
    }

    // 删除标签
    @RequiresPermission(code = "label:delete", name = "标签-删除")
    @DeleteMapping(value = "/{labelId}")
    public Response<Integer> delete(@PathVariable("labelId") String labelId) {
        log.info("删除标签: labelId={}", labelId);
        int result = labelService.delete(labelId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除标签失败", ResponseCode.ERROR);
        }
    }

    // 未绑定数据集的标签（数据集创建/编辑场景）
    @GetMapping(value = "/unbound")
    public Response<List<LabelVO>> getUnboundLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId,
            @RequestParam(name = "dataset_id", required = false) String datasetId) {
        log.info("获取未绑定标签: entityIdentifierId={}, datasetId={}", entityIdentifierId, datasetId);
        List<Label> labels = labelService.getAvailableList(entityIdentifierId, datasetId);
        return Response.success(LabelConverter.do2voList(labels));
    }

    // 线上可用标签（群组规则/分析场景）
    @GetMapping(value = "/online")
    public Response<List<LabelVO>> getOnlineLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("获取线上标签: entityIdentifierId={}", entityIdentifierId);
        List<Label> labels = labelService.getOnlineList(entityIdentifierId);
        return Response.success(LabelConverter.do2voList(labels));
    }

    // 标签配置枚举
    @GetMapping(value = "/config")
    public Response<Map<String, Object>> getConfig() {
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

}
