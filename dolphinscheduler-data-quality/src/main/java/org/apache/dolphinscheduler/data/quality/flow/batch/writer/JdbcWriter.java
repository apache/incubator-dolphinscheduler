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
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchWriter;
import org.apache.dolphinscheduler.data.quality.utils.StringUtils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcWriter
 */
public class JdbcWriter implements BatchWriter {

    private static final Logger logger = LoggerFactory.getLogger(JdbcWriter.class);

    private final Config config;

    public JdbcWriter(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ValidateResult validateConfig() {
        List<String> requiredOptions = Arrays.asList("url", "table", "user", "password");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x -> {
            if (!config.has(x)) {
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new ValidateResult(
                    false,
                    "please specify" + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new ValidateResult(true, "");
        }
    }

    @Override
    public void prepare(SparkRuntimeEnvironment prepareEnv) {

        if (StringUtils.isEmpty(config.getString("save_mode"))) {
            config.put("save_mode","append");
        }
    }

    @Override
    public void write(Dataset<Row> data, SparkRuntimeEnvironment env) {

        if (!StringUtils.isBlank(config.getString("sql"))) {
            data = env.sparkSession().sql(config.getString("sql"));
        }

        data.write()
            .format("jdbc")
            .option("driver",config.getString("driver"))
            .option("url",config.getString("url"))
            .option("dbtable", config.getString("table"))
            .option("user", config.getString("user"))
            .option("password", config.getString("password"))
            .mode(config.getString("save_mode"))
            .save();
    }
}
