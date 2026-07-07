package com.data.profile.web.converter;

import java.util.List;

/**
 * 功能：POJO转换器
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/6/28 22:38
 */
public interface BaseConverter<IN, OUT> {
    OUT convert(IN in);
    List<OUT> convertList(List<IN> inList);
}
