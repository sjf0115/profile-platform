package com.data.profile.web.controller;


import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Label;
import com.data.profile.service.LabelService;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    private static Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private LabelService labelService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody Label label) {
        List<Label> labels = labelService.getList(label);
        return Response.success(labels);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String labelId) {
        Optional<Label> optional = labelService.getDetail(labelId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的标签不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Label label) {
        int result = labelService.save(label);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加标签失败", ResponseCode.ERROR);
        }
    }

    /*@PostMapping(value = "/upload")
    public Response upload(@RequestParam Mutip labelId) {
        int result = labelService.save(label);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加标签失败", ResponseCode.ERROR);
        }
    }*/
}