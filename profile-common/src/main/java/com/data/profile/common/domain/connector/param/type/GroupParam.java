package com.data.profile.common.domain.connector.param.type;

import com.data.profile.common.domain.connector.param.FormType;
import com.data.profile.common.domain.connector.param.PluginParams;
import com.data.profile.common.domain.connector.param.Validate;
import com.data.profile.common.domain.connector.param.props.GroupParamsProps;

import java.util.ArrayList;
import java.util.List;

/**
 * Text param
 */
public class GroupParam extends PluginParams {

    private GroupParam(Builder builder) {
        super(builder);
    }

    public static Builder newBuilder(String field, String title) {
        return new Builder(field, title);
    }

    public static class Builder extends PluginParams.Builder {

        public Builder(String field, String title) {
            super(field, FormType.GROUP, title);
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
        
        public Builder setProps(GroupParamsProps props) {
            this.props = props;
            return this;
        }

        public Builder setRules(List<PluginParams> rules) {
            if (this.props == null) {
                this.setProps(new GroupParamsProps());
            }

            ((GroupParamsProps)this.props).setRules(rules);
            return this;
        }
        
        @Override
        public GroupParam build() {
            return new GroupParam(this);
        }
    }
}
