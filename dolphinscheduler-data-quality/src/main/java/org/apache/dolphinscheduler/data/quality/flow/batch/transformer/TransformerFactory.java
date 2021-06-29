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

package org.apache.dolphinscheduler.data.quality.flow.batch.transformer;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.TransformerConfig;
import org.apache.dolphinscheduler.data.quality.enums.TransformerType;
import org.apache.dolphinscheduler.data.quality.exception.DataQualityException;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * WriterFactory
 */
public class TransformerFactory {

    private static class Singleton {
        static TransformerFactory instance = new TransformerFactory();
    }

    public static TransformerFactory getInstance() {
        return Singleton.instance;
    }

    public List<BatchTransformer> getTransformer(SparkRuntimeEnvironment sparkRuntimeEnvironment, List<TransformerConfig> transformerConfigs) throws DataQualityException {

        List<BatchTransformer> transformers = new ArrayList<>();

        for (TransformerConfig transformerConfig:transformerConfigs) {
            BatchTransformer transformer = getTransformer(transformerConfig);
            if (transformer != null) {
                transformer.validateConfig();
                transformer.prepare(sparkRuntimeEnvironment);
                transformers.add(transformer);
            }
        }

        return transformers;
    }

    private BatchTransformer getTransformer(TransformerConfig transformerConfig) throws DataQualityException {
        TransformerType type = TransformerType.getType(transformerConfig.getType());
        Config config = new Config(transformerConfig.getConfig());
        if (type != null) {
            if (type == TransformerType.SQL) {
                return new SqlTransformer(config);
            }
            throw new DataQualityException("writer type $readerType is not supported!");
        }

        return null;
    }

}
