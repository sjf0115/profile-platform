package com.data.engine.plugin;

import com.data.engine.api.EngineExecutor;
import com.data.engine.api.EngineFactory;
import com.data.engine.api.RuntimeEnvironment;
import com.data.engine.plugin.executor.ClickHouseEngineExecutor;
import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;

/**
 * 功能：ClickHouseEngineFactory
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2026/4/12 13:05
 */
public class ClickHouseEngineFactory implements EngineFactory {
    @Override
    public void prepare(RuntimeEnvironment env) throws Exception {

    }

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
    public EngineExecutor getExecutor() {
        return new ClickHouseEngineExecutor();
    }
}
