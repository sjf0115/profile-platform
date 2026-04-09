package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.Role;
import com.data.profile.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：角色
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/role", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody Role role) {
        List<Role> roles = roleService.getList(role);
        return Response.success(roles);
    }

    @GetMapping(value = "/{roleId}/detail")
    public Response getDetail(@PathVariable(value = "roleId") String roleId) {
        Optional<Role> optional = roleService.getDetail(roleId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的角色不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping
    public Response create(@RequestBody Role role) {
        int result = roleService.create(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加角色失败", ResponseCode.ERROR);
        }
    }

    @PutMapping("/{userId}")
    public Response update(@PathVariable(value = "roleId") String roleId, @RequestBody Role role) {
        role.setRoleId(roleId);
        int result = roleService.update(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改角色失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/{roleId}")
    public Response delete(@PathVariable(value = "roleId") String roleId) {
        int result = roleService.delete(roleId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除角色失败", ResponseCode.ERROR);
        }
    }
}
