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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.queue.TaskPriority;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueue;
import org.apache.dolphinscheduler.service.queue.TaskPriorityQueueImpl;

import java.util.Date;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * master task exec base class
 */
public class MasterBaseTaskExecThread implements Callable<Boolean> {

    /**
     * logger of MasterBaseTaskExecThread
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * process service
     */
    protected ProcessService processService;

    /**
     * alert database access
     */
    protected AlertDao alertDao;

    /**
     * process instance
     */
    protected ProcessInstance processInstance;

    /**
     * task instance
     */
    protected TaskInstance taskInstance;

    /**
     * whether need cancel
     */
    protected boolean cancel;

    /**
     * master config
     */
    protected MasterConfig masterConfig;

    /**
     * taskUpdateQueue
     */
    private TaskPriorityQueue taskUpdateQueue;

    /**
     * whether need check task time out.
     */
    protected boolean checkTimeoutFlag = false;

    /**
     * task timeout parameters
     */
    protected TaskTimeoutParameter taskTimeoutParameter;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance task instance
     */
    public MasterBaseTaskExecThread(TaskInstance taskInstance) {
        this.processService = SpringApplicationContext.getBean(ProcessService.class);
        this.alertDao = SpringApplicationContext.getBean(AlertDao.class);
        this.cancel = false;
        this.taskInstance = taskInstance;
        this.masterConfig = SpringApplicationContext.getBean(MasterConfig.class);
        this.taskUpdateQueue = SpringApplicationContext.getBean(TaskPriorityQueueImpl.class);
        initTaskParams();
    }

    /**
     * init task ordinary parameters
     */
    private void initTaskParams() {
        initTimeoutParams();
    }

    /**
     * init task timeout parameters
     */
    private void initTimeoutParams() {
        String taskJson = taskInstance.getTaskJson();
        TaskNode taskNode = JSONUtils.parseObject(taskJson, TaskNode.class);
        taskTimeoutParameter = taskNode.getTaskTimeoutParameter();

        if (taskTimeoutParameter.getEnable()) {
            checkTimeoutFlag = true;
        }
    }

    /**
     * get task instance
     *
     * @return TaskInstance
     */
    public TaskInstance getTaskInstance() {
        return this.taskInstance;
    }

    /**
     * kill master base task exec thread
     */
    public void kill() {
        this.cancel = true;
    }

    /**
     * submit master base task exec thread
     *
     * @return TaskInstance
     */
    protected TaskInstance submit() {
        Integer commitRetryTimes = masterConfig.getMasterTaskCommitRetryTimes();
        Integer commitRetryInterval = masterConfig.getMasterTaskCommitInterval();

        int retryTimes = 1;
        boolean submitDB = false;
        boolean submitTask = false;
        TaskInstance task = null;
        while (retryTimes <= commitRetryTimes) {
            try {
                if (!submitDB) {
                    // submit task to db
                    task = processService.submitTask(taskInstance);
                    if (task != null && task.getId() != 0) {
                        submitDB = true;
                    }
                }
                if (submitDB && !submitTask) {
                    // dispatch task
                    submitTask = dispatchTask(task);
                }
                if (submitDB && submitTask) {
                    return task;
                }
                if (!submitDB) {
                    logger.error("task commit to db failed , taskId {} has already retry {} times, please check the database", taskInstance.getId(), retryTimes);
                } else if (!submitTask) {
                    logger.error("task commit  failed , taskId {} has already retry {} times, please check", taskInstance.getId(), retryTimes);
                }
                Thread.sleep(commitRetryInterval);
            } catch (Exception e) {
                logger.error("task commit to mysql and dispatch task failed", e);
            }
            retryTimes += 1;
        }
        return task;
    }

