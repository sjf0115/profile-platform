package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.Role;
import com.data.profile.web.service.RoleService;
import lombok.extern.slf4j.Slf4j;
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
    public Response<List<Role>> getList(@RequestBody Role role) {
        log.info("请求查询角色：{}", JSONUtils.toJsonString(role));
        List<Role> roles = roleService.getList(role);
        return Response.success(roles);
    }

    @GetMapping(value = "/{roleId}/detail")
    public Response<Role> getDetail(@PathVariable(value = "roleId") String roleId) {
        log.info("请求查询角色 {} 详细信息", roleId);
        Optional<Role> optional = roleService.getDetail(roleId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的角色不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping
    public Response<Integer> create(@RequestBody Role role) {
        log.info("请求创建角色：{}", JSONUtils.toJsonString(role));
        int result = roleService.create(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加角色失败", ResponseCode.ERROR);
        }
    }

    @PutMapping("/{roleId}")
    public Response<Integer> update(@PathVariable(value = "roleId") String roleId, @RequestBody Role role) {
        role.setRoleId(roleId);
        log.info("请求更新角色：{}", JSONUtils.toJsonString(role));
        int result = roleService.update(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改角色失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/{roleId}")
    public Response<Integer> delete(@PathVariable(value = "roleId") String roleId) {
        log.info("请求删除角色：{}", roleId);
        int result = roleService.delete(roleId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除角色失败", ResponseCode.ERROR);
        }
    }
}
