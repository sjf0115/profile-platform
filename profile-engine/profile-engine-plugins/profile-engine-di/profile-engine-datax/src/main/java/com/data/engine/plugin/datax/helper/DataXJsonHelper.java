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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 构造 DataX 任务 JSON。结构参考 datax-web 的 DataxJsonHelper，但 Reader/Writer 走 SPI 一源一插件。
 *
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
public final class DataXJsonHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private DataXJsonHelper() {
    }

    /**
     * 构造 DataX JSON 字符串。
     */
    public static String buildJobJson(DataXJobBuildRequest req) {
        Map<String, Object> job = buildJob(req);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(DataXConstant.JOB, job);
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Serialize DataX job json failed", e);
        }
    }

    static Map<String, Object> buildJob(DataXJobBuildRequest req) {
        Objects.requireNonNull(req, "DataXJobBuildRequest == null");
        Map<String, Object> job = new LinkedHashMap<>();
        job.put(DataXConstant.SETTING, buildSetting(req));
        job.put(DataXConstant.CONTENT, buildContent(req));
        return job;
    }

    static Map<String, Object> buildSetting(DataXJobBuildRequest req) {
        Map<String, Object> setting = new LinkedHashMap<>();

        Map<String, Object> speed = new LinkedHashMap<>();
        if (req.getSettingSpeedChannel() != null) {
            speed.put(DataXConstant.CHANNEL, req.getSettingSpeedChannel());
        } else {
            speed.put(DataXConstant.CHANNEL, 1);
        }
        if (req.getSettingSpeedByte() != null) {
            speed.put(DataXConstant.BYTE, req.getSettingSpeedByte());
        }
        if (req.getSettingSpeedRecord() != null) {
            speed.put(DataXConstant.RECORD, req.getSettingSpeedRecord());
        }
        setting.put(DataXConstant.SPEED, speed);

        Map<String, Object> errorLimit = new LinkedHashMap<>();
        errorLimit.put(DataXConstant.RECORD,
                req.getSettingErrorRecord() != null ? req.getSettingErrorRecord() : 0);
        if (req.getSettingErrorPercentage() != null) {
            errorLimit.put(DataXConstant.PERCENTAGE, req.getSettingErrorPercentage());
        }
        setting.put(DataXConstant.ERROR_LIMIT, errorLimit);

        return setting;
    }

    static List<Map<String, Object>> buildContent(DataXJobBuildRequest req) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put(DataXConstant.READER, buildReader(req.getReaderSource(), req.getReaderContext()));
        entry.put(DataXConstant.WRITER, buildWriter(req.getWriterSource(), req.getWriterContext()));
        List<Map<String, Object>> content = new ArrayList<>(1);
        content.add(entry);
        return content;
    }

    static Map<String, Object> buildReader(DataXDataSource source, ReaderContext ctx) {
        Objects.requireNonNull(source, "readerSource == null");
        Objects.requireNonNull(ctx, "readerContext == null");
        DataXReaderBuilder builder = PluginLoader.getPluginLoader(DataXReaderBuilder.class)
                .getOrCreatePlugin(source.getCategory());
        if (builder == null) {
            throw new IllegalStateException("No DataXReaderBuilder for category=" + source.getCategory()
                    + ", please register it in META-INF/plugins/com.data.engine.plugin.datax.plugin.DataXReaderBuilder");
        }
        Map<String, Object> reader = new LinkedHashMap<>();
        reader.put(DataXConstant.NAME, builder.getPluginName());
        reader.put(DataXConstant.PARAMETER, builder.build(source, ctx));
        return reader;
    }

    static Map<String, Object> buildWriter(DataXDataSource source, WriterContext ctx) {
        Objects.requireNonNull(source, "writerSource == null");
        Objects.requireNonNull(ctx, "writerContext == null");
        DataXWriterBuilder builder = PluginLoader.getPluginLoader(DataXWriterBuilder.class)
                .getOrCreatePlugin(source.getCategory());
        if (builder == null) {
            throw new IllegalStateException("No DataXWriterBuilder for category=" + source.getCategory()
                    + ", please register it in META-INF/plugins/com.data.engine.plugin.datax.plugin.DataXWriterBuilder");
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
