package com.data.profile.web.controller;

import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.Application;
import com.data.profile.web.service.ApplicationService;
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
    public Response<List<Application>> getList(@RequestBody(required = false) Application application) {
        if (application == null) {
            application = new Application();
        }
        List<Application> list = applicationService.getList(application);
        return Response.success(list);
    }

    /**
     * 模糊查询
     */
    @GetMapping(value = "/keyword")
    public Response<List<Application>> getByKeyword(@RequestParam String keyword) {
        List<Application> list = applicationService.getByKeyword(keyword);
        return Response.success(list);
    }

    /**
     * 应用详情
     */
    @GetMapping(value = "/detail")
    public Response<Application> getDetail(@RequestParam Long id) {
        Optional<Application> optional = applicationService.getDetail(id);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("应用不存在", ResponseCode.ERROR);
        }
    }

    /**
     * 创建/编辑应用
     */
    @PostMapping(value = "/save")
    public Response<Application> save(@RequestBody Application application) {
        try {
            Application result = applicationService.save(application);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除应用
     */
    @DeleteMapping(value = "/delete")
    public Response<Integer> delete(@RequestParam Long id) {
        try {
            int result = applicationService.delete(id);
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
    @PostMapping(value = "/reset-secret")
    public Response<String> resetSecret(@RequestParam Long id) {
        try {
            String newSecret = applicationService.resetSecret(id);
            return Response.success(newSecret);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }
}
