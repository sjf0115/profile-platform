package com.data.profile.web.vo;

import com.data.profile.web.model.Role;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 功能：用户视图对象
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
@Data
public class UserVO {
    private Long id;
    private Integer status;
    private String userId;
    private String userName;
    private String email;
    private Integer sourceType;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
    /**
     * 用户角色列表，包含完整角色信息
     */
    private List<Role> roles;
}
