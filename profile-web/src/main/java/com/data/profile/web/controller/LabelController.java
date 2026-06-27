package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.*;
import com.data.profile.web.model.Label;
import com.data.profile.web.vo.LabelVO;
import com.data.profile.web.service.LabelService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private LabelService labelService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Label label) {
        log.info("请求根据标签信息请求查询标签: {}", gson.toJson(label));
        List<LabelVO> labels = labelService.getList(label);
        return Response.success(labels);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "label_id") String labelId) {
        log.info("根据标签ID [{}] 请求查询标签信息", labelId);
        Optional<LabelVO> optional = labelService.getDetail(labelId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的标签不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody LabelVO labelVO) {
        log.info("请求保存/更新标签信息: {}", gson.toJson(labelVO));
        Label label = new Label();
        org.springframework.beans.BeanUtils.copyProperties(labelVO, label);
        int result = labelService.save(label, labelVO.getDatasetId(), labelVO.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加标签失败", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/update")
    public Response update(@RequestBody LabelVO labelVO) {
        log.info("请求更新标签信息: {}", gson.toJson(labelVO));
        Label label = new Label();
        org.springframework.beans.BeanUtils.copyProperties(labelVO, label);
        int result = labelService.save(label, labelVO.getDatasetId(), labelVO.getDatasetFieldName());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("更新标签失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "label_id") String labelId) {
        log.info("请求删除标签: {}", labelId);
        int result = labelService.delete(labelId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除标签失败", ResponseCode.ERROR);
        }
    }

    /**
     * 获取可用标签列表，未被其他数据集绑定的标签
     * 编辑模式传入 dataset_id 可保留当前数据集已绑定标签
     */
    @GetMapping(value = "/available")
    public Response getAvailableLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId,
            @RequestParam(name = "dataset_id", required = false) String datasetId) {
        log.info("请求获取实体 [{}] 下未被 [{}] 之外数据集绑定的可用标签", entityIdentifierId, datasetId);
        List<LabelVO> labels = labelService.getAvailableList(entityIdentifierId, datasetId);
        return Response.success(labels);
    }

    @GetMapping(value = "/config")
    public Response getConfig() {
        log.info("请求获取标签配置信息");
        Map<String, Object> config = new HashMap<>();

        // 标签类型: 1-属性标签,2-行为标签
        config.put("label_type", Stream.of(LabelType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));

        // 标签数据类型: 1-文本型, 2-数值型, 3-时间型
        config.put("data_type", Stream.of(LabelDataType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        
        // 标签数据分布类型: 1-枚举, 2-非枚举
        config.put("dist_type", Stream.of(LabelDistType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        
        // 标签组织类型: 1-单值, 2-多值, 3-KV, 4-KKV
        config.put("organize_type", Stream.of(LabelOrganizeType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        
        // 标签加工类型: 0-未知,1-事实标签,2-统计标签,3-预测标签
        config.put("produce_type", Stream.of(LabelProduceType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        
        // 标签时效性类型: 0-未知,1-离线标签,2-实时标签
        config.put("time_type", Stream.of(LabelTimeType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));

        // 创建方式: 1-系统内置,2-数据源导入,3-文件上传,4-四则运算,5-SQL计算,6-自定义规则,7-API导入,8-数据表导入
        config.put("source_type", Stream.of(LabelSourceType.values())
                .map(e -> ImmutableMap.of("id", e.getCode(), "name", e.getMessage()))
                .collect(Collectors.toList()));
        
        return Response.success(config);
    }
}