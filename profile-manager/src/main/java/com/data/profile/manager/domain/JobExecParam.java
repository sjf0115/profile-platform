package com.data.profile.manager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/17 22:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecParam {
    // job config placeholder name -> value
    private Map<String, String> placeholderValues;
    // task name -> new datasource id
    private Map<String, String> datasource;
}
