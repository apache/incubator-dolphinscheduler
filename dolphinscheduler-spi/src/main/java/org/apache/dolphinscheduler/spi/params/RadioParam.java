/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.spi.params;

import static org.apache.dolphinscheduler.spi.utils.Constants.STRING_PLUGIN_PARAM_OPTIONS;

import org.apache.dolphinscheduler.spi.params.base.FormType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * radio
 */
public class RadioParam extends PluginParams {

    @JsonProperty(STRING_PLUGIN_PARAM_OPTIONS)
    private List<ParamsOptions> paramsOptionsList;

    private RadioParam(Builder builder) {
        super(builder);
        this.paramsOptionsList = builder.paramsOptionsList;
    }

    public static Builder newBuilder(String name, String title) {
        return new Builder(name, title);
    }

    public static class Builder extends PluginParams.Builder {

        private List<ParamsOptions> paramsOptionsList;

        public Builder(String name, String title) {
            super(name, FormType.RADIO, title);
        }

        public Builder addValidate(Validate validate) {
            if (this.validateList == null) {
                this.validateList = new ArrayList<>();
            }
            this.validateList.add(validate);
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
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

        public Builder setParamsOptionsList(List<ParamsOptions> paramsOptionsList) {
            this.paramsOptionsList = paramsOptionsList;
            return this;
        }

        public Builder addParamsOptions(ParamsOptions paramsOptions) {
            if (this.paramsOptionsList == null) {
                this.paramsOptionsList = new ArrayList<>();
            }

            this.paramsOptionsList.add(paramsOptions);
            return this;
        }

        @Override
        public RadioParam build() {
            return new RadioParam(this);
        }
    }

    public List<ParamsOptions> getParamsOptionsList() {
        return paramsOptionsList;
    }
}
