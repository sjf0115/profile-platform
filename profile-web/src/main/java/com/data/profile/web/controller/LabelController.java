package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.*;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.LabelConverter;
import com.data.profile.web.dto.LabelDTO;
import com.data.profile.web.dto.LabelParam;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.model.FileImportLabelConfig;
import com.data.profile.web.model.Label;
import com.data.profile.web.vo.LabelVO;
import com.data.profile.web.service.LabelService;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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

    // 获取可绑定到数据集的标签（数据集创建/编辑场景）
    @GetMapping(value = "/unbound")
    public Response<List<LabelVO>> getUnboundLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId,
            @RequestParam(name = "dataset_id", required = false) String datasetId) {
        log.info("请求获取数据集 [{}] 可以绑定的标签", datasetId);
        List<Label> labels = labelService.getUnboundLabels(entityIdentifierId, datasetId);
        return Response.success(LabelConverter.do2voList(labels));
    }

    // 线上可用标签（群组规则/分析场景）
    @GetMapping(value = "/online")
    public Response<List<LabelVO>> getOnlineLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("获取实体 {} 所有在线标签", entityIdentifierId);
        List<Label> labels = labelService.getOnlineLabels(entityIdentifierId);
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

    // 上传 CSV 文件到 MinIO
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<FileImportLabelConfig> upload(@RequestPart("file") MultipartFile file) {
        log.info("上传标签文件: {}", file.getOriginalFilename());
        FileImportLabelConfig config = labelService.upload(file);
        if (config != null) {
            return Response.success(config);
        } else {
            return Response.error("上传 CSV 文件到 MinIO 失败", ResponseCode.ERROR);
        }
    }

    // 取消上传（删除 MinIO 文件）
    @DeleteMapping(value = "/cancel-upload")
    public Response<Void> cancelUploaded(@RequestParam(name = "file_key") String fileKey) {
        log.info("取消标签文件上传: {}", fileKey);
        labelService.cancelUpload(fileKey);
        return Response.success(null);
    }

    // 下载上传模板
    @GetMapping(value = "/template/download")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=label_upload_template.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println("entity_id,label_value");
        writer.println("10001,\u666e\u901a\u7528\u6237");
        writer.println("10002,VIP\u7528\u6237");
        writer.println("10003,\u666e\u901a\u7528\u6237");
        writer.flush();
    }

    // 刷新文件上传标签（重新解析 CSV 并重建引擎表）
    @RequiresPermission(code = "label:edit", name = "标签-编辑")
    @PostMapping(value = "/{labelId}/refresh")
    public Response<Void> refreshFileUpload(@PathVariable("labelId") String labelId) {
        log.info("刷新文件上传标签: labelId={}", labelId);
        labelService.refreshFileUpload(labelId);
        return Response.success(null);
    }

}
