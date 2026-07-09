package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.web.vo.UserLoginVO;
import com.data.profile.web.dto.UserDTO;
import com.data.profile.web.dto.UserLoginDTO;
import com.data.profile.web.dto.UserLoginRequest;
import com.data.profile.common.enums.ResponseCode;
import com.data.profile.common.utils.JSONUtils;
import com.data.profile.web.dto.UserParam;
import com.data.profile.web.dto.UserRequest;
import com.data.profile.web.model.User;
import com.data.profile.web.converter.UserConverter;
import com.data.profile.web.dto.UserOverviewDTO;
import com.data.profile.web.vo.UserOverviewVO;
import com.data.profile.web.vo.UserVO;
import com.data.profile.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @Autowired
    private UserService userService;

    @PostMapping(value = "/list")
    public Response<List<UserVO>> getList(@RequestBody UserParam param) {
        log.info("请求查询用户：{}", JSONUtils.toJsonString(param));
        User user = UserConverter.param2do(param);
        List<UserDTO> dtos = userService.getList(user);
        return Response.success(UserConverter.dto2voList(dtos));
    }

    @GetMapping(value = "/{userId}/detail")
    public Response<UserVO> getDetail(@PathVariable(value = "userId") String userId) {
        log.info("请求查询用户 {} 详细信息", userId);
        Optional<UserDTO> opt = userService.getDetail(userId);
        if (!opt.isPresent()) {
            return Response.error("用户不存在", ResponseCode.ERROR);
        }
        return Response.success(UserConverter.dto2vo(opt.get()));
    }

    @PostMapping
    public Response<Integer> create(@RequestBody UserRequest request) {
        log.info("请求创建用户：{}", JSONUtils.toJsonString(request));
        int result = userService.create(request);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("添加用户失败", ResponseCode.ERROR);
        }
    }

    @PutMapping("/{userId}")
    public Response<Integer> update(@PathVariable(value = "userId") String userId, @RequestBody UserRequest request) {
        log.info("请求更新用户：{}", JSONUtils.toJsonString(request));
        User user = UserConverter.request2do(request);
        user.setUserId(userId);

        int result = userService.update(user, request.getRoles());
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("修改用户失败", ResponseCode.ERROR);
        }
    }

    @DeleteMapping(value = "/{userId}")
    public Response<Integer> delete(@PathVariable(value = "userId") String userId) {
        log.info("请求删除用户：{}", userId);
        int result = userService.delete(userId);
        if (result > 0) {
            return Response.success(result);
        } else {
            return Response.error("删除用户失败", ResponseCode.ERROR);
        }
    }

    @GetMapping(value = "/overview")
    public Response<UserOverviewVO> getOverview() {
        log.info("请求查询用户概览统计");
        UserOverviewDTO dto = userService.getOverview();
        UserOverviewVO vo = UserOverviewVO.builder()
                .totalCount(dto.getTotalCount())
                .adminCount(dto.getAdminCount())
                .memberCount(dto.getMemberCount())
                .noPermissionCount(dto.getNoPermissionCount())
                .build();
        return Response.success(vo);
    }

    @PostMapping(value = "/login")
    public Response<UserLoginVO> login(@RequestBody UserLoginRequest userLoinRequest,
                                       @RequestHeader(value = "auth-type", required = false) String authType,
                                       HttpServletRequest request) {
        UserLoginDTO loginDTO = userService.login(userLoinRequest, authType, request);
        UserLoginVO loginVO = UserConverter.convertLogin(loginDTO);
        return Response.success(loginVO);
    }

    @PatchMapping("/logout")
    public Response<Void> logout() {
        userService.logout();
        return Response.success(null);
    }
}
