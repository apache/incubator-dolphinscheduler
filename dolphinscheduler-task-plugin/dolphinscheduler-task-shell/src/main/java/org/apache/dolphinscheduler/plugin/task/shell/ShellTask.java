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

package org.apache.dolphinscheduler.plugin.task.shell;

import org.apache.dolphinscheduler.plugin.task.api.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskResponse;
import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.Direct;
import org.apache.dolphinscheduler.spi.task.Property;
import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

/**
 * shell task
 */
public class ShellTask extends AbstractTask {

    /**
     * shell parameters
     */
    private ShellParameters shellParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskRequest taskRequest;

    private String command;

    /**
     * constructor
     *
     * @param taskRequest taskRequest
     * @param logger logger
     */
    public ShellTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);

        this.taskRequest = taskRequest;
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    @Override
    public void init() {
        logger.info("shell task params {}", taskRequest.getTaskParams());

        shellParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), ShellParameters.class);

        assert shellParameters != null;
        if (!shellParameters.checkParameters()) {
            throw new RuntimeException("shell task params is not valid");
        }
    }

    @Override
    public void handle() {
        try {
            // construct process
            TaskResponse response = shellCommandExecutor.run(command);
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(response.getAppIds());
            setProcessId(response.getProcessId());
            setResult(shellCommandExecutor.getTaskResultString());
        } catch (Exception e) {
            logger.error("shell task error", e);
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new TaskException("shell task error", e);
        }
    }

    @Override
    public void cancelApplication(boolean cancelApplication) throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    @Override
    public String getPreScript() {
        return shellParameters.getRawScript().replaceAll("\\r\\n", "\n");
    }

    /**
     * set command
     *
     * @throws IOException exception
     */
    @Override
    public void setCommand(String command) throws IOException {
        // generate scripts
        String fileName = String.format("%s/%s_node.%s",
                taskRequest.getExecutePath(),
                taskRequest.getTaskAppId(), OSUtils.isWindows() ? "bat" : "sh");

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            this.command = fileName;
            return;
        }
        this.command = command;
        shellParameters.setRawScript(command);

        logger.info("raw script : {}", shellParameters.getRawScript());
        logger.info("task execute path : {}", taskRequest.getExecutePath());

        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(TaskConstants.RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, shellParameters.getRawScript().getBytes(), StandardOpenOption.APPEND);
        this.command = fileName;
    }

    @Override
    public AbstractParameters getParameters() {
        return shellParameters;
    }

    public void setResult(String result) {
        Map<String, Property> localParams = shellParameters.getLocalParametersMap();
        List<Map<String, String>> outProperties = new ArrayList<>();
        Map<String, String> p = new HashMap<>();
        localParams.forEach((k, v) -> {
            if (v.getDirect() == Direct.OUT) {
                p.put(k, result);
            }
        });
        outProperties.add(p);
        resultString = JSONUtils.toJsonString(outProperties);
    }
}
