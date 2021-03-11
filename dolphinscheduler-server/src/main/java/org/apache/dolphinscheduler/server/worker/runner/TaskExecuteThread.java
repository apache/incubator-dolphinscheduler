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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.RetryerUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.ResponseCache;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskManager;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.apache.commons.collections.MapUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rholder.retry.RetryException;

/**
 *  task scheduler thread
 */
public class TaskExecuteThread implements Runnable, Delayed {

    /**
     * logger
     */
    private final Logger logger = LoggerFactory.getLogger(TaskExecuteThread.class);

    /**
     *  task instance
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     *  abstract task
     */
    private AbstractTask task;

    /**
     *  task callback service
     */
    private TaskCallbackService taskCallbackService;

    /**
     * taskExecutionContextCacheManager
     */
    private TaskExecutionContextCacheManager taskExecutionContextCacheManager;

    /**
     * task logger
     */
    private Logger taskLogger;

    /**
     * alert client server
     */
    private AlertClientService alertClientService;

    /**
     *  constructor
     * @param taskExecutionContext taskExecutionContext
     * @param taskCallbackService taskCallbackService
     */
    public TaskExecuteThread(TaskExecutionContext taskExecutionContext
            , TaskCallbackService taskCallbackService
            , Logger taskLogger, AlertClientService alertClientService) {
        this.taskExecutionContext = taskExecutionContext;
        this.taskCallbackService = taskCallbackService;
        this.taskExecutionContextCacheManager = SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class);
        this.taskLogger = taskLogger;
        this.alertClientService = alertClientService;
    }

    @Override
    public void run() {

        TaskExecuteResponseCommand responseCommand = new TaskExecuteResponseCommand(taskExecutionContext.getTaskInstanceId());
        try {
            logger.info("script path : {}", taskExecutionContext.getExecutePath());
            // check if the OS user exists
            if (!OSUtils.getUserList().contains(taskExecutionContext.getTenantCode())) {
                String errorLog = String.format("tenantCode: %s does not exist", taskExecutionContext.getTenantCode());
                taskLogger.error(errorLog);
                responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
                responseCommand.setEndTime(new Date());
                return;
            }

            // task node
            TaskNode taskNode = JSONUtils.parseObject(taskExecutionContext.getTaskJson(), TaskNode.class);

            if (taskExecutionContext.getStartTime() == null) {
                taskExecutionContext.setStartTime(new Date());
            }
            if (taskExecutionContext.getCurrentExecutionStatus() != ExecutionStatus.RUNNING_EXECUTION) {
                changeTaskExecutionStatusToRunning();
            }
            logger.info("the task begins to execute. task instance id: {}", taskExecutionContext.getTaskInstanceId());

            // copy hdfs/minio file to local
            downloadResource(taskExecutionContext.getExecutePath(),
                    taskExecutionContext.getResources(),
                    logger);

            taskExecutionContext.setTaskParams(taskNode.getParams());
            taskExecutionContext.setEnvFile(CommonUtils.getSystemEnvPath());
            taskExecutionContext.setDefinedParams(getGlobalParamsMap());

            // set task timeout
            setTaskTimeout(taskExecutionContext, taskNode);

            taskExecutionContext.setTaskAppId(String.format("%s_%s_%s",
                    taskExecutionContext.getProcessDefineId(),
                    taskExecutionContext.getProcessInstanceId(),
                    taskExecutionContext.getTaskInstanceId()));

            task = TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService);

            // task init
            task.init();

            // task handle
            task.handle();

            // task result process
            task.after();

            responseCommand.setStatus(task.getExitStatus().getCode());
            responseCommand.setEndTime(new Date());
            responseCommand.setProcessId(task.getProcessId());
            responseCommand.setAppIds(task.getAppIds());
            responseCommand.setVarPool(task.getVarPool());
            responseCommand.setResult(task.getResultString());
            logger.info("task instance id : {},task final status : {}", taskExecutionContext.getTaskInstanceId(), task.getExitStatus());
        } catch (Exception e) {
            logger.error("task scheduler failure", e);
            kill();
            responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
            responseCommand.setEndTime(new Date());
            responseCommand.setProcessId(task.getProcessId());
            responseCommand.setAppIds(task.getAppIds());
        } finally {
            taskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
            ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(),responseCommand.convert2Command(),Event.RESULT);
            taskCallbackService.sendResult(taskExecutionContext.getTaskInstanceId(), responseCommand.convert2Command());

        }
    }

    /**
     * get global paras map
     * @return
     */
    private Map<String, String> getGlobalParamsMap() {
        Map<String,String> globalParamsMap = new HashMap<>(16);

        // global params string
        String globalParamsStr = taskExecutionContext.getGlobalParams();
        if (globalParamsStr != null) {
            List<Property> globalParamsList = JSONUtils.toList(globalParamsStr, Property.class);
            globalParamsMap.putAll(globalParamsList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue)));
        }
        return globalParamsMap;
    }

    /**
     * set task timeout
     * @param taskExecutionContext TaskExecutionContext
     * @param taskNode
     */
    private void setTaskTimeout(TaskExecutionContext taskExecutionContext, TaskNode taskNode) {
        // the default timeout is the maximum value of the integer
        taskExecutionContext.setTaskTimeout(Integer.MAX_VALUE);
        TaskTimeoutParameter taskTimeoutParameter = taskNode.getTaskTimeoutParameter();
        if (taskTimeoutParameter.getEnable()) {
            // get timeout strategy
            taskExecutionContext.setTaskTimeoutStrategy(taskTimeoutParameter.getStrategy().getCode());
            switch (taskTimeoutParameter.getStrategy()) {
                case WARN:
                    break;
                case FAILED:
                    if (Integer.MAX_VALUE > taskTimeoutParameter.getInterval() * 60) {
                        taskExecutionContext.setTaskTimeout(taskTimeoutParameter.getInterval() * 60);
                    }
                    break;
                case WARNFAILED:
                    if (Integer.MAX_VALUE > taskTimeoutParameter.getInterval() * 60) {
                        taskExecutionContext.setTaskTimeout(taskTimeoutParameter.getInterval() * 60);
                    }
                    break;
                default:
                    logger.error("not support task timeout strategy: {}", taskTimeoutParameter.getStrategy());
                    throw new IllegalArgumentException("not support task timeout strategy");

            }
        }
    }

    /**
     *  kill task
     */
    public void kill() {
        if (task != null) {
            try {
                task.cancelApplication(true);
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

    /**
     * download resource file
     *
     * @param execLocalPath
     * @param projectRes
     * @param logger
     */
    private void downloadResource(String execLocalPath,
                                  Map<String,String> projectRes,
                                  Logger logger) throws Exception {
        if (MapUtils.isEmpty(projectRes)) {
            return;
        }

        Set<Map.Entry<String, String>> resEntries = projectRes.entrySet();

        for (Map.Entry<String,String> resource : resEntries) {
            String fullName = resource.getKey();
            String tenantCode = resource.getValue();
            File resFile = new File(execLocalPath, fullName);
            if (!resFile.exists()) {
                try {
                    // query the tenant code of the resource according to the name of the resource
                    String resHdfsPath = HadoopUtils.getHdfsResourceFileName(tenantCode, fullName);

                    logger.info("get resource file from hdfs :{}", resHdfsPath);
                    HadoopUtils.getInstance().copyHdfsToLocal(resHdfsPath, execLocalPath + File.separator + fullName, false, true);
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                    throw new RuntimeException(e.getMessage());
                }
            } else {
                logger.info("file : {} exists ", resFile.getName());
            }
        }
    }

    /**
     * send an ack to change the status of the task.
     */
    private void changeTaskExecutionStatusToRunning() {
        taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        Command ackCommand = buildAckCommand().convert2Command();
        try {
            RetryerUtils.retryCall(() -> {
                taskCallbackService.sendAck(taskExecutionContext.getTaskInstanceId(), ackCommand);
                return Boolean.TRUE;
            });
        } catch (ExecutionException | RetryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * build ack command.
     *
     * @return TaskExecuteAckCommand
     */
    private TaskExecuteAckCommand buildAckCommand() {
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        ackCommand.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
        ackCommand.setStartTime(taskExecutionContext.getStartTime());
        ackCommand.setLogPath(taskExecutionContext.getLogPath());
        ackCommand.setHost(taskExecutionContext.getHost());
        if (taskExecutionContext.getTaskType().equals(TaskType.SQL.name())
                || taskExecutionContext.getTaskType().equals(TaskType.PROCEDURE.name())) {
            ackCommand.setExecutePath(null);
        } else {
            ackCommand.setExecutePath(taskExecutionContext.getExecutePath());
        }
        return ackCommand;
    }

    /**
     * get current TaskExecutionContext
     * @return TaskExecutionContext
     */
    public TaskExecutionContext getTaskExecutionContext() {
        return this.taskExecutionContext;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(),
                taskExecutionContext.getDelayTime() * 60L), TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
}
