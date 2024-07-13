package com.data.profile.common.utils;

import com.data.profile.common.enums.ModelType;

import java.security.SecureRandom;
import java.util.*;

/**
 * 功能：ID生成器
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/11 22:13
 */
public class IDGenerator {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final int STRING_LENGTH = 8; // 应为偶数

    public static String generate(ModelType code) {
        // 确保字符串长度为偶数以平均分配
        if (STRING_LENGTH % 2 != 0) {
            throw new IllegalArgumentException("String length must be even.");
        }

        StringBuilder stringBuilder = new StringBuilder(STRING_LENGTH);
        Random random = new SecureRandom();

        // 从每个池中选取字符填充一半长度
        for (int i = 0; i < STRING_LENGTH / 2; i++) {
            stringBuilder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
            stringBuilder.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }

        // 打乱字符串以使数字和字母随机分布
        List<Character> chars = new ArrayList<>();
        for (char c : stringBuilder.toString().toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);

        // 重构字符串
        stringBuilder.setLength(0);
        for (char c : chars) {
            stringBuilder.append(c);
        }

        return code.getCode() + stringBuilder.toString().toUpperCase();
    }

    public static void main(String[] args) {
        String name = IDGenerator.generate(ModelType.USER);
        System.out.println(name);
    }
}