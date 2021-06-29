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

package org.apache.dolphinscheduler.data.quality.execution;

import org.apache.dolphinscheduler.data.quality.config.Config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

/**
 * SparkRuntimeEnvironment
 */
public class SparkRuntimeEnvironment {

    private static final String TYPE = "type";
    private static final String STREAM = "stream";
    private static final String BATCH = "batch";

    private SparkSession sparkSession;

    private Config config = new Config();

    public SparkRuntimeEnvironment(Config config) {
        if (config != null) {
            this.config = config;
        }

        this.prepare();
    }

    public Config getConfig() {
        return this.config;
    }

    public void prepare() {
        sparkSession = SparkSession.builder().config(createSparkConf()).getOrCreate();
    }

    private SparkConf createSparkConf() {
        SparkConf conf = new SparkConf();
        this.config.entrySet()
            .forEach(entry -> {
                conf.set(entry.getKey(), String.valueOf(entry.getValue()));
            });
        conf.set("spark.sql.crossJoin.enabled","true");
        return conf;
    }

    public SparkSession sparkSession() {
        return sparkSession;
    }

    public boolean isBatch() {
        return BATCH.equals(config.getString(TYPE).toLowerCase());
    }

    public SparkBatchExecution getBatchExecution() {
        return new SparkBatchExecution(this);
    }
}
