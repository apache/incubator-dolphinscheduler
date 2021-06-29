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

import static org.apache.dolphinscheduler.data.quality.Constants.INPUT_TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.OUTPUT_TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.TMP_TABLE;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.exception.ConfigRuntimeException;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchReader;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchTransformer;
import org.apache.dolphinscheduler.data.quality.flow.batch.BatchWriter;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

/**
 * SparkBatchExecution
 */
public class SparkBatchExecution implements Execution<BatchReader, BatchTransformer, BatchWriter> {

    private final SparkRuntimeEnvironment environment;

    public SparkBatchExecution(SparkRuntimeEnvironment environment) throws ConfigRuntimeException {
        this.environment = environment;
    }

    @Override
    public void execute(List<BatchReader> readers, List<BatchTransformer> transformers, List<BatchWriter> writers) {
        readers.forEach(reader -> {
            registerInputTempView(reader, environment);
        });

        if (!readers.isEmpty()) {
            Dataset<Row> ds = readers.get(0).read(environment);
            for (BatchTransformer tf:transformers) {
                if (ds.takeAsList(1).size() > 0) {
                    ds = executeTransformer(environment, tf, ds);
                    registerTransformTempView(tf, ds);
                }
            }

            for (BatchWriter sink: writers) {
                executeWriter(environment, sink, ds);
            }
        }

        environment.sparkSession().stop();
    }

    private void registerTempView(String tableName, Dataset<Row> ds) {
        ds.createOrReplaceTempView(tableName);
    }

    private void registerInputTempView(BatchReader reader, SparkRuntimeEnvironment environment) {
        Config conf = reader.getConfig();
        if (conf.has(OUTPUT_TABLE)) {
            String tableName = conf.getString(OUTPUT_TABLE);
            registerTempView(tableName, reader.read(environment));
        } else {
            throw new ConfigRuntimeException(
                    "[" + reader.getClass().getName() + "] must be registered as dataset, please set \"output_table\" config");
        }
    }

    private Dataset<Row> executeTransformer(SparkRuntimeEnvironment environment, BatchTransformer transformer, Dataset<Row> dataset) {
        Config config = transformer.getConfig();
        Dataset<Row> inputDataset;
        Dataset<Row> outputDataset = null;
        if (config.has(INPUT_TABLE)) {
            String[] tableNames = config.getString(INPUT_TABLE).split(",");

            for (String sourceTableName: tableNames) {
                inputDataset = environment.sparkSession().read().table(sourceTableName);

                if (outputDataset == null) {
                    outputDataset = inputDataset;
                } else {
                    outputDataset = outputDataset.union(inputDataset);
                }
            }
        } else {
            outputDataset = dataset;
        }

        if (config.has(TMP_TABLE)) {
            if (outputDataset == null) {
                outputDataset = dataset;
            }
            String tableName = config.getString(TMP_TABLE);
            registerTempView(tableName, outputDataset);
        }

        return transformer.transform(outputDataset, environment);
    }

    private void registerTransformTempView(BatchTransformer transformer, Dataset<Row> ds) {
        Config config = transformer.getConfig();
        if (config.has(OUTPUT_TABLE)) {
            String tableName = config.getString(OUTPUT_TABLE);
            registerTempView(tableName, ds);
        }
    }

    private void executeWriter(SparkRuntimeEnvironment environment, BatchWriter writer, Dataset<Row> ds) {
        Config config = writer.getConfig();
        Dataset<Row> inputDataSet = ds;
        if (config.has(INPUT_TABLE)) {
            String sourceTableName = config.getString(INPUT_TABLE);
            inputDataSet = environment.sparkSession().read().table(sourceTableName);
        }
        writer.write(inputDataSet, environment);
    }
}
