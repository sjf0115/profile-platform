package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.domain.request.UserLoginRequest;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.model.User;
import com.data.profile.service.UserService;
import com.data.profile.web.dto.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 功能：用户
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Slf4j
@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping(value = "/list")
    public Response getList(@RequestBody User user) {
        log.info("请求查询用户：{}", JSONUtils.toJsonString(user));
        List<User> users = userService.getList(user);
        return Response.success(users);
    }

    @GetMapping(value = "/{userId}/detail")
    public Response getDetail(@PathVariable(value = "userId") String userId) {
        log.info("请求查询用户 {} 详细信息", userId);
        Optional<User> userOptional = userService.getDetail(userId);
        if (userOptional.isPresent()) {
            return Response.success(userOptional.get());
        }
        return Response.error("用户不存在", ResponseCode.ERROR);
    }

    @PostMapping
    public Response create(@RequestBody UserRequest request) {
        log.info("请求创建用户：{}", JSONUtils.toJsonString(request));
        // 构建 User 对象
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        
        int result = userService.create(user, request.getRoles());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加用户失败", ResponseCode.ERROR);
        }
    }

    @PutMapping("/{userId}")
    public Response update(@PathVariable(value = "userId") String userId, @RequestBody UserRequest request) {
        log.info("请求更新用户：{}", JSONUtils.toJsonString(request));
        
        // 构建 User 对象
        User user = new User();
        user.setUserId(userId);
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        
        int result = userService.update(user, request.getRoles());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改用户失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/{userId}")
    public Response delete(@PathVariable(value = "userId") String userId) {
        log.info("请求删除用户：{}", userId);
        int result = userService.delete(userId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除用户失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/overview")
    public Response getOverview() {
        log.info("请求查询用户概览统计");
        Map<String, Object> overview = new HashMap<>();
        overview.put("total_count", 0);
        overview.put("admin_count", 0);
        overview.put("member_count", 0);
        overview.put("no_permission_count", 0);
        return Response.success(overview);
    }

    @PostMapping(value = "/login")
    public Response login(@RequestBody UserLoginRequest userLoinRequest, @RequestHeader(value = "auth-Type", required = false) String authType) {
        User user = userService.login(userLoinRequest, authType);
        return Response.success(user);
    }

    @PatchMapping("/logout")
    public Response logout() {
        return Response.success(null);
    }
}