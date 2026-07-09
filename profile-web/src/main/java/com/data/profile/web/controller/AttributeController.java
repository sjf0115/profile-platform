package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.Attribute;
import com.data.profile.web.service.AttributeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：事件属性
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/attr", produces = MediaType.APPLICATION_JSON_VALUE)
public class AttributeController {
    private static Logger LOG = LoggerFactory.getLogger(AttributeController.class);

    @Autowired
    private AttributeService attrService;

    @GetMapping(value = "/list")
    public Response<List<Attribute>> getList(@RequestBody Attribute attr) {
        List<Attribute> attrs = attrService.getList(attr);
        return Response.success(attrs);
    }

    @GetMapping(value = "/detail")
    public Response<Attribute> getDetail(@RequestParam String entityId) {
        Optional<Attribute> optional = attrService.getDetail(entityId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的事件属性不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response<Integer> save(@RequestBody Attribute attr) {
        int result = attrService.save(attr);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加事件属性失败", ResponseCode.ERROR);
        }
    }
}