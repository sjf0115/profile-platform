package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.EntityIdentifier;
import com.data.profile.web.service.EntityIdentifierService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：实体标识
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/entity/identifier", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntityIdentifierController {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private EntityIdentifierService entityIdentifierService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody EntityIdentifier entityIdentifier) {
        log.info("根据查询条件请求获取实体标识: {}", gson.toJson(entityIdentifier));
        List<EntityIdentifier> entityIdentifiers = entityIdentifierService.getList(entityIdentifier);
        return Response.success(entityIdentifiers);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("根据实体标识ID请求获取实体标识: {}", entityIdentifierId);
        Optional<EntityIdentifier> optional = entityIdentifierService.getDetail(entityIdentifierId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的实体标识不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody EntityIdentifier entityIdentifier) {
        log.info("请求保存实体标识: {}", gson.toJson(entityIdentifier));
        int result = entityIdentifierService.save(entityIdentifier);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加实体标识失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam(name = "entity_identifier_id") String entityIdentifierId) {
        log.info("根据实体标识ID请求删除实体标识: {}", entityIdentifierId);
        int result = entityIdentifierService.delete(entityIdentifierId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除实体标识失败", ResponseCode.ERROR);
        }
    }
}