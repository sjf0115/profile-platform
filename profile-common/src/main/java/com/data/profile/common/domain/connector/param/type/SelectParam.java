package com.data.profile.common.domain.connector.param.type;

import com.data.profile.common.domain.connector.param.FormType;
import com.data.profile.common.domain.connector.param.ParamsOptions;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.SelectParamsProps;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SelectParam extends PluginParams {

    @JsonProperty("options")
    private List<ParamsOptions> paramsOptionsList;

    private SelectParam(Builder builder) {
        super(builder);
        this.paramsOptionsList = builder.paramsOptionsList;
    }

    public static Builder newBuilder(String field, String title) {
        return new Builder(field, title);
    }

    public static class Builder extends PluginParams.Builder {

        private List<ParamsOptions> paramsOptionsList;

        public Builder(String field, String title) {
            super(field, FormType.SELECT, title);
        }

        public Builder setParamsOptionsList(List<ParamsOptions> paramsOptionsList) {
            this.paramsOptionsList = paramsOptionsList;
            return this;
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new ArrayList<>();
            }
            this.validateList.add(validate);
            return this;
        }

        public Builder setField(String field) {
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

        public Builder setEmit(List<String> emit) {
            this.emit = emit;
            return this;
        }

        public Builder addParamsOptions(ParamsOptions paramsOptions) {
            if (this.paramsOptionsList == null) {
                this.paramsOptionsList = new ArrayList<>();
            }

            this.paramsOptionsList.add(paramsOptions);
            return this;
        }

        public Builder setProps(SelectParamsProps props) {
            this.props = props;
            return this;
        }

        public Builder setPlaceHolder(String placeholder) {
            if (this.props == null) {
                this.setProps(new SelectParamsProps());
            }

            ((SelectParamsProps)this.props).setPlaceholder(placeholder);
            return this;
        }

        public Builder setSize(String size) {
            if (this.props == null) {
                this.setProps(new SelectParamsProps());
            }

            ((SelectParamsProps)this.props).setSize(size);
            return this;
        }

        @Override
        public SelectParam build() {
            return new SelectParam(this);
        }
    }

    public List<ParamsOptions> getParamsOptionsList() {
        return paramsOptionsList;
    }
}
