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

package org.apache.dolphinscheduler.spi.params.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * the props field in form-create`s json rule
 */
public class ParamsProps {

    private String placeholder;

    private String size = "small";

    private String inputType;

    public void setSize(String size) {
        this.size = size;
    }

    @JsonProperty("size")
    public String getSize() {
        return size;
    }

    @JsonProperty("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    @JsonProperty("type")
    @JsonInclude(value = Include.NON_NULL)
    public String getInputType() {
        return inputType;
    }

    public ParamsProps setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    public ParamsProps setInputType(String inputType) {
        this.inputType = inputType;
        return this;
    }

}
