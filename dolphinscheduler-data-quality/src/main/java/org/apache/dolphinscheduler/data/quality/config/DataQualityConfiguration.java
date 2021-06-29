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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DataQualityConfiguration
 */
public class DataQualityConfiguration implements IConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("env")
    private EnvConfig envConfig;

    @JsonProperty("readers")
    private List<ReaderConfig> readerConfigs;

    @JsonProperty("transformers")
    private List<TransformerConfig> transformerConfigs;

    @JsonProperty("writers")
    private List<WriterConfig> writerConfigs;

    public DataQualityConfiguration(){}

    public DataQualityConfiguration(String name,
                                    EnvConfig envConfig,
                                    List<ReaderConfig> readerConfigs,
                                    List<WriterConfig> writerConfigs,
                                    List<TransformerConfig> transformerConfigs) {
        this.name = name;
        this.envConfig = envConfig;
        this.readerConfigs = readerConfigs;
        this.writerConfigs = writerConfigs;
        this.transformerConfigs = transformerConfigs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnvConfig getEnvConfig() {
        return envConfig;
    }

    public void setEnvConfig(EnvConfig envConfig) {
        this.envConfig = envConfig;
    }

    public List<ReaderConfig> getReaderConfigs() {
        return readerConfigs;
    }

    public void setReaderConfigs(List<ReaderConfig> readerConfigs) {
        this.readerConfigs = readerConfigs;
    }

    public List<TransformerConfig> getTransformerConfigs() {
        return transformerConfigs;
    }

    public void setTransformerConfigs(List<TransformerConfig> transformerConfigs) {
        this.transformerConfigs = transformerConfigs;
    }

    public List<WriterConfig> getWriterConfigs() {
        return writerConfigs;
    }

    public void setWriterConfigs(List<WriterConfig> writerConfigs) {
        this.writerConfigs = writerConfigs;
    }

    @Override
    public void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "name should not be empty");

        Preconditions.checkArgument(envConfig != null, "env config should not be empty");

        Preconditions.checkArgument(readerConfigs != null, "reader config should not be empty");
        for (ReaderConfig readerConfig : readerConfigs) {
            readerConfig.validate();
        }

        Preconditions.checkArgument(transformerConfigs != null, "transform config should not be empty");
        for (TransformerConfig transformParameter : transformerConfigs) {
            transformParameter.validate();
        }

        Preconditions.checkArgument(writerConfigs != null, "writer config should not be empty");
        for (WriterConfig writerConfig :writerConfigs) {
            writerConfig.validate();
        }
    }

    @Override
    public String toString() {
        return "DataQualityConfiguration{"
                + "name='" + name + '\''
                + ", envConfig=" + envConfig
                + ", readerConfigs=" + readerConfigs
                + ", transformerConfigs=" + transformerConfigs
                + ", writerConfigs=" + writerConfigs
                + '}';
    }
}
