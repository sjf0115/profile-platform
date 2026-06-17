package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.web.model.LabelCategory;
import com.data.profile.web.service.LabelCategoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 功能：标签类目
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/labelCategory", produces = MediaType.APPLICATION_JSON_VALUE)
public class LabelCategoryController {
    private static final Gson gson = new GsonBuilder().create();
    @Autowired
    private LabelCategoryService categoryService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody LabelCategory category) {
        log.info("根据类目信息请求查询标签类目: {}", gson.toJson(category));
        List<LabelCategory> categories = categoryService.getList(category);
        return Response.success(categories);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam(name = "category_id") String categoryId) {
        log.info("根据类目ID查询标签类目: {}", categoryId);
        Optional<LabelCategory> optional = categoryService.getDetail(categoryId);
        if (optional.isPresent()) {
            return Response.success(optional.get());
        } else {
            return Response.error("请求的标签类目不存在", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/add")
    public Response add(@RequestParam(name = "category_name") String categoryName, @RequestParam(name = "parent_category_id") String parentCategoryId) {
        log.info("在父类目ID {} 下添加标签类目: {}", parentCategoryId, categoryName);
        int result = categoryService.add(categoryName, parentCategoryId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加标签类目失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/delete")
    public Response delete(@RequestParam(name = "category_id") String categoryId) {
        log.info("删除标签类目: {}", categoryId);
        int result = categoryService.delete(categoryId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除标签类目失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/rename")
    public Response rename(@RequestParam(name = "category_id") String categoryId, @RequestParam(name = "category_name") String categoryName) {
        log.info("对标签类目ID {} 重名为: {}", categoryId, categoryName);
        int result = categoryService.rename(categoryId, categoryName);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("标签类目重命名失败", ResponseCode.ERROR);
        }
    }
}