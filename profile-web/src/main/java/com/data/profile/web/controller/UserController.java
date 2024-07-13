package com.data.profile.web.controller;

import com.data.profile.common.domain.RequestContext;
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

import java.util.Objects;
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

    @PostMapping(value = "/save")
    public Response save(@RequestBody User user) {
        int result = userService.save(user);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("保存用户失败", ResponseCode.USER_NO_ERROR);
        }
    }

    @GetMapping(value = "/detail")
    public Response getDetail(@RequestParam String userId) {
        Optional<User> userOptional = userService.getDetail(userId);
        if (userOptional.isPresent()) {
            return Response.success(userOptional.get());
        } else {
            return Response.error("请求用户不存在", ResponseCode.USER_NO_ERROR);
        }
    }

    @GetMapping(value = "/list")
    public Response getList(@RequestParam (value = "search", required = false) String name,
                            @RequestParam (value = "userId", required = false) String userId,
                            @RequestParam (defaultValue = "1") Integer currentPage,
                            @RequestParam (defaultValue = "10")Integer pageSize) {
        return null;
    }

    @PostMapping(value = "/login")
    public Response login(@RequestParam User loginUser) {
        Optional<User> userOptional = userService.getDetail(loginUser.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 验证密码
            if (Objects.equals(loginUser.getUserName(), user.getUserName())) {
                RequestContext.setUser(loginUser);
                return Response.success(true);
            } else {
                return Response.error("密码错误", ResponseCode.USER_NO_ERROR);
            }
        } else {
            return Response.error("登录用户不存在", ResponseCode.USER_NO_ERROR);
        }
    }
}