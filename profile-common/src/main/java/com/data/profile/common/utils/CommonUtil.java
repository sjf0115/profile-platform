package com.data.profile.common.utils;

/**
 * 功能：常用工具
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/9/5 22:43
 */
public class CommonUtil {
    public static long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    public static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
