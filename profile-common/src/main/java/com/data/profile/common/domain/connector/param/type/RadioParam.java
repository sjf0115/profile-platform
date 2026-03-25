package com.data.profile.common.domain.connector.param.type;

import com.data.profile.common.domain.connector.param.FormType;
import com.data.profile.common.domain.connector.param.ParamsOptions;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.RadioParamProps;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * front-end radio select component
 */
public class RadioParam extends PluginParams {

    @JsonProperty("options")
    private List<ParamsOptions> options;

    private RadioParamProps props;

    private RadioParam(Builder builder) {
        super(builder);
        this.options = builder.options;
    }

    public static Builder newBuilder(String name, String title) {
        return new Builder(name, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String name, String title) {
            super(name, FormType.RADIO, title);
        }

        private List<ParamsOptions> options;

        private RadioParamProps props;

        public Builder setOptions(List<ParamsOptions> options) {
            this.options = options;
            return this;
        }

        public Builder addParamsOptions(ParamsOptions paramsOptions) {
            if (this.options == null) {
                this.options = new LinkedList<>();
            }

            this.options.add(paramsOptions);
            return this;
        }

        public Builder setProps(RadioParamProps props) {
            this.props = props;
            return this;
        }

        public Builder setName(String field) {
            this.field = field;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }



        public Builder setValue(Object value) {
            this.value = value;
            return this;
        }

        public Builder setValidateList(List<Validate> validateList) {
            this.validateList = validateList;
            return this;
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new LinkedList<>();
            }
            this.validateList.add(validate);
            return this;
        }


        @Override
        public RadioParam build() {
            return new RadioParam(this);
        }
    }

    public List<ParamsOptions> getOptions() {
        return options;
    }

    @Override
    public RadioParamProps getProps() {
        return props;
    }
}
