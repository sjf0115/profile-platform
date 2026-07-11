package com.data.profile.web.controller;

import com.data.profile.web.model.Permission;
import com.data.profile.web.service.PermissionService;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 功能：权限点
 */
@Slf4j
@RestController
@RequestMapping(value = "/permission", produces = MediaType.APPLICATION_JSON_VALUE)
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取权限点菜单树
     */
    @GetMapping("/tree")
    public Response<List<Permission>> getTree() {
        log.info("查询权限点菜单树");
        return Response.success(permissionService.getPermissionTree());
    }
}
