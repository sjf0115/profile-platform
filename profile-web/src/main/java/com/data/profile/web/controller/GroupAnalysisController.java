package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.converter.AnalysisLabelConverter;
import com.data.profile.web.converter.GroupAnalysisConverter;
import com.data.profile.web.converter.GroupConverter;
import com.data.profile.web.converter.LabelDistributionConverter;
import com.data.profile.web.dto.AnalysisLabelDTO;
import com.data.profile.web.dto.GroupAnalysisDTO;
import com.data.profile.web.dto.GroupAnalysisRequest;
import com.data.profile.web.dto.GroupDTO;
import com.data.profile.web.dto.LabelDistributionDTO;
import com.data.profile.web.model.Group;
import com.data.profile.web.model.GroupAnalysis;
import com.data.profile.web.service.GroupAnalysisService;
import com.data.profile.web.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 功能：群组分析
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
@Slf4j
@RestController
@RequestMapping(value = "/group/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupAnalysisController {

    @Autowired
    private GroupAnalysisService groupAnalysisService;

    /**
     * 获取群组分析列表
     */
    // TODO GroupAnalysis -> GroupAnalysisRequest
    @PostMapping(value = "/list")
    public Response<List<GroupAnalysisVO>> getAnalysisList(@RequestBody(required = false) GroupAnalysis query) {
        log.info("请求获取群组分析列表");
        try {
            List<GroupAnalysisDTO> dtos = groupAnalysisService.getAnalysisList(query != null ? query : new GroupAnalysis());
            return Response.success(GroupAnalysisConverter.dto2voList(dtos));
        } catch (Exception e) {
            log.error("获取群组分析列表失败", e);
            return Response.error("获取群组分析列表失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取群组分析详情
     */
    @GetMapping(value = "/{analysisId}/detail")
    public Response<GroupAnalysisVO> getAnalysisDetail(@PathVariable(name = "analysisId") String analysisId) {
        log.info("请求获取群组分析详情: {}", analysisId);
        Optional<GroupAnalysisDTO> opt = groupAnalysisService.getAnalysisDetail(analysisId);
        if (opt.isPresent()) {
            return Response.success(GroupAnalysisConverter.dto2vo(opt.get()));
        } else {
            return Response.error("群组分析不存在", ResponseCode.ERROR);
        }
    }

    /**
     * 保存群组分析（新增/修改）
     */
    @RequiresPermission(code = "groupAnalysis:edit", name = "群组分析-编辑")
    @PostMapping(value = "/save")
    public Response<Integer> saveAnalysis(@RequestBody GroupAnalysis analysis) {
        log.info("请求保存群组分析: {}", JSONUtils.toJsonString(analysis));
        try {
            int result = groupAnalysisService.saveAnalysis(analysis);
            return Response.success(result);
        } catch (Exception e) {
            log.error("保存群组分析失败", e);
            return Response.error("保存群组分析失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除群组分析
     */
    @RequiresPermission(code = "groupAnalysis:delete", name = "群组分析-删除")
    @DeleteMapping(value = "/delete")
    public Response<Integer> deleteAnalysis(@RequestParam(name = "analysis_id") String analysisId) {
        log.info("请求删除群组分析: {}", analysisId);
        try {
            int result = groupAnalysisService.deleteAnalysis(analysisId);
            return Response.success(result);
        } catch (Exception e) {
            log.error("删除群组分析失败", e);
            return Response.error("删除群组分析失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // ========== 分析计算接口 ==========

    /**
     * 获取可分析群组列表（用于创建分析时选择群组）
     */
    @PostMapping(value = "/groups")
    public Response<List<GroupVO>> getAnalyzableGroups(@RequestBody(required = false) Group query) {
        log.info("请求获取可分析群组列表");
        try {
            List<GroupDTO> dtos = groupAnalysisService.getAnalyzableGroups(query != null ? query : new Group());
            return Response.success(GroupConverter.dto2voList(dtos));
        } catch (Exception e) {
            log.error("获取可分析群组列表失败", e);
            return Response.error("获取可分析群组列表失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取可分析标签列表
     */
    @GetMapping(value = "/labels")
    public Response<List<AnalysisLabelVO>> getAvailableLabels(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("请求获取实体 [{}] 的可分析标签", entityIdentifierId);
        try {
            List<AnalysisLabelDTO> dtos = groupAnalysisService.getAvailableLabels(entityIdentifierId);
            return Response.success(AnalysisLabelConverter.dto2voList(dtos));
        } catch (Exception e) {
            log.error("获取可分析标签失败", e);
            return Response.error("获取可分析标签失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取单个标签的分布数据
     */
    @PostMapping(value = "/distribution")
    public Response<LabelDistributionVO> getLabelDistribution(@RequestBody GroupAnalysisRequest request) {
        log.info("请求获取群组 [{}] 标签 [{}] 的分布", request.getGroupId(), request.getLabelId());
        try {
            LabelDistributionDTO dto = groupAnalysisService.getLabelDistribution(request);
            if (dto == null) {
                return Response.error("获取标签分布失败，请连续管理员", ResponseCode.ERROR);
            }
            return Response.success(LabelDistributionConverter.dto2vo(dto));
        } catch (Exception e) {
            log.error("获取标签分布失败", e);
            return Response.error("获取标签分布失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }
}
