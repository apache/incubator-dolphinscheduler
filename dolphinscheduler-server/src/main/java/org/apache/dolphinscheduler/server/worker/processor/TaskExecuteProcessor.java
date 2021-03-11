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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.enums.Event;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.cache.ResponseCache;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * worker request processor
 */
public class TaskExecuteProcessor implements NettyRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteProcessor.class);

    /**
     * worker config
     */
    private final WorkerConfig workerConfig;

    /**
     * task callback service
     */
    private final TaskCallbackService taskCallbackService;

    /**
     * alert client service
     */
    private AlertClientService alertClientService;

    /**
     * taskExecutionContextCacheManager
     */
    private final TaskExecutionContextCacheManager taskExecutionContextCacheManager;

    /*
     * task execute manager
     */
    private final WorkerManagerThread workerManager;

    public TaskExecuteProcessor() {
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.taskExecutionContextCacheManager = SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class);
        this.workerManager = SpringApplicationContext.getBean(WorkerManagerThread.class);
    }

    /**
     * Pre-cache task to avoid extreme situations when kill task. There is no such task in the cache
     *
     * @param taskExecutionContext task
     */
    private void setTaskCache(TaskExecutionContext taskExecutionContext) {
        TaskExecutionContext preTaskCache = new TaskExecutionContext();
        preTaskCache.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        taskExecutionContextCacheManager.cacheTaskExecutionContext(preTaskCache);
    }

    public TaskExecuteProcessor(AlertClientService alertClientService) {
        this();
        this.alertClientService = alertClientService;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.TASK_EXECUTE_REQUEST == command.getType(),
            String.format("invalid command type : %s", command.getType()));

        TaskExecuteRequestCommand taskRequestCommand = JSONUtils.parseObject(
            command.getBody(), TaskExecuteRequestCommand.class);

        logger.info("received command : {}", taskRequestCommand);

        if (taskRequestCommand == null) {
            logger.error("task execute request command is null");
            return;
        }

        String contextJson = taskRequestCommand.getTaskExecutionContext();
        TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextJson, TaskExecutionContext.class);

        if (taskExecutionContext == null) {
            logger.error("task execution context is null");
            return;
        }

        setTaskCache(taskExecutionContext);
        // custom logger
        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
            taskExecutionContext.getProcessDefineId(),
            taskExecutionContext.getProcessInstanceId(),
            taskExecutionContext.getTaskInstanceId()));

        taskExecutionContext.setHost(NetUtils.getAddr(workerConfig.getListenPort()));
        taskExecutionContext.setLogPath(LogUtils.getTaskLogPath(taskExecutionContext));

        // local execute path
        String execLocalPath = getExecLocalPath(taskExecutionContext);
        logger.info("task instance  local execute path : {} ", execLocalPath);
        taskExecutionContext.setExecutePath(execLocalPath);

        FileUtils.taskLoggerThreadLocal.set(taskLogger);
        try {
            FileUtils.createWorkDirIfAbsent(execLocalPath);
        } catch (Throwable ex) {
            String errorLog = String.format("create execLocalPath : %s", execLocalPath);
            LoggerUtils.logError(Optional.of(logger), errorLog, ex);
            LoggerUtils.logError(Optional.ofNullable(taskLogger), errorLog, ex);
            taskExecutionContextCacheManager.removeByTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        }
        FileUtils.taskLoggerThreadLocal.remove();

        taskCallbackService.addRemoteChannel(taskExecutionContext.getTaskInstanceId(),
            new NettyRemoteChannel(channel, command.getOpaque()));

        // delay task process
        long remainTime = DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(), taskExecutionContext.getDelayTime() * 60L);
        if (remainTime > 0) {
            logger.info("delay the execution of task instance {}, delay time: {} s", taskExecutionContext.getTaskInstanceId(), remainTime);
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.DELAY_EXECUTION);
            taskExecutionContext.setStartTime(null);
        } else {
            taskExecutionContext.setCurrentExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
            taskExecutionContext.setStartTime(new Date());
        }

        this.doAck(taskExecutionContext);

        // submit task to manager
        if (!workerManager.offer(new TaskExecuteThread(taskExecutionContext, taskCallbackService, taskLogger, alertClientService))) {
            logger.info("submit task to manager error, queue is full, queue size is {}", workerManager.getQueueSize());
        }
    }

    private void doAck(TaskExecutionContext taskExecutionContext) {
        // tell master that task is in executing
        TaskExecuteAckCommand ackCommand = buildAckCommand(taskExecutionContext);
        ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(), ackCommand.convert2Command(), Event.ACK);
        taskCallbackService.sendAck(taskExecutionContext.getTaskInstanceId(), ackCommand.convert2Command());
    }

    /**
     * build ack command
     *
     * @param taskExecutionContext taskExecutionContext
     * @return TaskExecuteAckCommand
     */
    private TaskExecuteAckCommand buildAckCommand(TaskExecutionContext taskExecutionContext) {
        TaskExecuteAckCommand ackCommand = new TaskExecuteAckCommand();
        ackCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        ackCommand.setStatus(taskExecutionContext.getCurrentExecutionStatus().getCode());
        ackCommand.setLogPath(LogUtils.getTaskLogPath(taskExecutionContext));
        ackCommand.setHost(taskExecutionContext.getHost());
        ackCommand.setStartTime(taskExecutionContext.getStartTime());
        if (taskExecutionContext.getTaskType().equals(TaskType.SQL.name()) || taskExecutionContext.getTaskType().equals(TaskType.PROCEDURE.name())) {
            ackCommand.setExecutePath(null);
        } else {
            ackCommand.setExecutePath(taskExecutionContext.getExecutePath());
        }
        taskExecutionContext.setLogPath(ackCommand.getLogPath());
        return ackCommand;
    }

    /**
     * get execute local path
     *
     * @param taskExecutionContext taskExecutionContext
     * @return execute local path
     */
    private String getExecLocalPath(TaskExecutionContext taskExecutionContext) {
        return FileUtils.getProcessExecDir(taskExecutionContext.getProjectId(),
            taskExecutionContext.getProcessDefineId(),
            taskExecutionContext.getProcessInstanceId(),
            taskExecutionContext.getTaskInstanceId());
    }
}
