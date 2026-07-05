package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.dto.GroupAnalysisRequest;
import com.data.profile.web.model.Group;
import com.data.profile.web.model.GroupAnalysis;
import com.data.profile.web.model.Label;
import com.data.profile.web.service.GroupAnalysisService;
import com.data.profile.web.vo.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

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
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private GroupAnalysisService groupAnalysisService;
    @Resource
    private LabelMapper labelMapper;

    // ========== 群组分析 CRUD ==========

    /**
     * 获取群组分析列表
     */
    @PostMapping(value = "/list")
    public Response<List<GroupAnalysisVO>> getAnalysisList(@RequestBody(required = false) GroupAnalysis query) {
        log.info("请求获取群组分析列表");
        try {
            List<GroupAnalysisVO> list = groupAnalysisService.getAnalysisList(query != null ? query : new GroupAnalysis());
            return Response.success(list);
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
        Optional<GroupAnalysisVO> opt = groupAnalysisService.getAnalysisDetail(analysisId);
        if (opt.isPresent()) {
            return Response.success(opt.get());
        } else {
            return Response.error("群组分析不存在", ResponseCode.ERROR);
        }
    }

    /**
     * 保存群组分析（新增/修改）
     */
    @PostMapping(value = "/save")
    public Response<Integer> saveAnalysis(@RequestBody GroupAnalysis analysis) {
        log.info("请求保存群组分析: {}", gson.toJson(analysis));
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
            List<GroupVO> groups = groupAnalysisService.getAnalyzableGroups(query != null ? query : new Group());
            return Response.success(groups);
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
            List<AnalysisLabelVO> labels = groupAnalysisService.getAvailableLabels(entityIdentifierId);
            return Response.success(labels);
        } catch (Exception e) {
            log.error("获取可分析标签失败", e);
            return Response.error("获取可分析标签失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 获取标签分布数据（Mock，后续补全Service层）
     */
    @PostMapping(value = "/distribution")
    public Response<List<LabelDistributionVO>> getLabelDistribution(@RequestBody GroupAnalysisRequest request) {
        log.info("请求获取标签分布(Mock): groupId={}, labelIds={}", request.getGroupId(), request.getLabelIds());
        // 查找标签真实名称
        Map<String, String> labelNameMap = new HashMap<>();
        for (String labelId : request.getLabelIds()) {
            Label label = labelMapper.selectByLabelId(labelId);
            if (label != null) {
                labelNameMap.put(labelId, label.getLabelName());
            }
        }
        List<LabelDistributionVO> result = new ArrayList<>();
        Random rand = new Random(42); // 固定种子保证同参数同结果
        String[][] mockValues = {
            {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京", "西安", "重庆"},
            {"丰田", "大众", "本田", "日产", "宝马", "奔驰", "奥迪", "特斯拉", "比亚迪", "理想"},
            {"男", "女"},
            {"18-25岁", "26-35岁", "36-45岁", "46-55岁", "55岁以上"},
            {"高", "中", "低"},
            {"是", "否"}
        };
        for (String labelId : request.getLabelIds()) {
            LabelDistributionVO dist = new LabelDistributionVO();
            String labelName = labelNameMap.getOrDefault(labelId, labelId);
            dist.setLabelId(labelId);
            dist.setLabelName(labelName);
            dist.setDatasetName("mock_dataset");
            dist.setUpdateType("手动更新");
            String[] values = mockValues[rand.nextInt(mockValues.length)];
            List<DistributionItemVO> items = new ArrayList<>();
            long totalCurrent = 0, totalAll = 0;
            long[] currentCounts = new long[values.length];
            long[] allCounts = new long[values.length];
            for (int i = 0; i < values.length; i++) {
                currentCounts[i] = rand.nextInt(500) + 20;
                allCounts[i] = rand.nextInt(2000) + 100;
                totalCurrent += currentCounts[i];
                totalAll += allCounts[i];
            }
            for (int i = 0; i < values.length; i++) {
                DistributionItemVO item = new DistributionItemVO();
                item.setValue(values[i]);
                item.setCurrentCount(currentCounts[i]);
                item.setCurrentRate(totalCurrent > 0 ? (double) currentCounts[i] / totalCurrent * 100 : 0);
                item.setAllCount(allCounts[i]);
                item.setAllRate(totalAll > 0 ? (double) allCounts[i] / totalAll * 100 : 0);
                items.add(item);
            }
            items.sort((a, b) -> Long.compare(b.getCurrentCount(), a.getCurrentCount()));
            dist.setValues(items);
            result.add(dist);
        }
        return Response.success(result);
    }
}
