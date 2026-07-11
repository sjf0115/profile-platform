package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.ApplicationConverter;
import com.data.profile.web.dto.ApplicationDTO;
import com.data.profile.web.dto.ApplicationParam;
import com.data.profile.web.dto.ApplicationRequest;
import com.data.profile.web.model.Application;
import com.data.profile.web.service.ApplicationService;
import com.data.profile.web.vo.ApplicationVO;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 应用管理
 */
@Slf4j
@RestController
@RequestMapping(value = "/application", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    /**
     * 应用列表（支持搜索/筛选）
     */
    @PostMapping(value = "/list")
    public Response<List<ApplicationVO>> getList(@RequestBody ApplicationParam param) {
        log.info("请求查询应用列表：{}", JSONUtils.toJsonString(param));
        Application application = ApplicationConverter.param2do(param);
        List<ApplicationDTO> dtos = applicationService.getList(application);
        return Response.success(ApplicationConverter.dto2voList(dtos));
    }

    /**
     * 应用详情
     */
    @GetMapping(value = "/{appKey}/detail")
    public Response<ApplicationVO> getDetail(@PathVariable(value = "appKey") String appKey) {
        log.info("请求查询应用 {} 详细信息", appKey);
        Optional<ApplicationDTO> opt = applicationService.getDetail(appKey);
        if (!opt.isPresent()) {
            return Response.error("应用不存在", ResponseCode.ERROR);
        }
        return Response.success(ApplicationConverter.dto2vo(opt.get()));
    }

    /**
     * 创建应用
     */
    @RequiresPermission(code = "application:create", name = "应用-创建")
    @PostMapping
    public Response<ApplicationVO> create(@RequestBody ApplicationRequest request) {
        log.info("请求创建应用：{}", JSONUtils.toJsonString(request));
        try {
            ApplicationDTO dto = applicationService.create(request);
            return Response.success(ApplicationConverter.dto2vo(dto));
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新应用
     */
    @RequiresPermission(code = "application:edit", name = "应用-编辑")
    @PutMapping("/{appKey}")
    public Response<Integer> update(@PathVariable(value = "appKey") String appKey,
                                    @RequestBody ApplicationRequest request) {
        log.info("请求更新应用 {}：{}", appKey, JSONUtils.toJsonString(request));
        try {
            int result = applicationService.update(appKey, request);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改应用失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除应用
     */
    @RequiresPermission(code = "application:delete", name = "应用-删除")
    @DeleteMapping("/{appKey}")
    public Response<Integer> delete(@PathVariable(value = "appKey") String appKey) {
        log.info("请求删除应用：{}", appKey);
        try {
            int result = applicationService.delete(appKey);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除应用失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 重置 AppSecret
     */
    @RequiresPermission(code = "application:edit", name = "应用-编辑")
    @PostMapping("/{appKey}/reset-secret")
    public Response<String> resetSecret(@PathVariable(value = "appKey") String appKey) {
        log.info("请求重置应用 {} 的 AppSecret", appKey);
        try {
            String newSecret = applicationService.resetSecret(appKey);
            return Response.success(newSecret);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新应用状态（启用/停用）
     */
    @RequiresPermission(code = "application:edit", name = "应用-编辑")
    @PutMapping("/{appKey}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "appKey") String appKey, @RequestParam Integer status) {
        log.info("请求更新应用 {} 状态为：{}", appKey, status);
        try {
            int result = applicationService.updateStatus(appKey, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
