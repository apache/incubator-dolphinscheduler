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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODE_NAMES;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.MAX_TASK_TIMEOUT;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;

import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * executor service impl
 */
@Service
public class ExecutorServiceImpl extends BaseServiceImpl implements ExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private MonitorService monitorService;


    @Autowired
    private ProcessInstanceMapper processInstanceMapper;


    @Autowired
    private ProcessService processService;

    /**
     * execute process instance
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process Definition Id
     * @param cronTime cron time
     * @param commandType command type
     * @param failureStrategy failuer strategy
     * @param startNodeList start nodelist
     * @param taskDependType node dependency type
     * @param warningType warning type
     * @param warningGroupId notify group id
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group name
     * @param runMode run mode
     * @param timeout timeout
     * @param startParams the global param values which pass to new process instance
     * @return execute process instance code
     */
    @Override
    public Result<Void> execProcessInstance(User loginUser, String projectName,
                                            int processDefinitionId, String cronTime, CommandType commandType,
                                            FailureStrategy failureStrategy, String startNodeList,
                                            TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                            RunMode runMode,
                                            Priority processInstancePriority, String workerGroup, Integer timeout,
                                            Map<String, String> startParams) {
        // timeout is invalid
        if (timeout <= 0 || timeout > MAX_TASK_TIMEOUT) {
            return Result.error(Status.TASK_TIMEOUT_PARAMS_ERROR);
        }
        Project project = projectMapper.queryByName(projectName);
        CheckParamResult checkResult = checkResultAndAuth(loginUser, projectName, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check process define release state
        ProcessDefinition processDefinition = processDefinitionMapper.selectById(processDefinitionId);
        Result<Void> result = checkProcessDefinitionValid(processDefinition, processDefinitionId);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        if (!checkTenantSuitable(processDefinition)) {
            logger.error("there is not any valid tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            return Result.error(Status.TENANT_NOT_SUITABLE);
        }

        // check master exists
        checkResult = checkMasterExists();
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        /**
         * create command
         */
        int create = this.createCommand(commandType, processDefinitionId,
                taskDependType, failureStrategy, startNodeList, cronTime, warningType, loginUser.getId(),
                warningGroupId, runMode, processInstancePriority, workerGroup, startParams);

        if (create > 0) {
            processDefinition.setWarningGroupId(warningGroupId);
            processDefinitionMapper.updateById(processDefinition);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.START_PROCESS_INSTANCE_ERROR);
        }
        return result;
    }

    /**
     * check whether master exists
     *
     * @return master exists return true , otherwise return false
     */
    private CheckParamResult checkMasterExists() {
        // check master server exists
        List<Server> masterServers = monitorService.getServerListFromZK(true);

        // no master
        if (masterServers.isEmpty()) {
            return new CheckParamResult(Status.MASTER_NOT_EXISTS);
        }
        return new CheckParamResult(Status.SUCCESS);
    }

    /**
     * check whether the process definition can be executed
     *
     * @param processDefinition process definition
     * @param processDefineId process definition id
     * @return check result code
     */
    @Override
    public Result<Void> checkProcessDefinitionValid(ProcessDefinition processDefinition, int processDefineId) {
        if (processDefinition == null) {
            // check process definition exists
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, processDefineId);
        } else if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            // check process definition online
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_RELEASE, processDefineId);
        } else {
            return Result.success(null);
        }
    }

    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @Override
    public Result<Void> execute(User loginUser, String projectName, Integer processInstanceId, ExecuteType executeType) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = checkResultAndAuth(loginUser, projectName, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check master exists
        checkResult = checkMasterExists();
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
        }

        ProcessDefinition processDefinition = processService.findProcessDefineById(processInstance.getProcessDefinitionId());
        if (executeType != ExecuteType.STOP && executeType != ExecuteType.PAUSE) {
            Result<Void> result = checkProcessDefinitionValid(processDefinition, processInstance.getProcessDefinitionId());
            if (result.getCode() != Status.SUCCESS.getCode()) {
                return result;
            }
        }

        checkResult = checkExecuteType(processInstance, executeType);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }
        if (!checkTenantSuitable(processDefinition)) {
            logger.error("there is not any valid tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            return Result.error(Status.TENANT_NOT_SUITABLE);
        }

        //get the startParams user specified at the first starting while repeat running is needed
        Map<String, Object> commandMap = JSONUtils.toMap(processInstance.getCommandParam(), String.class, Object.class);
        String startParams = null;
        if (MapUtils.isNotEmpty(commandMap) && executeType == ExecuteType.REPEAT_RUNNING) {
            Object startParamsJson = commandMap.get(Constants.CMD_PARAM_START_PARAMS);
            if (startParamsJson != null) {
                startParams = startParamsJson.toString();
            }
        }

        switch (executeType) {
            case REPEAT_RUNNING:
                return insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.REPEAT_RUNNING, startParams);
            case RECOVER_SUSPENDED_PROCESS:
                return insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.RECOVER_SUSPENDED_PROCESS, startParams);
            case START_FAILURE_TASK_PROCESS:
                return insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.START_FAILURE_TASK_PROCESS, startParams);
            case STOP:
                if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                    return Result.errorWithArgs(Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    return updateProcessInstancePrepare(processInstance, CommandType.STOP, ExecutionStatus.READY_STOP);
                }
            case PAUSE:
                if (processInstance.getState() == ExecutionStatus.READY_PAUSE) {
                    return Result.errorWithArgs(Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    return updateProcessInstancePrepare(processInstance, CommandType.PAUSE, ExecutionStatus.READY_PAUSE);
                }
            default:
                logger.error("unknown execute type : {}", executeType);
                return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "unknown execute type");
        }
    }

    /**
     * check tenant suitable
     *
     * @param processDefinition process definition
     * @return true if tenant suitable, otherwise return false
     */
    private boolean checkTenantSuitable(ProcessDefinition processDefinition) {
        Tenant tenant = processService.getTenantForProcess(processDefinition.getTenantId(),
                processDefinition.getUserId());
        return tenant != null;
    }

    /**
     * Check the state of process instance and the type of operation match
     *
     * @param processInstance process instance
     * @param executeType execute type
     * @return check result code
     */
    private CheckParamResult checkExecuteType(ProcessInstance processInstance, ExecuteType executeType) {

        ExecutionStatus executionStatus = processInstance.getState();
        boolean checkResult = false;
        switch (executeType) {
            case PAUSE:
            case STOP:
                if (executionStatus.typeIsRunning()) {
                    checkResult = true;
                }
                break;
            case REPEAT_RUNNING:
                if (executionStatus.typeIsFinished()) {
                    checkResult = true;
                }
                break;
            case START_FAILURE_TASK_PROCESS:
                if (executionStatus.typeIsFailure()) {
                    checkResult = true;
                }
                break;
            case RECOVER_SUSPENDED_PROCESS:
                if (executionStatus.typeIsPause() || executionStatus.typeIsCancel()) {
                    checkResult = true;
                }
                break;
            default:
                break;
        }
        if (!checkResult) {
            return new CheckParamResult(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                    MessageFormat.format(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR.getMsg(),
                            processInstance.getName(), executionStatus.toString(), executeType.toString()));
        } else {
            return new CheckParamResult(Status.SUCCESS);
        }
    }

    /**
     * prepare to update process instance command type and status
     *
     * @param processInstance process instance
     * @param commandType command type
     * @param executionStatus execute status
     * @return update result
     */
    private Result<Void> updateProcessInstancePrepare(ProcessInstance processInstance, CommandType commandType, ExecutionStatus executionStatus) {

        processInstance.setCommandType(commandType);
        processInstance.addHistoryCmd(commandType);
        processInstance.setState(executionStatus);
        int update = processService.updateProcessInstance(processInstance);

        // determine whether the process is normal
        if (update > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }
    }

    /**
     * insert command, used in the implementation of the page, re run, recovery (pause / failure) execution
     *
     * @param loginUser login user
     * @param instanceId instance id
     * @param processDefinitionId process definition id
     * @param commandType command type
     * @return insert result code
     */
    private Result<Void> insertCommand(User loginUser, Integer instanceId, Integer processDefinitionId, CommandType commandType, String startParams) {

        //To add startParams only when repeat running is needed
        Map<String, Object> cmdParam = new HashMap<>();
        cmdParam.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, instanceId);
        if (StringUtils.isNotEmpty(startParams)) {
            cmdParam.put(CMD_PARAM_START_PARAMS, startParams);
        }

        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionId(processDefinitionId);
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(loginUser.getId());

        if (!processService.verifyIsNeedCreateCommand(command)) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_EXECUTING_COMMAND, processDefinitionId);
        }

        int create = processService.createCommand(command);

        if (create > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }
    }

    /**
     * check if sub processes are offline before starting process definition
     *
     * @param processDefineId process definition id
     * @return check result code
     */
    @Override
    public Result<Void> startCheckByProcessDefinedId(int processDefineId) {

        if (processDefineId == 0) {
            logger.error("process definition id is null");
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "process definition id");
        }
        List<Integer> ids = new ArrayList<>();
        processService.recurseFindSubProcessId(processDefineId, ids);
        Integer[] idArray = ids.toArray(new Integer[ids.size()]);
        if (!ids.isEmpty()) {
            List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryDefinitionListByIdList(idArray);
            if (processDefinitionList != null) {
                for (ProcessDefinition processDefinition : processDefinitionList) {
                    /*
                      if there is no online process, exit directly
                     */
                    if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
                        logger.info("not release process definition id: {} , name : {}",
                                processDefinition.getId(), processDefinition.getName());
                        return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_RELEASE, processDefinition.getName());
                    }
                }
            }
        }
        return Result.success(null);
    }

    /**
     * create command
     *
     * @param commandType commandType
     * @param processDefineId processDefineId
     * @param nodeDep nodeDep
     * @param failureStrategy failureStrategy
     * @param startNodeList startNodeList
     * @param schedule schedule
     * @param warningType warningType
     * @param executorId executorId
     * @param warningGroupId warningGroupId
     * @param runMode runMode
     * @param processInstancePriority processInstancePriority
     * @param workerGroup workerGroup
     * @return command id
     */
    private int createCommand(CommandType commandType, int processDefineId,
                              TaskDependType nodeDep, FailureStrategy failureStrategy,
                              String startNodeList, String schedule, WarningType warningType,
                              int executorId, int warningGroupId,
                              RunMode runMode, Priority processInstancePriority, String workerGroup,
                              Map<String, String> startParams) {

        /**
         * instantiate command schedule instance
         */
        Command command = new Command();

        Map<String, String> cmdParam = new HashMap<>();
        if (commandType == null) {
            command.setCommandType(CommandType.START_PROCESS);
        } else {
            command.setCommandType(commandType);
        }
        command.setProcessDefinitionId(processDefineId);
        if (nodeDep != null) {
            command.setTaskDependType(nodeDep);
        }
        if (failureStrategy != null) {
            command.setFailureStrategy(failureStrategy);
        }

        if (StringUtils.isNotEmpty(startNodeList)) {
            cmdParam.put(CMD_PARAM_START_NODE_NAMES, startNodeList);
        }
        if (warningType != null) {
            command.setWarningType(warningType);
        }
        if (startParams != null && startParams.size() > 0) {
            cmdParam.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        }
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(executorId);
        command.setWarningGroupId(warningGroupId);
        command.setProcessInstancePriority(processInstancePriority);
        command.setWorkerGroup(workerGroup);

        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(schedule)) {
            String[] interval = schedule.split(",");
            if (interval.length == 2) {
                start = DateUtils.getScheduleDate(interval[0]);
                end = DateUtils.getScheduleDate(interval[1]);
            }
        }

        // determine whether to complement
        if (commandType == CommandType.COMPLEMENT_DATA) {
            runMode = (runMode == null) ? RunMode.RUN_MODE_SERIAL : runMode;
            if (null != start && null != end && !start.after(end)) {
                if (runMode == RunMode.RUN_MODE_SERIAL) {
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(start));
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(end));
                    command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    return processService.createCommand(command);
                } else if (runMode == RunMode.RUN_MODE_PARALLEL) {
                    List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionId(processDefineId);
                    List<Date> listDate = new LinkedList<>();
                    if (!CollectionUtils.isEmpty(schedules)) {
                        for (Schedule item : schedules) {
                            listDate.addAll(CronUtils.getSelfFireDateList(start, end, item.getCrontab()));
                        }
                    }
                    if (!CollectionUtils.isEmpty(listDate)) {
                        // loop by schedule date
                        for (Date date : listDate) {
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(date));
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(date));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            processService.createCommand(command);
                        }
                        return listDate.size();
                    } else {
                        // loop by day
                        int runCunt = 0;
                        while (!start.after(end)) {
                            runCunt += 1;
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(start));
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(start));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            processService.createCommand(command);
                            start = DateUtils.getSomeDay(start, 1);
                        }
                        return runCunt;
                    }
                }
            } else {
                logger.error("there is not valid schedule date for the process definition: id:{}", processDefineId);
            }
        } else {
            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
            return processService.createCommand(command);
        }

        return 0;
    }

    /**
     * check result and auth
     */
    private CheckParamResult checkResultAndAuth(User loginUser, String projectName, Project project) {
        // check project auth
        return projectService.checkProjectAndAuth(loginUser, project, projectName);
    }

}
