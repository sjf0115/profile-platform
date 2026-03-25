package com.data.profile.common.domain.connector.param;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParamsOptions {

    private String label;

    private Object value;

    /**
     * is can be select
     */
    private boolean disabled;

    public ParamsOptions(){}

    public ParamsOptions(String label, Object value, boolean disabled) {
        this.label = label;
        this.value = value;
        this.disabled = disabled;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @JsonProperty("disabled")
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
