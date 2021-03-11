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
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.ResponseCache;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manage tasks
 */
@Component
public class WorkerManagerThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(WorkerManagerThread.class);

    /**
     * task queue
     */
    private final DelayQueue<TaskExecuteThread> workerExecuteQueue = new DelayQueue<>();

    /**
     * worker config
     */
    private final WorkerConfig workerConfig;

    /**
     * thread executor service
     */
    private final ExecutorService workerExecService;

    /**
     * taskExecutionContextCacheManager
     */
    private TaskExecutionContextCacheManager taskExecutionContextCacheManager;

    /**
     * task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public WorkerManagerThread() {
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.taskExecutionContextCacheManager = SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class);
        this.workerExecService = ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", this.workerConfig.getWorkerExecThreads());
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
    }

    /**
     * get queue size
     *
     * @return queue size
     */
    public int getQueueSize() {
        return workerExecuteQueue.size();
    }

    /**
     * Kill tasks that have not been executed, like delay task
     * then send Response to Master, update the execution status of task instance
     */
    public void killTaskBeforeExecuteByInstanceId(Integer taskInstanceId) {
        workerExecuteQueue.stream()
                .filter(taskExecuteThread -> taskExecuteThread.getTaskExecutionContext().getTaskInstanceId() == taskInstanceId)
                .forEach(workerExecuteQueue::remove);
        sendTaskKillResponse(taskInstanceId);
    }

    /**
     * kill task before execute , like delay task
     */
    private void sendTaskKillResponse(Integer taskInstanceId) {
        TaskExecutionContext taskExecutionContext = taskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId);
        if (taskExecutionContext == null) {
            return;
        }
        TaskExecuteResponseCommand responseCommand = new TaskExecuteResponseCommand(taskExecutionContext.getTaskInstanceId());
        responseCommand.setStatus(ExecutionStatus.KILL.getCode());
        ResponseCache.get().cache(taskExecutionContext.getTaskInstanceId(), responseCommand.convert2Command(), Event.RESULT);
        taskCallbackService.sendResult(taskExecutionContext.getTaskInstanceId(), responseCommand.convert2Command());
    }

    /**
     * submit task
     *
     * @param taskExecuteThread taskExecuteThread
     * @return submit result
     */
    public boolean offer(TaskExecuteThread taskExecuteThread) {
        return workerExecuteQueue.offer(taskExecuteThread);
    }

    public void start() {
        Thread thread = new Thread(this, this.getClass().getName());
        thread.start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Worker-Execute-Manager-Thread");
        TaskExecuteThread taskExecuteThread;
        while (Stopper.isRunning()) {
            try {
                taskExecuteThread = workerExecuteQueue.take();
                workerExecService.submit(taskExecuteThread);
            } catch (Exception e) {
                logger.error("An unexpected interrupt is happened, "
                        + "the exception will be ignored and this thread will continue to run", e);
            }
        }
    }
}
