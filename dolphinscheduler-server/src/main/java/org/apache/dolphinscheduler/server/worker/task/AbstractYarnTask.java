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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.slf4j.Logger;


/**
 * abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractTask {

    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(Constants.APPLICATION_REGEX);

    /**
     * process database access
     */
    protected ProcessService processService;

    /**
     * process task
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * Abstract Yarn Task
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    public AbstractYarnTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.processService = SpringApplicationContext.getBean(ProcessService.class);
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,
                logger);
    }

    @Override
    public void handle() throws Exception {
        try {
            // SHELL task exit code
            CommandExecuteResult commandExecuteResult = shellCommandExecutor.run(buildCommand());
            // parse appIds from log
            List<String> appIds = parseAppIdsFromLog(taskExecutionContext.getLogPath());
            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            // if yarn task , yarn state is final state
            if (commandExecuteResult.getExitStatusCode() == Constants.EXIT_CODE_SUCCESS) {
                setExitStatusCode(isSuccessOfYarnState(appIds) ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE);
            }
            setAppIds(String.join(Constants.COMMA, appIds));
            setProcessId(commandExecuteResult.getProcessId());
        } catch (Exception e) {
            logger.error("yarn process failure", e);
            exitStatusCode = -1;
            throw e;
        }
    }

    /**
     * check yarn state
     *
     * @param appIds application id list
     * @return is success of yarn task state
     */
    public boolean isSuccessOfYarnState(List<String> appIds) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while (Stopper.isRunning()) {
                    ExecutionStatus applicationStatus = HadoopUtils.getInstance().getApplicationStatus(appId);
                    logger.info("appId:{}, final state:{}", appId, applicationStatus.name());
                    if (ExecutionStatus.FAILURE.equals(applicationStatus)
                            || ExecutionStatus.KILL.equals(applicationStatus)) {
                        return false;
                    }
                    if (ExecutionStatus.SUCCESS.equals(applicationStatus)) {
                        break;
                    }
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", appIds.toString()), e);
            result = false;
        }
        return result;

    }

    /**
     * parse yarn app id list from log
     *
     * @param logPath log path
     * @return app id list
     */
    private List<String> parseAppIdsFromLog(String logPath) {
        List<String> logs = convertFile2List(logPath);

        List<String> appIds = new ArrayList<>();
        /**
         * analysis log?get submited yarn application id
         */
        for (String log : logs) {
            String appId = findAppId(log);
            if (StringUtils.isNotEmpty(appId) && !appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    /**
     * convert file to list
     *
     * @param filename file name
     * @return line list
     */
    private List<String> convertFile2List(String filename) {
        List lineList = new ArrayList<String>(100);
        File file = new File(filename);

        if (!file.exists()) {
            return lineList;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ", filename), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
        return lineList;
    }

    /**
     * find app id
     *
     * @param line line
     * @return appid
     */
    private String findAppId(String line) {
        Matcher matcher = APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * cancel application
     *
     * @param status status
     * @throws Exception exception
     */
    @Override
    public void cancelApplication(boolean status) throws Exception {
        cancel = true;
        // cancel process
        shellCommandExecutor.cancelApplication();
        TaskInstance taskInstance = processService.findTaskInstanceById(taskExecutionContext.getTaskInstanceId());
        if (status && taskInstance != null) {
            ProcessUtils.killYarnJob(taskExecutionContext);
        }
    }

    /**
     * create command
     *
     * @return String
     * @throws Exception exception
     */
    protected abstract String buildCommand() throws Exception;

    /**
     * set main jar name
     */
    protected abstract void setMainJarName();
}
