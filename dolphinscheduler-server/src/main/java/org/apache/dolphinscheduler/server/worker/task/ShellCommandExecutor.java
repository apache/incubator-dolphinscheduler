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
package org.apache.dolphinscheduler.server.worker.task;

import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * shell command executor
 */
public class ShellCommandExecutor extends AbstractCommandExecutor {

    /**
     * For Unix-like, using sh
     */
    public static final String SH = "sh";

    /**
     * For Windows, using cmd.exe
     */
    public static final String CMD = "cmd.exe";

    /**
     * constructor
     * @param logHandler logHandler
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    public ShellCommandExecutor(Consumer<LinkedBlockingQueue<String>> logHandler,
                                TaskExecutionContext taskExecutionContext,
                                Logger logger) {
        super(logHandler,taskExecutionContext,logger);
    }


    @Override
    protected String buildCommandFilePath() {
        // command file
        return String.format("%s/%s.%s"
                , taskExecutionContext.getExecutePath()
                , taskExecutionContext.getTaskAppId()
                , OSUtils.isWindows() ? "bat" : "command");
    }

    /**
     * get command type
     * @return command type
     */
    @Override
    protected String commandInterpreter() {
        return OSUtils.isWindows() ? CMD : SH;
    }


    /**
     * create command file if not exists
     * @param execCommand   exec command
     * @param commandFile   command file
     * @throws IOException  io exception
     */
    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        logger.info("tenantCode user:{}, task dir:{}", taskExecutionContext.getTenantCode(),
                taskExecutionContext.getTaskAppId());

        // create if non existence
        if (!Files.exists(Paths.get(commandFile))) {
            logger.info("create command file:{}", commandFile);

            StringBuilder sb = new StringBuilder();
            if (OSUtils.isWindows()) {
                sb.append("@echo off\n");
                sb.append("cd /d %~dp0\n");
                if (taskExecutionContext.getEnvFile() != null) {
                    sb.append("call ").append(taskExecutionContext.getEnvFile()).append("\n");
                }
            } else {
                sb.append("#!/bin/sh\n");
                sb.append("BASEDIR=$(cd `dirname $0`; pwd)\n");
                sb.append("cd $BASEDIR\n");
                if (taskExecutionContext.getEnvFile() != null) {
                    sb.append("source ").append(taskExecutionContext.getEnvFile()).append("\n");
                }
            }

            sb.append(execCommand);
            logger.info("command : {}", sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(commandFile), sb.toString(), StandardCharsets.UTF_8);
        }
    }


}
