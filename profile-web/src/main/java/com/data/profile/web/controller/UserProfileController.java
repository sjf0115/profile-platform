package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.web.vo.UserProfileRowVO;
import com.data.profile.web.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能：用户画像
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Slf4j
@RestController
@RequestMapping(value = "/insight/user-profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 根据类型和ID随机抽取用户列表
     *
     * @param type  类型: entity / label / group
     * @param id    实体标识ID / 标签ID / 群组ID
     * @param limit 返回数量，默认50
     */
    @GetMapping("/random-users")
    public Response<List<UserProfileRowVO>> getRandomUsers(
            @RequestParam String type,
            @RequestParam String id,
            @RequestParam(defaultValue = "50") int limit) {
        log.info("请求随机抽取用户: type={}, id={}, limit={}", type, id, limit);

        List<UserProfileRowVO> users;
        if ("entity".equals(type)) {
            users = userProfileService.getRandomUsersByEntityIdentifierId(id, limit);
        } else if ("label".equals(type)) {
            users = userProfileService.getRandomUsersByLabelId(id, limit);
        } else if ("group".equals(type)) {
            users = userProfileService.getRandomUsersByGroupId(id, limit);
        } else {
            return Response.error("不支持的类型: " + type, null);
        }

        return Response.success(users);
    }
}
