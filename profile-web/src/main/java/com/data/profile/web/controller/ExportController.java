package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.Export;
import com.data.profile.web.service.ExportService;
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

    @GetMapping(value = "/list")
    public Response getList(@RequestBody Export export) {
        List<Export> exports = exportService.getList(export);
        return Response.success(exports);
    }

    @GetMapping(value = "/name")
    public Response getByName(@RequestParam String exportName) {
        List<Export> exports = exportService.getByName(exportName);
        return Response.success(exports);
    }

    @GetMapping(value = "/keyword")
    public Response getByKeyword(@RequestParam String keyword) {
        List<Export> exports = exportService.getByKeyword(keyword);
        return Response.success(exports);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String exportId) {
        Optional<Export> optional = exportService.getDetail(exportId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的投递不存在", ResponseCode.ERROR);
        }
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody Export export) {
        int result = exportService.save(export);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("创建投递任务失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam String exportId) {
        int result = exportService.delete(exportId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除投递任务失败", ResponseCode.ERROR);
        }
    }
}
