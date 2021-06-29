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

package org.apache.dolphinscheduler.data.quality.flow.batch.reader;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchReader;
import org.apache.dolphinscheduler.data.quality.utils.TypesafeConfigUtils;

import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AbstractJdbcSource
 */
public class JdbcReader implements BatchReader {

    private final Config config;

    public JdbcReader(Config config) {
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
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new ValidateResult(true, "");
        }
    }

    @Override
    public void prepare(SparkRuntimeEnvironment prepareEnv) {

    }

    @Override
    public Dataset<Row> read(SparkRuntimeEnvironment env) {
        return jdbcReader(env.sparkSession()).load();
    }

    private DataFrameReader jdbcReader(SparkSession sparkSession) {

        DataFrameReader reader = sparkSession.read()
                .format("jdbc")
                .option("url", config.getString("url"))
                .option("dbtable", config.getString("table"))
                .option("user", config.getString("user"))
                .option("password", config.getString("password"))
                .option("driver", config.getString("driver"));

        Config jdbcConfig = TypesafeConfigUtils.extractSubConfigThrowable(config, "jdbc.", false);

        if (!config.isEmpty()) {
            Map<String,String> optionMap = new HashMap<>(16);
            jdbcConfig.entrySet().forEach(x -> {
                optionMap.put(x.getKey(),String.valueOf(x.getValue()));
            });
            reader.options(optionMap);
        }

        return reader;
    }
}
