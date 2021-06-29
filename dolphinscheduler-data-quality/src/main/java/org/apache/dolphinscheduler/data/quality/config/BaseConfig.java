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

package org.apache.dolphinscheduler.data.quality.config;

import org.apache.dolphinscheduler.data.quality.utils.Preconditions;
import org.apache.dolphinscheduler.data.quality.utils.StringUtils;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * BaseConfig
 */
public class BaseConfig implements IConfig {

    @JsonProperty("type")
    private String type;

    @JsonProperty("config")
    private Map<String,Object> config;

    public BaseConfig() {
    }

    public BaseConfig(String type, Map<String,Object> config) {
        this.type = type;
        this.config = config;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type should not be empty");
        Preconditions.checkArgument(config != null, "config should not be empty");
    }
}
