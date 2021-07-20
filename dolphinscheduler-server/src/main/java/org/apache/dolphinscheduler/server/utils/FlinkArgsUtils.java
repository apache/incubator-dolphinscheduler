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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * flink args utils
 */
public class FlinkArgsUtils {

    private static final String LOCAL_DEPLOY_MODE = "local";

    private static final String FLINK_VERSION_BEFORE_1_10 = "<1.10";

    private FlinkArgsUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * build args
     * @param param flink parameters
     * @return argument list
     */
    public static List<String> buildArgs(FlinkParameters param) {
        List<String> args = new ArrayList<>();

        String deployMode = "cluster";
        String tmpDeployMode = param.getDeployMode();
        if (StringUtils.isNotEmpty(tmpDeployMode)) {
            deployMode = tmpDeployMode;
        }
        String others = param.getOthers();
        if (!LOCAL_DEPLOY_MODE.equals(deployMode)) {
            args.add(Constants.FLINK_RUN_MODE);  //-m

            args.add(Constants.FLINK_YARN_CLUSTER);   //yarn-cluster

            int slot = param.getSlot();
            if (slot > 0) {
                args.add(Constants.FLINK_YARN_SLOT);
                args.add(String.format("%d", slot));   //-ys
            }

            String appName = param.getAppName();
            if (StringUtils.isNotEmpty(appName)) { //-ynm
                args.add(Constants.FLINK_APP_NAME);
                args.add(ArgsUtils.escape(appName));
            }

            // judge flink version, the parameter -yn has removed from flink 1.10
            String flinkVersion = param.getFlinkVersion();
            if (flinkVersion == null || FLINK_VERSION_BEFORE_1_10.equals(flinkVersion)) {
                int taskManager = param.getTaskManager();
                if (taskManager > 0) {                        //-yn
                    args.add(Constants.FLINK_TASK_MANAGE);
                    args.add(String.format("%d", taskManager));
                }
            }
            String jobManagerMemory = param.getJobManagerMemory();
            if (StringUtils.isNotEmpty(jobManagerMemory)) {
                args.add(Constants.FLINK_JOB_MANAGE_MEM);
                args.add(jobManagerMemory); //-yjm
            }

            String taskManagerMemory = param.getTaskManagerMemory();
            if (StringUtils.isNotEmpty(taskManagerMemory)) { // -ytm
                args.add(Constants.FLINK_TASK_MANAGE_MEM);
                args.add(taskManagerMemory);
            }

            if (StringUtils.isEmpty(others) || !others.contains(Constants.FLINK_QUEUE)) {
                String queue = param.getQueue();
                if (StringUtils.isNotEmpty(queue)) { // -yqu
                    args.add(Constants.FLINK_QUEUE);
                    args.add(queue);
                }
            }
        }

        int parallelism = param.getParallelism();
        if (parallelism > 0) {
            args.add(Constants.FLINK_PARALLELISM);
            args.add(String.format("%d", parallelism));   // -p
        }

        // If the job is submitted in attached mode, perform a best-effort cluster shutdown when the CLI is terminated abruptly
        // The task status will be synchronized with the cluster job status
        args.add(Constants.FLINK_SHUTDOWN_ON_ATTACHED_EXIT); // -sae

        // -s -yqu -yat -yD -D
        if (StringUtils.isNotEmpty(others)) {
            args.add(others);
        }

        ProgramType programType = param.getProgramType();
        String mainClass = param.getMainClass();

        if (ProgramType.PYTHON == programType) {
            // -py
            args.add(Constants.FLINK_PYTHON);
        } else if (programType != null && StringUtils.isNotEmpty(mainClass)) {
            // -c
            args.add(Constants.FLINK_MAIN_CLASS);
            // main class
            args.add(param.getMainClass());
        }

        ResourceInfo mainJar = param.getMainJar();
        if (mainJar != null) {
            args.add(mainJar.getRes());
        }

        String mainArgs = param.getMainArgs();
        if (StringUtils.isNotEmpty(mainArgs)) {
            args.add(mainArgs);
        }

        return args;
    }

}
