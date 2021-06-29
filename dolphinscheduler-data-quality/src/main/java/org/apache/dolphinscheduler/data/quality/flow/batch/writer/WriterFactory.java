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

package org.apache.dolphinscheduler.data.quality.flow.batch.writer;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.WriterConfig;
import org.apache.dolphinscheduler.data.quality.enums.WriterType;
import org.apache.dolphinscheduler.data.quality.exception.DataQualityException;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchWriter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WriterFactory
 */
public class WriterFactory {

    private static final Logger logger = LoggerFactory.getLogger(WriterFactory.class);

    private static class Singleton {
        static WriterFactory instance = new WriterFactory();
    }

    public static WriterFactory getInstance() {
        return Singleton.instance;
    }

    public List<BatchWriter> getWriters(SparkRuntimeEnvironment sparkRuntimeEnvironment, List<WriterConfig> writerConfigs) throws DataQualityException {

        List<BatchWriter> writerList = new ArrayList<>();

        for (WriterConfig writerConfig:writerConfigs) {
            BatchWriter writer = getWriter(writerConfig);
            if (writer != null) {
                writer.validateConfig();
                writer.prepare(sparkRuntimeEnvironment);
                writerList.add(writer);
            }
        }

        return writerList;
    }

    private BatchWriter getWriter(WriterConfig writerConfig) throws DataQualityException {

        WriterType writerType = WriterType.getType(writerConfig.getType());
        Config config = new Config(writerConfig.getConfig());
        if (writerType != null) {
            if (writerType == WriterType.JDBC) {
                return new JdbcWriter(config);
            }
            throw new DataQualityException("writer type $readerType is not supported!");
        }

        return null;
    }

}
