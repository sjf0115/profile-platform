package com.data.profile.web.controller;

import com.data.profile.common.enums.TriggerMode;
import com.data.profile.web.annotation.RequiresPermission;
import com.data.profile.web.converter.ExportConverter;
import com.data.profile.web.dto.ExportDTO;
import com.data.profile.web.dto.ExportParam;
import com.data.profile.web.dto.ExportRequest;
import com.data.profile.web.model.Export;
import com.data.profile.web.model.TaskInstance;
import com.data.profile.web.service.TaskExecutionService;
import com.data.profile.web.service.ExportService;
import com.data.profile.web.vo.ExportVO;
import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：投递
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/12/29 11:46
 */
@Slf4j
@RestController
@RequestMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
public class ExportController {
    @Autowired
    private ExportService exportService;
    @Autowired
    private TaskExecutionService taskExecutionService;

    /**
     * 投递列表
     */
    @PostMapping(value = "/list")
    public Response<List<ExportVO>> getList(@RequestBody ExportParam param) {
        log.info("请求查询投递列表：{}", JSONUtils.toJsonString(param));
        Export export = ExportConverter.param2do(param);
        List<ExportDTO> dtos = exportService.getList(export);
        return Response.success(ExportConverter.dto2voList(dtos));
    }

    /**
     * 投递详情
     */
    @GetMapping(value = "/{exportId}/detail")
    public Response<ExportVO> getDetail(@PathVariable(value = "exportId") String exportId) {
        log.info("请求查询投递 {} 详细信息", exportId);
        Optional<ExportDTO> opt = exportService.getDetail(exportId);
        if (!opt.isPresent()) {
            return Response.error("投递不存在", ResponseCode.ERROR);
        }
        return Response.success(ExportConverter.dto2vo(opt.get()));
    }

    /**
     * 创建投递
     */
    @RequiresPermission(code = "export:create", name = "投递-创建")
    @PostMapping
    public Response<ExportVO> create(@RequestBody ExportRequest request) {
        log.info("请求创建投递：{}", JSONUtils.toJsonString(request));
        try {
            ExportDTO dto = exportService.create(request);
            return Response.success(ExportConverter.dto2vo(dto));
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新投递
     */
    @RequiresPermission(code = "export:edit", name = "投递-编辑")
    @PutMapping("/{exportId}")
    public Response<Integer> update(@PathVariable(value = "exportId") String exportId, @RequestBody ExportRequest request) {
        log.info("请求更新投递 {}：{}", exportId, JSONUtils.toJsonString(request));
        try {
            int result = exportService.update(exportId, request);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("修改投递失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 删除投递
     */
    @RequiresPermission(code = "export:delete", name = "投递-删除")
    @DeleteMapping("/{exportId}")
    public Response<Integer> delete(@PathVariable(value = "exportId") String exportId) {
        log.info("请求删除投递：{}", exportId);
        try {
            int result = exportService.delete(exportId);
            if (result > 0) {
                return Response.success(result);
            } else {
                return Response.error("删除投递失败", ResponseCode.ERROR);
            }
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 更新投递状态（启用/停用）
     */
    @RequiresPermission(code = "export:edit", name = "投递-编辑")
    @PutMapping("/{exportId}/status")
    public Response<Integer> updateStatus(@PathVariable(value = "exportId") String exportId, @RequestParam Integer status) {
        log.info("请求更新投递 {} 状态为：{}", exportId, status);
        try {
            int result = exportService.updateStatus(exportId, status);
            return Response.success(result);
        } catch (RuntimeException e) {
            return Response.error(e.getMessage(), ResponseCode.ERROR);
        }
    }

    /**
     * 立即执行投递
     */
    @RequiresPermission(code = "export:execute", name = "投递-执行")
    @PostMapping("/{exportId}/execute")
    public Response<String> execute(@PathVariable(value = "exportId") String exportId) {
        log.info("请求手动立即执行投递：{}", exportId);
        try {
            TaskInstance instance = taskExecutionService.executeByRelatedId(exportId, TriggerMode.MANUAL);
            return Response.success(instance.getInstanceId());
        } catch (Exception e) {
            log.error("手动立即执行投递 [{}] 圈选失败", exportId, e);
            return Response.error("投递执行失败: " + e.getMessage(), ResponseCode.ERROR);
        }
    }
}
