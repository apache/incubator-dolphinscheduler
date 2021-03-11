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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.ProcessAlertContent;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * alert manager
 */
public class AlertManager {

    /**
     * logger of AlertManager
     */
    private static final Logger logger = LoggerFactory.getLogger(AlertManager.class);

    /**
     * alert dao
     */
    private final AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);

    /**
     * command type convert chinese
     *
     * @param commandType command type
     * @return command name
     */
    private String getCommandCnName(CommandType commandType) {
        switch (commandType) {
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return "recover tolerance fault process";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended process";
            case START_CURRENT_TASK_PROCESS:
                return "start current task process";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task process";
            case START_PROCESS:
                return "start process";
            case REPEAT_RUNNING:
                return "repeat running";
            case SCHEDULER:
                return "scheduler";
            case COMPLEMENT_DATA:
                return "complement data";
            case PAUSE:
                return "pause";
            case STOP:
                return "stop";
            default:
                return "unknown type";
        }
    }

    /**
     * get process instance content
     *
     * @param processInstance process instance
     * @param taskInstances task instance list
     * @return process instance format content
     */
    public String getContentProcessInstance(ProcessInstance processInstance,
                                            List<TaskInstance> taskInstances,
                                            ProjectUser projectUser) {

        String res = "";
        if (processInstance.getState().typeIsSuccess()) {
            List<ProcessAlertContent> successTaskList = new ArrayList<>(1);
            ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                    .projectId(projectUser.getProjectId())
                    .projectName(projectUser.getProjectName())
                    .owner(projectUser.getUserName())
                    .processId(processInstance.getId())
                    .processName(processInstance.getName())
                    .processType(processInstance.getCommandType())
                    .processState(processInstance.getState())
                    .recovery(processInstance.getRecovery())
                    .runTimes(processInstance.getRunTimes())
                    .processStartTime(processInstance.getStartTime())
                    .processEndTime(processInstance.getEndTime())
                    .processHost(processInstance.getHost())
                    .build();
            successTaskList.add(processAlertContent);
            res = JSONUtils.toJsonString(successTaskList);
        } else if (processInstance.getState().typeIsFailure()) {

            List<ProcessAlertContent> failedTaskList = new ArrayList<>();
            for (TaskInstance task : taskInstances) {
                if (task.getState().typeIsSuccess()) {
                    continue;
                }
                ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                        .projectId(projectUser.getProjectId())
                        .projectName(projectUser.getProjectName())
                        .owner(projectUser.getUserName())
                        .processId(processInstance.getId())
                        .processName(processInstance.getName())
                        .taskId(task.getId())
                        .taskName(task.getName())
                        .taskType(task.getTaskType())
                        .taskState(task.getState())
                        .taskStartTime(task.getStartTime())
                        .taskEndTime(task.getEndTime())
                        .taskHost(task.getHost())
                        .logPath(task.getLogPath())
                        .build();
                failedTaskList.add(processAlertContent);
            }
            res = JSONUtils.toJsonString(failedTaskList);
        }

        return res;
    }

    /**
     * getting worker fault tolerant content
     *
     * @param processInstance process instance
     * @param toleranceTaskList tolerance task list
     * @return worker tolerance content
     */
    private String getWorkerToleranceContent(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList) {

        List<ProcessAlertContent> toleranceTaskInstanceList = new ArrayList<>();

        for (TaskInstance taskInstance : toleranceTaskList) {
            ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                    .processName(processInstance.getName())
                    .taskName(taskInstance.getName())
                    .taskHost(taskInstance.getHost())
                    .retryTimes(taskInstance.getRetryTimes())
                    .build();
            toleranceTaskInstanceList.add(processAlertContent);
        }
        return JSONUtils.toJsonString(toleranceTaskInstanceList);
    }

    /**
     * send worker alert fault tolerance
     *
     * @param processInstance process instance
     * @param toleranceTaskList tolerance task list
     */
    public void sendAlertWorkerToleranceFault(ProcessInstance processInstance, List<TaskInstance> toleranceTaskList) {
        try {
            Alert alert = new Alert();
            alert.setTitle("worker fault tolerance");
            String content = getWorkerToleranceContent(processInstance, toleranceTaskList);
            alert.setContent(content);
            alert.setCreateTime(new Date());
            alert.setAlertGroupId(processInstance.getWarningGroupId() == null ? 1 : processInstance.getWarningGroupId());
            alertDao.addAlert(alert);
            logger.info("add alert to db , alert : {}", alert.toString());

        } catch (Exception e) {
            logger.error("send alert failed:{} ", e.getMessage());
        }

    }

    /**
     * send process instance alert
     *
     * @param processInstance process instance
     * @param taskInstances task instance list
     */
    public void sendAlertProcessInstance(ProcessInstance processInstance,
                                         List<TaskInstance> taskInstances,
                                         ProjectUser projectUser) {

        if (Flag.YES == processInstance.getIsSubProcess()) {
            return;
        }
        boolean sendWarning = false;
        WarningType warningType = processInstance.getWarningType();
        switch (warningType) {
            case ALL:
                if (processInstance.getState().typeIsFinished()) {
                    sendWarning = true;
                }
                break;
            case SUCCESS:
                if (processInstance.getState().typeIsSuccess()) {
                    sendWarning = true;
                }
                break;
            case FAILURE:
                if (processInstance.getState().typeIsFailure()) {
                    sendWarning = true;
                }
                break;
            default:
        }
        if (!sendWarning) {
            return;
        }
        Alert alert = new Alert();

        String cmdName = getCommandCnName(processInstance.getCommandType());
        String success = processInstance.getState().typeIsSuccess() ? "success" : "failed";
        alert.setTitle(cmdName + " " + success);
        String content = getContentProcessInstance(processInstance, taskInstances,projectUser);
        alert.setContent(content);
        alert.setAlertGroupId(processInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alertDao.addAlert(alert);
        logger.info("add alert to db , alert: {}", alert.toString());
    }

    /**
     * send process timeout alert
     *
     * @param processInstance process instance
     * @param processDefinition process definition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        alertDao.sendProcessTimeoutAlert(processInstance, processDefinition);
    }
}
