package com.data.engine.plugin;

import com.data.engine.api.factory.DiEngineFactory;
import com.data.engine.api.DiRequestBuilder;
import com.data.engine.plugin.builder.SeaTunnelRequestBuilder;
import com.data.engine.plugin.executor.SeaTunnelDiEngineExecutor;
import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;

/**
 * 功能：SeaTunnelEngineFactory
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 13:05
 */
public class SeaTunnelEngineFactory implements DiEngineFactory {
    @Override
    public void setConfig(Config config) {

    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }

    @Override
    public String getCategory() {
        return "SeaTunnel";
    }

    @Override
    public SeaTunnelDiEngineExecutor getExecutor() {
        return new SeaTunnelDiEngineExecutor();
    }

    @Override
    public DiRequestBuilder getRequestBuilder() {
        return new SeaTunnelRequestBuilder();
    }
}
