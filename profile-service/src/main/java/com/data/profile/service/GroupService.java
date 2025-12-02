package com.data.profile.service;

import com.data.profile.common.domain.Response;
import com.data.profile.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 功能：群组服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class GroupService {
    private static Logger LOG = LoggerFactory.getLogger(GroupService.class);

    /*public Response upload(MultipartFile file) {
        if (file.isEmpty()) {
            return Response.error("请选择一个文件上传", ResponseCode.ERROR);
        }
        return Response.success(null);
    }*/
}