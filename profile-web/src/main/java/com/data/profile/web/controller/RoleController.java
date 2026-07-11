package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.RoleConverter;
import com.data.profile.web.dto.RoleParam;
import com.data.profile.web.dto.RoleRequest;
import com.data.profile.web.vo.Response;
import com.data.profile.web.vo.RoleVO;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.model.Role;
import com.data.profile.web.service.PermissionService;
import com.data.profile.web.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    @Autowired
    private PermissionService permissionService;

    @PostMapping(value = "/list")
    public Response<List<RoleVO>> getList(@RequestBody RoleParam param) {
        log.info("请求查询角色：{}", JSONUtils.toJsonString(param));
        Role query = RoleConverter.param2do(param);
        List<Role> roles = roleService.getList(query);
        return Response.success(RoleConverter.do2voList(roles));
    }

    @GetMapping(value = "/{roleId}/detail")
    public Response<RoleVO> getDetail(@PathVariable(value = "roleId") String roleId) {
        log.info("请求查询角色 {} 详细信息", roleId);
        Optional<Role> optional = roleService.getDetail(roleId);
        if (optional.isPresent()) {
            return Response.success(RoleConverter.do2vo(optional.get()));
        } else {
            return Response.error("请求的角色不存在", ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "role:create", name = "角色-创建")
    @PostMapping
    public Response<Integer> create(@RequestBody RoleRequest request) {
        log.info("请求创建角色：{}", JSONUtils.toJsonString(request));
        Role role = RoleConverter.request2do(request);
        int result = roleService.create(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加角色失败", ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "role:edit", name = "角色-编辑")
    @PutMapping("/{roleId}")
    public Response<Integer> update(@PathVariable(value = "roleId") String roleId, @RequestBody RoleRequest request) {
        request.setRoleId(roleId);
        log.info("请求更新角色：{}", JSONUtils.toJsonString(request));
        Role role = RoleConverter.request2do(request);
        int result = roleService.update(role);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改角色失败", ResponseCode.ERROR);
        }
    }

    @RequiresPermission(code = "role:delete", name = "角色-删除")
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

    /**
     * 获取角色已勾选的权限点ID列表
     */
    @GetMapping("/{roleId}/permissions")
    public Response<List<String>> getRolePermissions(@PathVariable(value = "roleId") String roleId) {
        log.info("查询角色 {} 的权限列表", roleId);
        return Response.success(permissionService.getPermissionsByRoleId(roleId));
    }

    /**
     * 保存角色权限配置
     */
    @PutMapping("/{roleId}/permissions")
    public Response<Integer> saveRolePermissions(@PathVariable(value = "roleId") String roleId,
                                                  @RequestBody Map<String, List<String>> body) {
        log.info("保存角色 {} 的权限配置", roleId);
        List<String> permissionIds = body.get("permissionIds");
        int result = permissionService.saveRolePermissions(roleId, permissionIds);
        return Response.success(result);
    }
}
