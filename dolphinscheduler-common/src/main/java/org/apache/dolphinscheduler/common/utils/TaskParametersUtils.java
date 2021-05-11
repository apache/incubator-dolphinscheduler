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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.task.conditions.SwitchParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.python.PythonParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.task.subprocess.SubProcessParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * task parameters utils
 */
public class TaskParametersUtils {

    private static Logger logger = LoggerFactory.getLogger(TaskParametersUtils.class);

    private TaskParametersUtils() {
        throw new UnsupportedOperationException("Construct TaskParametersUtils");
    }

    /**
     * get task parameters
     *
     * @param taskType task type
     * @param parameter parameter
     * @return task parameters
     */
    public static AbstractParameters getParameters(String taskType, String parameter) {
        TaskType anEnum = EnumUtils.getEnum(TaskType.class, taskType);
        if (anEnum == null) {
            logger.error("not support task type: {}", taskType);
            return null;
        }
        switch (anEnum) {
            case SUB_PROCESS:
                return JSONUtils.parseObject(parameter, SubProcessParameters.class);
            case SHELL:
            case WATERDROP:
                return JSONUtils.parseObject(parameter, ShellParameters.class);
            case PROCEDURE:
                return JSONUtils.parseObject(parameter, ProcedureParameters.class);
            case SQL:
                return JSONUtils.parseObject(parameter, SqlParameters.class);
            case MR:
                return JSONUtils.parseObject(parameter, MapReduceParameters.class);
            case SPARK:
                return JSONUtils.parseObject(parameter, SparkParameters.class);
            case PYTHON:
                return JSONUtils.parseObject(parameter, PythonParameters.class);
            case DEPENDENT:
                return JSONUtils.parseObject(parameter, DependentParameters.class);
            case FLINK:
                return JSONUtils.parseObject(parameter, FlinkParameters.class);
            case HTTP:
                return JSONUtils.parseObject(parameter, HttpParameters.class);
            case DATAX:
                return JSONUtils.parseObject(parameter, DataxParameters.class);
            case CONDITIONS:
                return JSONUtils.parseObject(parameter, ConditionsParameters.class);
            case SQOOP:
                return JSONUtils.parseObject(parameter, SqoopParameters.class);
            case SWITCH:
                return JSONUtils.parseObject(parameter, SwitchParameters.class);
            default:
                return null;
        }

    }
}
