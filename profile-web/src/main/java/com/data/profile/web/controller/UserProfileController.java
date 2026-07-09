package com.data.profile.web.controller;

import com.data.profile.web.vo.Response;
import com.data.profile.web.vo.UserProfileRowVO;
import com.data.profile.web.vo.UserProfileVO;
import com.data.profile.web.service.UserProfileService;
import com.data.profile.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // ==================== 用户细查接口 ====================

    /**
     * 获取用户完整画像（聚合接口）
     */
    @GetMapping("/{userId}/profile")
    public Response<UserProfileVO> getProfile(@PathVariable(value = "userId") String userId) {
        log.info("请求查询用户 {} 画像", userId);
        UserProfileVO profile = userProfileService.getUserProfile(userId);
        return Response.success(profile);
    }

    /**
     * 为用户打标签
     */
    @PostMapping("/{userId}/labels")
    public Response<Integer> addLabel(@PathVariable(value = "userId") String userId,
                                      @RequestBody Map<String, String> body) {
        log.info("请求为用户 {} 打标签: {}", userId, JSONUtils.toJsonString(body));
        int result = userProfileService.addLabel(userId, body.get("label_id"), body.get("label_value"));
        return Response.success(result);
    }

    /**
     * 删除用户单个标签
     */
    @DeleteMapping("/{userId}/labels/{labelId}")
    public Response<Integer> deleteLabel(@PathVariable(value = "userId") String userId,
                                         @PathVariable(value = "labelId") String labelId) {
        log.info("请求删除用户 {} 标签 {}", userId, labelId);
        int result = userProfileService.deleteLabel(userId, labelId);
        return Response.success(result);
    }

    /**
     * 更新类目排序（拖拽后调用）
     */
    @PutMapping("/{userId}/categories/sort")
    public Response<Integer> updateCategorySort(@PathVariable(value = "userId") String userId,
                                                @RequestBody List<String> categoryIds) {
        log.info("请求更新用户 {} 类目排序: {}", userId, categoryIds);
        int result = userProfileService.updateCategorySort(userId, categoryIds);
        return Response.success(result);
    }

    /**
     * 删除类目（解绑该类目下所有标签）
     */
    @DeleteMapping("/{userId}/categories/{categoryId}")
    public Response<Integer> deleteCategory(@PathVariable(value = "userId") String userId,
                                            @PathVariable(value = "categoryId") String categoryId) {
        log.info("请求删除用户 {} 类目 {}", userId, categoryId);
        int result = userProfileService.deleteCategory(userId, categoryId);
        return Response.success(result);
    }
}
