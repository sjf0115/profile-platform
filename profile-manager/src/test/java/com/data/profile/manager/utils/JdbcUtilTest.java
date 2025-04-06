package com.data.profile.manager.utils;

import com.data.profile.manager.domain.ConnectionParam;
import org.junit.Test;

import java.net.URISyntaxException;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/4/6 16:58
 */
public class JdbcUtilTest {
    @Test
    public void testBuildUrl() throws URISyntaxException {
        ConnectionParam param = new ConnectionParam();
        param.setProtocol("jdbc://mysql");
        param.setHost("localhost");
        param.setPort(3306);
        param.setDatabase("test");
        param.setUserName("root");
        param.setPassword("root");
        String url = JdbcUtil.buildUrl(param);
        System.out.println(url);
    }
}
