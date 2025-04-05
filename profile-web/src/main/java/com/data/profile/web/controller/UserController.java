package com.data.profile.web.controller;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.model.User;
import com.data.profile.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping(value = "/list")
    public Response getList(@RequestBody User user) {
        List<User> users = userService.getList(user);
        return Response.success(users);
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String userId) {
        Optional<User> userOptional = userService.getDetail(userId);
        if (userOptional.isPresent()) {
            return Response.success(userOptional.get());
        }
        return Response.error("用户不存在", ResponseCode.ERROR);
    }

    @PostMapping(value = "/login")
    public Response login(@RequestParam String userId, String password) {
        if (userService.login(userId, password)) {
            return Response.success(true);
        }
        return Response.error("登录失败", ResponseCode.ERROR);
    }

    @PostMapping(value = "/save")
    public Response save(@RequestBody User user) {
        int result = userService.save(user);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("保存用户失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public Response delete(@RequestParam String userId) {
        int result = userService.delete(userId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除用户失败", ResponseCode.ERROR);
        }
    }
}