package com.data.engine.plugin.datax.helper;

import com.data.engine.plugin.datax.constants.DataXConstant;
import com.data.engine.plugin.datax.datasource.DataXDataSource;
import com.data.engine.plugin.datax.plugin.DataXReaderBuilder;
import com.data.engine.plugin.datax.plugin.DataXWriterBuilder;
import com.data.engine.plugin.datax.plugin.bean.ReaderContext;
import com.data.engine.plugin.datax.plugin.bean.WriterContext;
import com.data.spi.PluginLoader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 构造 DataX 任务 JSON
 * <pre>
 *   {
 *     "job": {
 *       "setting": { "speed": {...}, "errorLimit": {...} },
 *       "content": [
 *         {
 *           "reader": { "name": "mysqlreader", "parameter": {...} },
 *           "writer": { "name": "clickhousewriter", "parameter": {...} }
 *         }
 *       ]
 *     }
 *   }
 * </pre>
 */
@Slf4j
public final class DataXJsonHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private DataXJsonHelper() {
    }

    /**
     * 构建同步作业 Job JSON 字符串
     */
    public static String buildJobJson(DataXJobBuildRequest request) {
        Map<String, Object> job = buildJob(request);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(DataXConstant.JOB, job);
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialize DataX job json failed", e);
        }
    }

    /**
     * 构建同步作业 Job
     */
    public static Map<String, Object> buildJob(DataXJobBuildRequest request) {
        Objects.requireNonNull(request, "DataXJobBuildRequest == null");
        Map<String, Object> job = new LinkedHashMap<>();
        // setting
        job.put(DataXConstant.SETTING, buildSetting(request));
        // Content
        job.put(DataXConstant.CONTENT, buildContent(request));
        return job;
    }

    /**
     * 构建同步作业 setting 部分
     */
    public static Map<String, Object> buildSetting(DataXJobBuildRequest request) {
        Map<String, Object> setting = new LinkedHashMap<>();
        // 同步速度
        Map<String, Object> speed = new LinkedHashMap<>();
        if (request.getSettingSpeedChannel() != null) {
            speed.put(DataXConstant.CHANNEL, request.getSettingSpeedChannel());
        } else {
            speed.put(DataXConstant.CHANNEL, 1);
        }
        if (request.getSettingSpeedByte() != null) {
            speed.put(DataXConstant.BYTE, request.getSettingSpeedByte());
        }
        if (request.getSettingSpeedRecord() != null) {
            speed.put(DataXConstant.RECORD, request.getSettingSpeedRecord());
        }
        setting.put(DataXConstant.SPEED, speed);

        // 脏数据阈值
        Map<String, Object> errorLimit = new LinkedHashMap<>();
        if (request.getSettingErrorRecord() != null) {
            errorLimit.put(DataXConstant.RECORD, request.getSettingErrorRecord());
        } else {
            errorLimit.put(DataXConstant.RECORD, 0);
        }
        if (request.getSettingErrorPercentage() != null) {
            errorLimit.put(DataXConstant.PERCENTAGE, request.getSettingErrorPercentage());
        }
        setting.put(DataXConstant.ERROR_LIMIT, errorLimit);

        return setting;
    }

    /**
     * 构建同步作业 Content 部分
     */
    public static List<Map<String, Object>> buildContent(DataXJobBuildRequest request) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put(DataXConstant.READER, buildReader(request.getReaderSource(), request.getReaderContext()));
        entry.put(DataXConstant.WRITER, buildWriter(request.getWriterSource(), request.getWriterContext()));
        List<Map<String, Object>> content = new ArrayList<>(1);
        content.add(entry);
        return content;
    }

    /**
     * 构建同步作业 Content Reader 部分
     */
    public static Map<String, Object> buildReader(DataXDataSource source, ReaderContext ctx) {
        Objects.requireNonNull(source, "readerSource == null");
        Objects.requireNonNull(ctx, "readerContext == null");
        DataXReaderBuilder builder = PluginLoader.getPluginLoader(DataXReaderBuilder.class).getOrCreatePlugin(source.getCategory());
        if (builder == null) {
            log.info("没有 [{}] 类型的 DataXReader 构造器, 请先注册插件", source.getCategory());
            throw new IllegalStateException("没有[ " + source.getCategory() + "] 类型的 DataXReader 构造器, 请先注册插件");
        }
        Map<String, Object> reader = new LinkedHashMap<>();
        reader.put(DataXConstant.NAME, builder.getPluginName());
        reader.put(DataXConstant.PARAMETER, builder.build(source, ctx));
        return reader;
    }

    /**
     * 构建同步作业 Content Writer 部分
     */
    public static Map<String, Object> buildWriter(DataXDataSource source, WriterContext ctx) {
        Objects.requireNonNull(source, "writerSource == null");
        Objects.requireNonNull(ctx, "writerContext == null");
        DataXWriterBuilder builder = PluginLoader.getPluginLoader(DataXWriterBuilder.class).getOrCreatePlugin(source.getCategory());
        if (builder == null) {
            log.info("没有 [{}] 类型的 DataXWriter 构造器, 请先注册插件", source.getCategory());
            throw new IllegalStateException("没有[ " + source.getCategory() + "] 类型的 DataXWriter 构造器, 请先注册插件");
        }
        Map<String, Object> writer = new LinkedHashMap<>();
        writer.put(DataXConstant.NAME, builder.getPluginName());
        writer.put(DataXConstant.PARAMETER, builder.build(source, ctx));
        return writer;
    }

    /** 共享 ObjectMapper（也用于 Executor 反序列化 ExecutorRequest.config） */
    public static ObjectMapper sharedMapper() {
        return MAPPER;
    }
}
