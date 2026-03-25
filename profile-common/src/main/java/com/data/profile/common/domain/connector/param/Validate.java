package com.data.profile.common.domain.connector.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * form validate
 */
@JsonDeserialize(builder = Validate.Builder.class)
public class Validate {

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("message")
    private String message;

    @JsonProperty("type")
    private String type;

    @JsonProperty("trigger")
    private String trigger;

    @JsonProperty("min")
    private Double min;

    @JsonProperty("max")
    private Double max;

    private Validate() {

    }

    private Validate(Builder builder) {
        this.required = builder.required;
        this.message = builder.message;
        this.type = builder.type;
        this.trigger = builder.trigger;
        this.min = builder.min;
        this.max = builder.max;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    public static class Builder {
        private boolean required = false;

        private String message;

        private String type = ValueType.STRING.getDescription();

        private String trigger = TriggerType.BLUR.getTriggerType();

        private Double min;

        private Double max;

        public Builder setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setTrigger(String trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder setMin(Double min) {
            this.min = min;
            return this;
        }

        public Builder setMax(Double max) {
            this.max = max;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Validate build() {
            return new Validate(this);
        }
    }

    public boolean isRequired() {
        return required;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getTrigger() {
        return trigger;
    }

    public Double getMin() {
        return min;
    }

    public Double getMax() {
        return max;
    }
}
