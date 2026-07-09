package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.Entity;
import com.data.profile.web.service.EntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：实体
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/entity", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntityController {
    @Autowired
    private EntityService entityService;

    @PostMapping(value = "/list")
    public Response<List<Entity>> getList(@RequestBody Entity entity) {
        List<Entity> entities = entityService.getList(entity);
        return Response.success(entities);
    }

    @GetMapping(value = "/detail")
    public Response<Entity> getDetail(@RequestParam(name = "entity_id") String entityId) {
        Optional<Entity> optional = entityService.getDetail(entityId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的实体不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response<Integer> save(@RequestBody Entity entity) {
        int result = entityService.save(entity);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加实体失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response<Integer> delete(@RequestParam(name = "entity_id", required = true) String entityId) {
        int result = entityService.delete(entityId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除实体失败", ResponseCode.ERROR);
        }
    }
}