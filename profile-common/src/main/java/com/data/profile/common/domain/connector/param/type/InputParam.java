package com.data.profile.common.domain.connector.param.type;

import com.data.profile.common.domain.connector.param.FormType;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.PropsType;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.InputParamsProps;

import java.util.ArrayList;
import java.util.List;

public class InputParam extends PluginParams {

    private InputParam(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder(String field, String title) {
        return new Builder(field, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String field, String title) {
            super(field, FormType.INPUT, title);
        }

        public Builder addValidate(Validate validate) {
            if (validate == null) {
                return this;
            }
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
            if (value == null) {
                return this;
            }

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

        public Builder setPlaceholder(String placeholder) {
            if (this.props == null) {
                this.setProps(new InputParamsProps());
            }

            ((InputParamsProps)this.props).setPlaceholder(placeholder);
            return this;
        }

        public Builder setProps(InputParamsProps props) {
            this.props = props;
            return this;
        }

        public Builder setSize(String size) {
            if (this.props == null) {
                this.setProps(new InputParamsProps());
            }

            ((InputParamsProps)this.props).setSize(size);
            return this;
        }

        public Builder setType(PropsType type) {
            if (this.props == null) {
                this.setProps(new InputParamsProps());
            }

            ((InputParamsProps)this.props).setPropsType(type);
            return this;
        }

        public Builder setRows(int rows) {
            if (this.props == null) {
                this.setProps(new InputParamsProps());
            }

            ((InputParamsProps)this.props).setRows(rows);
            return this;
        }

        @Override
        public InputParam build() {
            return new InputParam(this);
        }
    }
}
