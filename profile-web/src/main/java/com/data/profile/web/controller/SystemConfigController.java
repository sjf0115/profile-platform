package com.data.profile.web.controller;

import com.data.notification.api.entity.NotificationResult;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.service.NotificationService;
import com.data.profile.web.service.SystemConfigService;
import com.data.profile.web.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/system-config", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取 SMTP 表单 Schema
     */
    @GetMapping("/smtp/form")
    public Response<String> getSmtpForm() {
        String formSchema = systemConfigService.getSmtpConfigForm();
        return Response.success(formSchema);
    }

    /**
     * 获取指定分组配置
     */
    @GetMapping("/group/{group}")
    public Response<Map<String, String>> getConfigByGroup(@PathVariable String group) {
        Map<String, String> config = systemConfigService.getConfigByGroup(group);
        return Response.success(config);
    }

    /**
     * 保存指定分组配置
     */
    @RequiresPermission(code = "system:config:edit", name = "系统配置-编辑")
    @PostMapping("/group/{group}")
    public Response<String> saveConfig(@PathVariable String group, @RequestBody Map<String, String> configMap) {
        log.info("保存系统配置: group={}, keys={}", group, configMap.keySet());
        systemConfigService.saveConfig(group, configMap);
        return Response.success("保存成功");
    }

    /**
     * 测试 SMTP 连通性
     */
    @RequiresPermission(code = "system:config:edit", name = "系统配置-编辑")
    @PostMapping("/smtp/test")
    public Response<String> testSmtp(@RequestBody(required = false) Map<String, String> params) {
        String testReceiver = params != null ? params.get("testReceiver") : null;
        try {
            NotificationResult result = notificationService.testSmtp(testReceiver);
            if (result.getStatus()) {
                return Response.success("测试邮件发送成功");
            } else {
                String errorMsg = result.getRecords() != null && !result.getRecords().isEmpty()
                        ? result.getRecords().get(0).getMessage()
                        : "测试邮件发送失败";
                return Response.error(errorMsg, ResponseCode.ERROR);
            }
        } catch (Exception e) {
            log.error("SMTP 测试失败", e);
            return Response.error("SMTP 测试失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }
}
