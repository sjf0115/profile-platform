package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.EntityIdentifierConverter;
import com.data.profile.web.dto.EntityIdentifierDTO;
import com.data.profile.web.dto.EntityIdentifierParam;
import com.data.profile.web.dto.EntityIdentifierRequest;
import com.data.profile.web.model.EntityIdentifier;
import com.data.profile.web.service.EntityIdentifierService;
import com.data.profile.web.vo.EntityIdentifierVO;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能：实体标识管理
 * 作者：SmartSi
 * 日期：2024/7/7 15:44
 */
@Slf4j
@RestController
@RequestMapping(value = "/entity/identifier", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntityIdentifierController {

    @Autowired
    private EntityIdentifierService entityIdentifierService;

    /**
     * 实体标识列表
     */
    @PostMapping(value = "/list")
    public Response<List<EntityIdentifierVO>> getList(@RequestBody EntityIdentifierParam param) {
        log.info("请求查询实体标识列表：{}", JSONUtils.toJsonString(param));
        EntityIdentifier entityIdentifier = EntityIdentifierConverter.param2do(param);
        List<EntityIdentifierDTO> dtos = entityIdentifierService.getList(entityIdentifier);
        return Response.success(EntityIdentifierConverter.dto2voList(dtos));
    }

    /**
     * 实体标识详情
     */
    @GetMapping(value = "/{entityIdentifierId}/detail")
    public Response<EntityIdentifierVO> getDetail(@PathVariable(value = "entityIdentifierId") String entityIdentifierId) {
        log.info("请求查询实体标识 {} 详细信息", entityIdentifierId);
        EntityIdentifierDTO dto = entityIdentifierService.getDetail(entityIdentifierId);
        if (dto == null) {
            return Response.error("实体标识不存在", ResponseCode.ERROR);
        }
        return Response.success(EntityIdentifierConverter.dto2vo(dto));
    }

    /**
     * 创建实体标识
     */
    @RequiresPermission(code = "entity:identifier:create", name = "实体标识-创建")
    @PostMapping
    public Response<EntityIdentifierVO> create(@RequestBody EntityIdentifierRequest request) {
        log.info("请求创建实体标识：{}", JSONUtils.toJsonString(request));
        try {
            EntityIdentifierDTO dto = entityIdentifierService.create(request);
            return Response.success(EntityIdentifierConverter.dto2vo(dto));
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新实体标识
     */
    @RequiresPermission(code = "entity:identifier:edit", name = "实体标识-编辑")
    @PutMapping("/{entityIdentifierId}")
    public Response<Integer> update(@PathVariable(value = "entityIdentifierId") String entityIdentifierId, @RequestBody EntityIdentifierRequest request) {
        log.info("请求更新实体标识 {}：{}", entityIdentifierId, JSONUtils.toJsonString(request));
        try {
            int result = entityIdentifierService.update(entityIdentifierId, request);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改实体标识失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新实体标识状态（启用/停用）
     */
    @RequiresPermission(code = "entity:identifier:edit", name = "实体标识-编辑")
    @PutMapping("/{entityIdentifierId}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "entityIdentifierId") String entityIdentifierId, @RequestParam Integer status) {
        log.info("请求更新实体标识 {} 状态为：{}", entityIdentifierId, status);
        try {
            int result = entityIdentifierService.updateStatus(entityIdentifierId, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除实体标识
     */
    @RequiresPermission(code = "entity:identifier:delete", name = "实体标识-删除")
    @DeleteMapping("/{entityIdentifierId}")
    public Response<Integer> delete(@PathVariable(value = "entityIdentifierId") String entityIdentifierId) {
        log.info("请求删除实体标识：{}", entityIdentifierId);
        try {
            int result = entityIdentifierService.delete(entityIdentifierId);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除实体标识失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
