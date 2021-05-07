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

package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class SparkTask extends AbstractYarnTask {


    /**
     * spark1 command
     * usage: spark-submit [options] <app jar | python file> [app arguments]
     */
    private static final String SPARK1_COMMAND = "${SPARK_HOME1}/bin/spark-submit";

    /**
     * spark2 command
     * usage: spark-submit [options] <app jar | python file> [app arguments]
     */
    private static final String SPARK2_COMMAND = "${SPARK_HOME2}/bin/spark-submit";

    /**
     * spark parameters
     */
    private SparkParameters sparkParameters;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskRequest;

    public SparkTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);
        this.taskRequest = taskRequest;
    }

    @Override
    public void init() {

        logger.info("spark task params {}", taskRequest.getTaskParams());

        sparkParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), SparkParameters.class);

        if (null == sparkParameters) {
            logger.error("Spark params is null");
            return;
        }

        if (!sparkParameters.checkParameters()) {
            throw new RuntimeException("spark task params is not valid");
        }
        sparkParameters.setQueue(taskRequest.getQueue());
        setMainJarName();
    }

    @Override
    public String getPreScript() {
        // spark-submit [options] <app jar | python file> [app arguments]
        List<String> args = new ArrayList<>();

        // spark version
        String sparkCommand = SPARK2_COMMAND;

        if (SparkVersion.SPARK1.name().equals(sparkParameters.getSparkVersion())) {
            sparkCommand = SPARK1_COMMAND;
        }

        args.add(sparkCommand);

        // other parameters
        args.addAll(SparkArgsUtils.buildArgs(sparkParameters));
        return String.join(" ", args);
    }

    private String command;

    @Override
    public void setCommand(String command) {
        logger.info("spark task command: {}", command);
        this.command = command;
    }

    /**
     * get command
     *
     * @return command
     */
    @Override
    protected String getCommand() {
        return command;
    }

    @Override
    protected void setMainJarName() {
        // main jar
        ResourceInfo mainJar = sparkParameters.getMainJar();

        if (null == mainJar) {
            throw new RuntimeException("Spark task jar params is null");
        }

        int resourceId = mainJar.getId();
        String resourceName;
        if (resourceId == 0) {
            resourceName = mainJar.getRes();
        } else {
            //fixme when update resource maybe has error ,也许也可以交给上层去做控制 需要看资源是否可以抽象为共性 目前来讲我认为是可以的
            resourceName = mainJar.getResourceName().replaceFirst("/", "");
        }
        mainJar.setRes(resourceName);
        sparkParameters.setMainJar(mainJar);

    }

    @Override
    public AbstractParameters getParameters() {
        return sparkParameters;
    }
}
