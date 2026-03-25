package com.data.profile.common.domain.connector.param;

public enum TriggerType {

    /**
     * blur
     * change
     */
    BLUR("blur"),

    CHANGE("change");

    private final String type;

    TriggerType(String type) {
        this.type = type;
    }

    public String getTriggerType() {
        return this.type;
    }

}
