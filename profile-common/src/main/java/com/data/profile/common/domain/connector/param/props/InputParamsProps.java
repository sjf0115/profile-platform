package com.data.profile.common.domain.connector.param.props;

import com.data.profile.common.domain.connector.param.PropsType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InputParamsProps {

    private PropsType propsType;

    private String placeholder;

    private int rows;

    private boolean disabled;

    private String size;

    @JsonProperty("size")
    public String getSize() {
        return size;
    }

    public InputParamsProps setSize(String size) {
        this.size = size;
        return this;
    }

    @JsonProperty("type")
    public PropsType getPropsType() {
        return propsType;
    }

    public InputParamsProps setPropsType(PropsType propsType) {
        this.propsType = propsType;
        return this;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public InputParamsProps setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @JsonProperty("rows")
    public int getRows() {
        return rows;
    }

    public InputParamsProps setRows(int rows) {
        this.rows = rows;
        return this;
    }

    @JsonProperty("disabled")
    public boolean getDisabled() {
        return disabled;
    }

    public InputParamsProps setDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }
}
