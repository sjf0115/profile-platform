package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.vo.Response;
import com.data.profile.web.model.ResourceGrant;
import com.data.profile.web.service.ResourceGrantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资源授权管理
 */
@Slf4j
@RestController
@RequestMapping(value = "/grant", produces = MediaType.APPLICATION_JSON_VALUE)
public class ResourceGrantController {

    @Autowired
    private ResourceGrantService resourceGrantService;

    /**
     * 批量授权
     * body: { resourceType, resourceIds[], granteeType, granteeId, actions[], expireTime }
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @PostMapping("/batch")
    public Response<Integer> batchGrant(@RequestBody Map<String, Object> body) {
        String resourceType = (String) body.get("resourceType");
        @SuppressWarnings("unchecked")
        List<String> resourceIds = (List<String>) body.get("resourceIds");
        int granteeType = (int) body.get("granteeType");
        String granteeId = (String) body.get("granteeId");
        @SuppressWarnings("unchecked")
        List<Integer> actions = (List<Integer>) body.get("actions");
        Date expireTime = null;
        if (body.get("expireTime") != null) {
            try {
                expireTime = new Date(Long.parseLong(body.get("expireTime").toString()));
            } catch (NumberFormatException ignored) {}
        }

        int count = resourceGrantService.grant(resourceType, resourceIds, granteeType, granteeId, actions, expireTime);
        return Response.success(count);
    }

    /**
     * 授权台账（按资源查询）
     */
    @GetMapping("/list")
    public Response<List<ResourceGrant>> list(@RequestParam String resourceType,
                                               @RequestParam String resourceId) {
        return Response.success(resourceGrantService.listByResource(resourceType, resourceId));
    }

    /**
     * 回收授权
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @DeleteMapping("/{grantId}")
    public Response<Integer> revoke(@PathVariable String grantId) {
        return Response.success(resourceGrantService.revoke(grantId));
    }
}
