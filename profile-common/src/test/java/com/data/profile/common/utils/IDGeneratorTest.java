package com.data.profile.common.utils;

import com.data.profile.common.enums.ModelType;
import org.junit.Test;

/**
 * 功能：
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/17 23:13
 */
public class IDGeneratorTest {
    @Test
    public void getCodeTest() {
        long code = IDGenerator.getInstance().genCode();
        System.out.println(code);
    }

    @Test
    public void generateTest() {
        String code = IDGenerator.getInstance().generate(ModelType.USER);
        System.out.println(code);
    }
}
