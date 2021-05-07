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

package org.apache.dolphinscheduler.spi.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * executive task
 */
public abstract class AbstractTask {

    public static final Marker FINALIZE_SESSION_MARKER = MarkerFactory.getMarker("FINALIZE_SESSION");

    /**
     * varPool string
     */
    protected String varPool;

    /**
     * taskExecutionContext
     **/
    TaskRequest taskRequest;

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
    protected AbstractTask(TaskRequest taskExecutionContext, Logger logger) {
        this.taskRequest = taskExecutionContext;
        this.logger = logger;
    }

    /**
     * init task
     */
    public void init() {
    }

    public String getPreScript() {
        return null;
    }

    public void setCommand(String command) throws Exception {

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
     * result processing maybe
     */
    public void after() {

    }

    /**
     * get exit status according to exitCode
     *
     * @return exit status
     */
    public ExecutionStatus getExitStatus() {
        ExecutionStatus status;
        switch (getExitStatusCode()) {
            case TaskConstants.EXIT_CODE_SUCCESS:
                status = ExecutionStatus.SUCCESS;
                break;
            case TaskConstants.EXIT_CODE_KILL:
                status = ExecutionStatus.KILL;
                break;
            default:
                status = ExecutionStatus.FAILURE;
                break;
        }
        return status;
    }

}