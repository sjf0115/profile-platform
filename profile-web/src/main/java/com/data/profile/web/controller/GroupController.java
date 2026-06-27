package com.data.profile.web.controller;

import com.data.profile.web.task.GroupTask;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.*;
import com.data.profile.web.model.Group;
import com.data.profile.web.model.GroupRule;
import com.data.profile.web.model.LabelOperator;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.vo.DatasetVO;
import com.data.profile.web.vo.GroupVO;
import com.data.profile.web.service.GroupService;
import com.data.profile.web.service.TaskExecutionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 功能：群组
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/group", produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupController {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private GroupService groupService;
    @Autowired
    private TaskExecutionService taskExecutionService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Group group) {
        log.info("根据群组条件请求查询群组信息: {}", gson.toJson(group));
        List<GroupVO> groups = groupService.getList(group);
        return Response.success(groups);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "group_id") String groupId) {
        log.info("根据群组ID请求查询群组信息: {}", groupId);
        Optional<GroupVO> optional = groupService.getDetail(groupId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的群组不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Group group) {
        String groupId = group.getGroupId();
        if (StringUtils.isEmpty(groupId)) {
            // 创建群组
            log.info("请求创建群组: {}", gson.toJson(group));
            int result = groupService.create(group);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("创建群组失败", ResponseCode.ERROR);
            }
        } else {
            // 修改群组
            log.info("请求修改群组: {}", gson.toJson(group));
            int result = groupService.update(group);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改群组失败", ResponseCode.ERROR);
            }
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "group_id") String groupId) {
        log.info("根据群组ID {} 请求删除群组", groupId);
        int result = groupService.delete(groupId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除群组失败", ResponseCode.ERROR);
        }
    }

    // 文件上传
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response upload(@RequestPart("file") MultipartFile file) {
        GroupRule groupRule = groupService.upload(file);
        if (!Objects.equals(groupRule, null)) {
            return Response.success(groupRule);
        } else {
            return Response.error("上传 CSV 文件到 MinIO", ResponseCode.ERROR);
        }
    }

    // 取消上传
    @DeleteMapping(value = "/cancel-upload")
    public Response cancelUploaded(@RequestParam(name = "file_key") String fileKey) {
        log.info("删除已上传文件: {}", fileKey);
        groupService.cancelUpload(fileKey);
        return Response.success(null);
    }

    // 下载 CSV 上传模板
    @GetMapping(value = "/template/download")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=group_upload_template.csv");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println("unique_id");
        writer.println("1234567890");
        writer.println("1234567891");
        writer.println("1234567892");
        writer.println("1234567893");
        writer.flush();
    }

    // 预估群组人数
    @PostMapping(value = "/estimate")
    public Response estimate(@RequestBody Group group) {
        log.info("请求预估群组人数");
        try {
            long count = groupService.estimateGroupCount(group.getGroupRule());
            return Response.success(count);
        } catch (Exception e) {
            log.error("群组预估失败", e);
            return Response.error("群组预估失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // 立即执行群组圈选 TODO GroupTask 还是 taskExecutionService ？
    @PostMapping(value = "/{groupId}/execute")
    public Response execute(@PathVariable(value = "groupId") String groupId) {
        log.info("请求手动立即执行群组 [{}] 圈选", groupId);
        try {
            TaskInstance instance = taskExecutionService.executeByRelatedId(groupId, TriggerMode.MANUAL);
            return Response.success(instance);
        } catch (RuntimeException e) {
            log.warn("群组圈选触发被拒绝: groupId={}, reason={}", groupId, e.getMessage());
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        } catch (Exception e) {
            log.error("群组圈选失败: groupId={}", groupId, e);
            return Response.error("群组圈选失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }

    // 获取 SQL 创建可用的数据集表和字段列表
    @GetMapping(value = "/available-tables")
    public Response getAvailableTables(
            @RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("根据实体ID [{}] 请求获取可用数据集表和字段", entityIdentifierId);
        List<DatasetVO> tables = groupService.getAvailableTables(entityIdentifierId);
        return Response.success(tables);
    }

    @GetMapping(value = "/config/label")
    public Response getLabelConfig() {
        log.info("请求获取群组配置: 标签操作符");
        // 1-文本型,2-数值型,3-时间型
        List<LabelOperator> ops = Arrays.asList(
                LabelOperator.builder().code("eq").name("=").types(Arrays.asList(1, 2, 3)).build(),
                LabelOperator.builder().code("ne").name("≠").types(Arrays.asList(1, 2, 3)).build(),
                LabelOperator.builder().code("gte").name("≥").types(Arrays.asList(2, 3)).build(),
                LabelOperator.builder().code("gt").name(">").types(Arrays.asList(2, 3)).build(),
                LabelOperator.builder().code("lte").name("≤").types(Arrays.asList(2, 3)).build(),
                LabelOperator.builder().code("lt").name("<").types(Arrays.asList(2, 3)).build(),
                LabelOperator.builder().code("contains").name("包含").types(Arrays.asList(1)).build(),
                LabelOperator.builder().code("not_contains").name("不包含").types(Arrays.asList(1)).build(),
                LabelOperator.builder().code("is_null").name("为空").types(Arrays.asList(1)).build(),
                LabelOperator.builder().code("is_not_null").name("不为空").types(Arrays.asList(1)).build(),
                LabelOperator.builder().code("in").name("在范围内").types(Arrays.asList(2, 3)).build(),
                LabelOperator.builder().code("not_in").name("不在范围内").types(Arrays.asList(2, 3)).build()
                // 以xxx开始/结束
        );
        return Response.success(ops);
    }
}