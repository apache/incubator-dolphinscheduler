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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.spi.task.AbstractTask;
import org.apache.dolphinscheduler.spi.task.TaskRequest;

import org.slf4j.Logger;

/**
 * abstract yarn task
 */
public abstract class AbstractYarnTask extends AbstractTask {
    /**
     * process task
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * Abstract Yarn Task
     *
     * @param taskRequest taskRequest
     * @param logger logger
     */
    public AbstractYarnTask(TaskRequest taskRequest, Logger logger) {
        super(taskRequest, logger);
        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskRequest,
                logger);
    }

    @Override
    public void handle() throws Exception {
        try {
            // SHELL task exit code
            TaskResponse response = shellCommandExecutor.run(getCommand());
            setExitStatusCode(response.getExitStatusCode());
            setAppIds(response.getAppIds());
            setProcessId(response.getProcessId());
        } catch (Exception e) {
            logger.error("yarn process failure", e);
            exitStatusCode = -1;
            throw e;
        }
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

        //todo 交给上层处理
        shellCommandExecutor.cancelApplication();
        //  TaskInstance taskInstance = processService.findTaskInstanceById(taskExecutionContext.getTaskInstanceId());
        // if (status && taskInstance != null){
        //   ProcessUtils.killYarnJob(taskExecutionContext);
        //  }
    }

    /**
     * create command
     *
     * @return String
     * @throws Exception exception
     */
    protected abstract String getCommand();

    /**
     * set main jar name
     */
    protected abstract void setMainJarName();
}