    /**
     * dispatch task
     *
     * @param taskInstance taskInstance
     * @return whether submit task success
     */
    public Boolean dispatchTask(TaskInstance taskInstance) {

        try {
            if (taskInstance.isConditionsTask()
                    || taskInstance.isDependTask()
                    || taskInstance.isSubProcess()) {
                return true;
            }
            if (taskInstance.getState().typeIsFinished()) {
                logger.info(String.format("submit task , but task [%s] state [%s] is already  finished. ", taskInstance.getName(), taskInstance.getState().toString()));
                return true;
            }
            // task cannot be submitted because its execution state is RUNNING or DELAY.
            if (taskInstance.getState() == ExecutionStatus.RUNNING_EXECUTION
                    || taskInstance.getState() == ExecutionStatus.DELAY_EXECUTION) {
                logger.info("submit task, but the status of the task {} is already running or delayed.", taskInstance.getName());
                return true;
            }
            logger.info("task ready to submit: {}", taskInstance);

            /**
             *  taskPriority
             */
            TaskPriority taskPriority = buildTaskPriority(processInstance.getProcessInstancePriority().getCode(),
                    processInstance.getId(),
                    taskInstance.getProcessInstancePriority().getCode(),
                    taskInstance.getId(),
                    org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP);
            taskUpdateQueue.put(taskPriority);
            logger.info(String.format("master submit success, task : %s", taskInstance.getName()));
            return true;
        } catch (Exception e) {
            logger.error("submit task  Exception: ", e);
            logger.error("task error : %s", JSONUtils.toJsonString(taskInstance));
            return false;
        }
    }

    /**
     * buildTaskPriority
     *
     * @param processInstancePriority processInstancePriority
     * @param processInstanceId processInstanceId
     * @param taskInstancePriority taskInstancePriority
     * @param taskInstanceId taskInstanceId
     * @param workerGroup workerGroup
     * @return TaskPriority
     */
    private TaskPriority buildTaskPriority(int processInstancePriority,
                                           int processInstanceId,
                                           int taskInstancePriority,
                                           int taskInstanceId,
                                           String workerGroup) {
        return new TaskPriority(processInstancePriority, processInstanceId,
                taskInstancePriority, taskInstanceId, workerGroup);
    }

    /**
     * submit wait complete
     *
     * @return true
     */
    protected Boolean submitWaitComplete() {
        return true;
    }

    /**
     * call
     *
     * @return boolean
     * @throws Exception exception
     */
    @Override
    public Boolean call() throws Exception {
        this.processInstance = processService.findProcessInstanceById(taskInstance.getProcessInstanceId());
        return submitWaitComplete();
    }

    /**
     * alert time out
     */
    protected boolean alertTimeout() {
        if (TaskTimeoutStrategy.FAILED == this.taskTimeoutParameter.getStrategy()) {
            return true;
        }
        logger.warn("process id:{} process name:{} task id: {},name:{} execution time out",
                processInstance.getId(), processInstance.getName(), taskInstance.getId(), taskInstance.getName());
        // send warn mail
        ProcessDefinition processDefine = processService.findProcessDefineById(processInstance.getProcessDefinitionId());
        alertDao.sendTaskTimeoutAlert(processInstance.getWarningGroupId(), processInstance.getId(), processInstance.getName(),
                taskInstance.getId(), taskInstance.getName());
        return true;
    }

    /**
     * handle time out for time out strategy warn&&failed
     */
    protected void handleTimeoutFailed() {
        if (TaskTimeoutStrategy.WARN == this.taskTimeoutParameter.getStrategy()) {
            return;
        }
        logger.info("process id:{} name:{} task id:{} name:{} cancel because of timeout.",
                processInstance.getId(), processInstance.getName(), taskInstance.getId(), taskInstance.getName());
        this.cancel = true;
    }

    /**
     * check task remain time valid
     */
    protected boolean checkTaskTimeout() {
        if (!checkTimeoutFlag || taskInstance.getStartTime() == null) {
            return false;
        }
        long remainTime = getRemainTime(taskTimeoutParameter.getInterval() * 60L);
        return remainTime <= 0;
    }

    /**
     * get remain time
     *
     * @return remain time
     */
    protected long getRemainTime(long timeoutSeconds) {
        Date startTime = taskInstance.getStartTime();
        long usedTime = (System.currentTimeMillis() - startTime.getTime()) / 1000;
        return timeoutSeconds - usedTime;
    }
}
