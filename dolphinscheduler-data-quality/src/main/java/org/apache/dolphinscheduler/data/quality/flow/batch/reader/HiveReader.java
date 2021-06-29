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

import static org.apache.dolphinscheduler.data.quality.Constants.DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.SQL;
import static org.apache.dolphinscheduler.data.quality.Constants.TABLE;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchReader;
import org.apache.dolphinscheduler.data.quality.utils.StringUtils;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HiveReader
 */
public class HiveReader implements BatchReader {

    private final Config config;

    public HiveReader(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ValidateResult validateConfig() {
        List<String> requiredOptions = Arrays.asList(DATABASE, TABLE);

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
        if (StringUtils.isEmpty(config.getString(SQL))) {
            config.put(SQL,"select * from " + config.getString(DATABASE) + "." + config.getString(TABLE));
        }
    }

    @Override
    public Dataset<Row> read(SparkRuntimeEnvironment env) {
        return env.sparkSession().sql(config.getString(SQL));
    }

}
