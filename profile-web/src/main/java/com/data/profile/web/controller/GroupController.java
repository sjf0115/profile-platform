package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Group;
import com.data.profile.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

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
    @Autowired
    private GroupService groupService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Group group) {
        List<Group> groups = groupService.getList(group);
        return Response.success(groups);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "group_id") String groupId) {
        Optional<Group> optional = groupService.getDetail(groupId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的群组不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Group group) {
        int result = groupService.save(group);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加群组失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "group_id") String groupId) {
        int result = groupService.delete(groupId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除群组失败", ResponseCode.ERROR);
        }
    }
}