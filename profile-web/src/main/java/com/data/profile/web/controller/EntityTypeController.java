package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Entity;
import com.data.profile.model.EntityType;
import com.data.profile.service.EntityService;
import com.data.profile.service.EntityTypeService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：实体类型
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/entityType", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntityTypeController {
    private static Logger LOG = LoggerFactory.getLogger(EntityTypeController.class);

    @Autowired
    private EntityTypeService entityTypeService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody EntityType entityType) {
        List<EntityType> entityTypes = entityTypeService.getList(entityType);
        return Response.success(entityTypes);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String entityTypeId) {
        Optional<EntityType> optional = entityTypeService.getDetail(entityTypeId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的实体类型不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody EntityType entityType) {
        int result = entityTypeService.save(entityType);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加实体类型失败", ResponseCode.ERROR);
        }
    }
}