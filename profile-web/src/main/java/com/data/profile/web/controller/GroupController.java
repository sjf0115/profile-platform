package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.*;
import com.data.profile.model.Group;
import com.data.profile.model.LabelOperator;
import com.data.profile.service.GroupService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Group group) {
        log.info("根据群组条件请求查询群组信息: {}", gson.toJson(group));
        List<Group> groups = groupService.getList(group);
        return Response.success(groups);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "group_id") String groupId) {
        log.info("根据群组ID请求查询群组信息: {}", groupId);
        Optional<Group> optional = groupService.getDetail(groupId);
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