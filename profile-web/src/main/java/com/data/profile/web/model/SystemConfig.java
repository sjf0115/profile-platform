package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    private Long id;
    // 配置分组: smtp/platform/appearance
    private String configGroup;
    // 配置键
    private String configKey;
    // 配置值
    private String configValue;
    // 配置说明
    private String configDesc;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}
