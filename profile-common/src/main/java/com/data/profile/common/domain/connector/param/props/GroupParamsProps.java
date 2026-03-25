package com.data.profile.common.domain.connector.param.props;

import com.data.profile.common.domain.connector.param.PluginParams;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * the props field in form-create`s json rule
 */
public class GroupParamsProps {

    private List<PluginParams> rules;

    private int fontSize;

    @JsonProperty("rules")
    public List<PluginParams> getRules() {
        return rules;
    }

    public GroupParamsProps setRules(List<PluginParams> rules) {
        this.rules = rules;
        return this;
    }

    @JsonProperty("fontSize")
    public int getFontSize() {
        return fontSize;
    }

    public GroupParamsProps setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }
}
