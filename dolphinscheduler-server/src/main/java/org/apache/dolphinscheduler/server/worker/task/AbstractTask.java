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

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskRecordStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.TaskRecordDao;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

/**
 * executive task
 */
public abstract class AbstractTask {

    /**
     * varPool string
     */
    protected String varPool;

    /**
     * taskExecutionContext
     **/
    TaskExecutionContext taskExecutionContext;

    /**
     * log record
     */
    protected Logger logger;


    /**
     * SHELL process pid
     */
    protected int processId;

    /**
     * SHELL result string
     */
    protected String resultString;

    /**
     * other resource manager appId , for example : YARN etc
     */
    protected String appIds;


    /**
     * cancel
     */
    protected volatile boolean cancel = false;

    /**
     * exit code
     */
    protected volatile int exitStatusCode = -1;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    protected AbstractTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        this.taskExecutionContext = taskExecutionContext;
        this.logger = logger;
    }

    /**
     * init task
     *
     * @throws Exception exception
     */
    public void init() throws Exception {
    }

    /**
     * task handle
     *
     * @throws Exception exception
     */
    public abstract void handle() throws Exception;


    /**
     * cancel application
     *
     * @param status status
     * @throws Exception exception
     */
    public void cancelApplication(boolean status) throws Exception {
        this.cancel = status;
    }

    /**
     * log handle
     *
     * @param logs log list
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        if (logs.contains(FINALIZE_SESSION_MARKER.toString())) {
            logger.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        } else {
            logger.info(" -> {}", String.join("\n\t", logs));
        }
    }

    public void setVarPool(String varPool) {
        this.varPool = varPool;
    }

    public String getVarPool() {
        return varPool;
    }

    /**
     * get exit status code
     *
     * @return exit status code
     */
    public int getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(int exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }

    public String getAppIds() {
        return appIds;
    }

    public void setAppIds(String appIds) {
        this.appIds = appIds;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }

    /**
     * get task parameters
     *
     * @return AbstractParameters
     */
    public abstract AbstractParameters getParameters();


    /**
     * result processing
     */
    public void after() {
        if (getExitStatusCode() == Constants.EXIT_CODE_SUCCESS) {
            // task record flat : if true , start up qianfan
            if (TaskRecordDao.getTaskRecordFlag()
                    && TaskType.typeIsNormalTask(taskExecutionContext.getTaskType())) {
                AbstractParameters params = TaskParametersUtils.getParameters(taskExecutionContext.getTaskType(), taskExecutionContext.getTaskParams());

                // replace placeholder
                Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                        taskExecutionContext.getDefinedParams(),
                        params.getLocalParametersMap(),
                        CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                        taskExecutionContext.getScheduleTime());
                if (paramsMap != null && !paramsMap.isEmpty()
                        && paramsMap.containsKey("v_proc_date")) {
                    String vProcDate = paramsMap.get("v_proc_date").getValue();
                    if (!StringUtils.isEmpty(vProcDate)) {
                        TaskRecordStatus taskRecordState = TaskRecordDao.getTaskRecordState(taskExecutionContext.getTaskName(), vProcDate);
                        logger.info("task record status : {}", taskRecordState);
                        if (taskRecordState == TaskRecordStatus.FAILURE) {
                            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
                        }
                    }
                }
            }

        } else if (getExitStatusCode() == Constants.EXIT_CODE_KILL) {
            setExitStatusCode(Constants.EXIT_CODE_KILL);
        } else {
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
        }
    }


    /**
     * get exit status according to exitCode
     *
     * @return exit status
     */
    public ExecutionStatus getExitStatus() {
        ExecutionStatus status;
        switch (getExitStatusCode()) {
            case Constants.EXIT_CODE_SUCCESS:
                status = ExecutionStatus.SUCCESS;
                break;
            case Constants.EXIT_CODE_KILL:
                status = ExecutionStatus.KILL;
                break;
            default:
                status = ExecutionStatus.FAILURE;
                break;
        }
        return status;
    }

}