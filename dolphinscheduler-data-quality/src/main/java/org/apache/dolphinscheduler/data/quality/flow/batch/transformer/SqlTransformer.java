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

import static org.apache.dolphinscheduler.data.quality.Constants.SQL;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.config.ValidateResult;
import org.apache.dolphinscheduler.data.quality.execution.SparkRuntimeEnvironment;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchTransformer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * SqlTransformer
 */
public class SqlTransformer implements BatchTransformer {

    private final Config config;

    public SqlTransformer(Config config) {
        this.config = config;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public ValidateResult validateConfig() {
        if (config.has(SQL)) {
            return new ValidateResult(true, "");
        } else {
            return new ValidateResult(false, "please specify [sql]");
        }
    }

    @Override
    public void prepare(SparkRuntimeEnvironment prepareEnv) {

    }

    @Override
    public Dataset<Row> transform(Dataset<Row> data, SparkRuntimeEnvironment env) {
        return env.sparkSession().sql(config.getString(SQL));
    }
}
