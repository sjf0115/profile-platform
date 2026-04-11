package com.data.profile.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabelCategory {
    private Long id;
    // 标签类目状态:启用-1,禁用-2
    private Integer status;
    // 是否是默认兜底类目:是-1,否-2
    private Integer isDefault;
    // 标签类目ID
    private String categoryId;
    // 标签类目名称
    private String categoryName;
    // 标签类目层级
    private Integer categoryLevel;
    // 父标签类目ID
    private String parentCategoryId;
    // 标签类目同级展示序列, 从1开始
    private Integer categorySeq;
    // 创建方式: 1-系统内置,2-自定义
    private Integer sourceType;
    // 创建者
    private String creator;
    // 修改者
    private String modifier;
    // 创建时间
    private Date gmtCreate;
    // 修改时间
    private Date gmtModified;
}