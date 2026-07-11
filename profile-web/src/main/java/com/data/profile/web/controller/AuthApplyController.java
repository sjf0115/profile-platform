package com.data.profile.web.controller;

import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.vo.Response;
import com.data.profile.web.model.AuthApply;
import com.data.profile.web.model.AuthApplyItem;
import com.data.profile.web.service.AuthApplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限申请审批
 */
@Slf4j
@RestController
@RequestMapping(value = "/authApply", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthApplyController {

    @Autowired
    private AuthApplyService authApplyService;

    /**
     * 提交权限申请
     * body: { applyReason, items: [{ resourceType, resourceId, action, expireTime }] }
     */
    @PostMapping("/submit")
    @SuppressWarnings("unchecked")
    public Response<String> submit(@RequestBody Map<String, Object> body) {
        String applyReason = (String) body.get("applyReason");
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) body.get("items");

        List<AuthApplyItem> items = null;
        if (itemMaps != null) {
            items = new java.util.ArrayList<>();
            for (Map<String, Object> m : itemMaps) {
                AuthApplyItem item = AuthApplyItem.builder()
                        .resourceType((String) m.get("resourceType"))
                        .resourceId((String) m.get("resourceId"))
                        .action((Integer) m.get("action"))
                        .build();
                if (m.get("expireTime") != null) {
                    try {
                        item.setExpireTime(new java.util.Date(Long.parseLong(m.get("expireTime").toString())));
                    } catch (NumberFormatException ignored) {}
                }
                items.add(item);
            }
        }

        String applyId = authApplyService.submit(applyReason, items);
        return Response.success(applyId);
    }

    /**
     * 我的申请列表
     */
    @GetMapping("/mine")
    public Response<List<AuthApply>> mine() {
        return Response.success(authApplyService.mine());
    }

    /**
     * 待审批列表
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @GetMapping("/todo")
    public Response<List<AuthApply>> todo() {
        return Response.success(authApplyService.todo());
    }

    /**
     * 已审批列表（我审批过的）
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @GetMapping("/approved")
    public Response<List<AuthApply>> approved() {
        return Response.success(authApplyService.approved());
    }

    /**
     * 申请明细
     */
    @GetMapping("/items")
    public Response<List<AuthApplyItem>> items(@RequestParam String applyId) {
        return Response.success(authApplyService.items(applyId));
    }

    /**
     * 审批通过
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @PostMapping("/approve")
    public Response<String> approve(@RequestBody Map<String, String> body) {
        String applyId = body.get("applyId");
        String approveRemark = body.get("approveRemark");
        authApplyService.approve(applyId, approveRemark);
        return Response.success("ok");
    }

    /**
     * 审批拒绝
     */
    @RequiresPermission(code = "grant:edit", name = "授权管理-编辑")
    @PostMapping("/reject")
    public Response<String> reject(@RequestBody Map<String, String> body) {
        String applyId = body.get("applyId");
        String approveRemark = body.get("approveRemark");
        authApplyService.reject(applyId, approveRemark);
        return Response.success("ok");
    }

    /**
     * 撤销申请
     */
    @PostMapping("/cancel")
    public Response<String> cancel(@RequestBody Map<String, String> body) {
        String applyId = body.get("applyId");
        authApplyService.cancel(applyId);
        return Response.success("ok");
    }
}
