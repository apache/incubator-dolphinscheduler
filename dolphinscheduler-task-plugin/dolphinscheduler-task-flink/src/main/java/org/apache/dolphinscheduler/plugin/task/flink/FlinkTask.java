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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

public class FlinkTask extends AbstractYarnTask {


    /**
     * flink command
     * usage: flink run [OPTIONS] <jar-file> <arguments>
     */
    private static final String FLINK_COMMAND = "flink";
    private static final String FLINK_RUN = "run";

    /**
     * flink parameters
     */
    private FlinkParameters flinkParameters;

    private String command;


    private TaskRequest flinkRequest;

    public FlinkTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);
        this.flinkRequest = taskRequest;
    }

    @Override
    public String getPreScript() {

        // flink run [OPTIONS] <jar-file> <arguments>
        List<String> args = new ArrayList<>();

        args.add(FLINK_COMMAND);
        args.add(FLINK_RUN);
        logger.info("flink task args : {}", args);
        // other parameters
        args.addAll(FlinkArgsUtils.buildArgs(flinkParameters));
        return String.join(" ", args);

    }

    @Override
    public void setCommand(String command) {
        this.command = command;

    }

    @Override
    public void init() {

        logger.info("flink task params {}", flinkRequest.getTaskParams());

        flinkParameters = JSONUtils.parseObject(flinkRequest.getTaskParams(), FlinkParameters.class);

        if (!flinkParameters.checkParameters()) {
            throw new TaskException("flink task params is not valid");
        }
    }

    /**
     * create command
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
        ResourceInfo mainJar = flinkParameters.getMainJar();
        if (mainJar != null) {
            int resourceId = mainJar.getId();
            String resourceName;
            if (resourceId == 0) {
                resourceName = mainJar.getRes();
            } else {
                //when update resource maybe has error ,也许也可以交给上层去做控制 需要看资源是否可以抽象为共性 目前来讲我认为是可以的
                resourceName = mainJar.getResourceName().replaceFirst("/", "");
            }
            mainJar.setRes(resourceName);
            flinkParameters.setMainJar(mainJar);
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return flinkParameters;
    }
}
