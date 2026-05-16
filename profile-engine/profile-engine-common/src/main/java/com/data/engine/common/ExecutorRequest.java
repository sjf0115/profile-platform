package com.data.engine.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 功能：ExecutorRequest
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 20:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecutorRequest {
    private String jobId;
    private String configPath;
    private Map<String, Object> config;
}
