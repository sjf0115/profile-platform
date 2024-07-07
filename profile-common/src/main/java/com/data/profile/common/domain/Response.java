package com.data.profile.common.domain;

import com.data.profile.common.enums.ResponseCode;
import lombok.Setter;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:32
 */
@Setter
public class Response<T> {
    // 结果码
    private Integer code;
    // 错误信息
    private String message;
    // 返回结果
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setMessage(ResponseCode.SUCCESS.getMessage());
        return response;
    }
}
