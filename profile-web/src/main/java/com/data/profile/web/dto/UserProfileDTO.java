package com.data.profile.web.dto;

import com.data.profile.web.dto.GroupDTO;
import com.data.profile.web.vo.UserVO;
import lombok.Data;

import java.util.List;

/**
 * 功能：用户画像DTO - 聚合传输
 * 作者：SmartSi
 * 日期：2026/3/14
 */
@Data
public class UserProfileDTO {
    // 用户基础信息
    private UserVO user;
    // 标签列表（扁平，Service层按类目分组）
    private List<UserLabelDTO> labels;
    // 所属人群
    private List<GroupDTO> groups;
}
