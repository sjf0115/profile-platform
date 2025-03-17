package com.data.profile.common.utils;

import com.data.profile.common.enums.ModelType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * 功能：雪花算法变体 - 生成ID
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2025/3/17 23:10
 */
public class IDGenerator {
    // start timestamp
    private static final long START_TIMESTAMP = 1609430400000L; // 2021-01-01 00:00:00
    // Each machine generates 32 in the same millisecond
    private static final long LOW_DIGIT_BIT = 5L;
    private static final long MIDDLE_BIT = 2L;
    private static final long MAX_LOW_DIGIT = ~(-1L << LOW_DIGIT_BIT);
    // The displacement to the left
    private static final long MIDDLE_LEFT = LOW_DIGIT_BIT;
    private static final long HIGH_DIGIT_LEFT = LOW_DIGIT_BIT + MIDDLE_BIT;
    private final long machineHash;
    private long lowDigit = 0L;
    private long recordMillisecond = -1L;

    private static final long SYSTEM_TIMESTAMP = System.currentTimeMillis();
    private static final long SYSTEM_NANOTIME = System.nanoTime();
    private static final int I = 1000000;

    private IDGenerator() throws CodeGenerateException {
        try {
            this.machineHash =
                    Math.abs(Objects.hash(InetAddress.getLocalHost().getHostName()))
                            % (2 << (MIDDLE_BIT - 1));
        } catch (UnknownHostException e) {
            throw new CodeGenerateException(e.getMessage());
        }
    }

    private static IDGenerator INSTANCE = null;

    public static synchronized IDGenerator getInstance() throws CodeGenerateException {
        if (INSTANCE == null) {
            INSTANCE = new IDGenerator();
        }
        return INSTANCE;
    }

    public synchronized long genCode() throws CodeGenerateException {
        long nowtMillisecond = systemMillisecond();
        if (nowtMillisecond < recordMillisecond) {
            throw new CodeGenerateException("New code exception because time is set back.");
        }
        if (nowtMillisecond == recordMillisecond) {
            lowDigit = (lowDigit + 1) & MAX_LOW_DIGIT;
            if (lowDigit == 0L) {
                while (nowtMillisecond <= recordMillisecond) {
                    nowtMillisecond = systemMillisecond();
                }
            }
        } else {
            lowDigit = 0L;
        }
        recordMillisecond = nowtMillisecond;
        return (nowtMillisecond - START_TIMESTAMP) << HIGH_DIGIT_LEFT
                | machineHash << MIDDLE_LEFT
                | lowDigit;
    }

    public String generate(ModelType code) {
        return code.getCode() + genCode();
    }

    private long systemMillisecond() {
        return SYSTEM_TIMESTAMP + (System.nanoTime() - SYSTEM_NANOTIME) / I;
    }

    public static class CodeGenerateException extends RuntimeException {
        public CodeGenerateException(String message) {
            super(message);
        }
    }
}
