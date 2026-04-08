package com.data.engine.api;

import com.data.profile.common.config.CheckResult;
import com.data.profile.common.config.Config;

import java.io.Serializable;

public interface Plugin extends Serializable {

    void setConfig(Config config);

    Config getConfig();

    CheckResult checkConfig();
}
