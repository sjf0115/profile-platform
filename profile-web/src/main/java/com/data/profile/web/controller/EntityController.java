package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.EntityConverter;
import com.data.profile.web.dto.EntityDTO;
import com.data.profile.web.dto.EntityParam;
import com.data.profile.web.dto.EntityRequest;
import com.data.profile.web.model.Entity;
import com.data.profile.web.service.EntityService;
import com.data.profile.web.vo.EntityVO;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能：实体管理
 * 作者：SmartSi
 * 日期：2024/7/7 15:44
 */
@Slf4j
@RestController
@RequestMapping(value = "/entity", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntityController {
    @Autowired
    private EntityService entityService;

    /**
     * 实体列表
     */
    @PostMapping(value = "/list")
    public Response<List<EntityVO>> getList(@RequestBody EntityParam param) {
        log.info("请求查询实体列表：{}", JSONUtils.toJsonString(param));
        Entity entity = EntityConverter.param2do(param);
        List<EntityDTO> dtos = entityService.getList(entity);
        return Response.success(EntityConverter.dto2voList(dtos));
    }

    /**
     * 实体详情
     */
    @GetMapping(value = "/{entityId}/detail")
    public Response<EntityVO> getDetail(@PathVariable(value = "entityId") String entityId) {
        log.info("请求查询实体 {} 详细信息", entityId);
        EntityDTO dto = entityService.getDetail(entityId);
        if (dto == null) {
            return Response.error("实体不存在", ResponseCode.ERROR);
        }
        return Response.success(EntityConverter.dto2vo(dto));
    }

    /**
     * 创建实体
     */
    @RequiresPermission(code = "entity:create", name = "实体-创建")
    @PostMapping
    public Response<EntityVO> create(@RequestBody EntityRequest request) {
        log.info("请求创建实体：{}", JSONUtils.toJsonString(request));
        try {
            EntityDTO dto = entityService.create(request);
            return Response.success(EntityConverter.dto2vo(dto));
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新实体
     */
    @RequiresPermission(code = "entity:edit", name = "实体-编辑")
    @PutMapping("/{entityId}")
    public Response<Integer> update(@PathVariable(value = "entityId") String entityId, @RequestBody EntityRequest request) {
        log.info("请求更新实体 {}：{}", entityId, JSONUtils.toJsonString(request));
        try {
            int result = entityService.update(entityId, request);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改实体失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新实体状态（启用/停用）
     */
    @RequiresPermission(code = "entity:edit", name = "实体-编辑")
    @PutMapping("/{entityId}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "entityId") String entityId, @RequestParam Integer status) {
        log.info("请求更新实体 {} 状态为：{}", entityId, status);
        try {
            int result = entityService.updateStatus(entityId, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除实体
     */
    @RequiresPermission(code = "entity:delete", name = "实体-删除")
    @DeleteMapping("/{entityId}")
    public Response<Integer> delete(@PathVariable(value = "entityId") String entityId) {
        log.info("请求删除实体：{}", entityId);
        try {
            int result = entityService.delete(entityId);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除实体失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
