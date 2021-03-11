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

package org.apache.dolphinscheduler.service.process;

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_EMPTY_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID;
import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.YYYY_MM_DD_HH_MM_SS;

import static java.util.stream.Collectors.toSet;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.DateInterval;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.task.subprocess.SubProcessParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CycleDependency;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cronutils.model.Cron;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * process relative dao that some mappers in this.
 */
@Component
public class ProcessService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int[] stateArray = new int[]{ExecutionStatus.SUBMITTED_SUCCESS.ordinal(),
            ExecutionStatus.RUNNING_EXECUTION.ordinal(),
            ExecutionStatus.DELAY_EXECUTION.ordinal(),
            ExecutionStatus.READY_PAUSE.ordinal(),
            ExecutionStatus.READY_STOP.ordinal()};

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private ProcessInstanceMapMapper processInstanceMapMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * handle Command (construct ProcessInstance from Command) , wrapped in transaction
     *
     * @param logger logger
     * @param host host
     * @param validThreadNum validThreadNum
     * @param command found command
     * @return process instance
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance handleCommand(Logger logger, String host, int validThreadNum, Command command) {
        ProcessInstance processInstance = constructProcessInstance(command, host);
        // cannot construct process instance, return null
        if (processInstance == null) {
            logger.error("scan command, command parameter is error: {}", command);
            moveToErrorCommand(command, "process instance is null");
            return null;
        }
        if (!checkThreadNum(command, validThreadNum)) {
            logger.info("there is not enough thread for this command: {}", command);
            return setWaitingThreadProcess(command, processInstance);
        }
        processInstance.setCommandType(command.getCommandType());
        processInstance.addHistoryCmd(command.getCommandType());
        saveProcessInstance(processInstance);
        this.setSubProcessParam(processInstance);
        delCommandById(command.getId());
        return processInstance;
    }

    /**
     * save error command, and delete original command
     *
     * @param command command
     * @param message message
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveToErrorCommand(Command command, String message) {
        ErrorCommand errorCommand = new ErrorCommand(command, message);
        this.errorCommandMapper.insert(errorCommand);
        delCommandById(command.getId());
    }

    /**
     * set process waiting thread
     *
     * @param command command
     * @param processInstance processInstance
     * @return process instance
     */
    private ProcessInstance setWaitingThreadProcess(Command command, ProcessInstance processInstance) {
        processInstance.setState(ExecutionStatus.WAITING_THREAD);
        if (command.getCommandType() != CommandType.RECOVER_WAITING_THREAD) {
            processInstance.addHistoryCmd(command.getCommandType());
        }
        saveProcessInstance(processInstance);
        this.setSubProcessParam(processInstance);
        createRecoveryWaitingThreadCommand(command, processInstance);
        return null;
    }

    /**
     * check thread num
     *
     * @param command command
     * @param validThreadNum validThreadNum
     * @return if thread is enough
     */
    private boolean checkThreadNum(Command command, int validThreadNum) {
        int commandThreadCount = this.workProcessThreadNumCount(command.getProcessDefinitionId());
        return validThreadNum >= commandThreadCount;
    }

    /**
     * insert one command
     *
     * @param command command
     * @return create result
     */
    public int createCommand(Command command) {
        int result = 0;
        if (command != null) {
            result = commandMapper.insert(command);
        }
        return result;
    }

    /**
     * find one command from queue list
     *
     * @return command
     */
    public Command findOneCommand() {
        return commandMapper.getOneToRun();
    }

    /**
     * check the input command exists in queue list
     *
     * @param command command
     * @return create command result
     */
    public boolean verifyIsNeedCreateCommand(Command command) {
        boolean isNeedCreate = true;
        EnumMap<CommandType, Integer> cmdTypeMap = new EnumMap<>(CommandType.class);
        cmdTypeMap.put(CommandType.REPEAT_RUNNING, 1);
        cmdTypeMap.put(CommandType.RECOVER_SUSPENDED_PROCESS, 1);
        cmdTypeMap.put(CommandType.START_FAILURE_TASK_PROCESS, 1);
        CommandType commandType = command.getCommandType();

        if (cmdTypeMap.containsKey(commandType)) {
            ObjectNode cmdParamObj = JSONUtils.parseObject(command.getCommandParam());
            int processInstanceId = cmdParamObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt();

            List<Command> commands = commandMapper.selectList(null);
            // for all commands
            for (Command tmpCommand : commands) {
                if (cmdTypeMap.containsKey(tmpCommand.getCommandType())) {
                    ObjectNode tempObj = JSONUtils.parseObject(tmpCommand.getCommandParam());
                    if (tempObj != null && processInstanceId == tempObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt()) {
                        isNeedCreate = false;
                        break;
                    }
                }
            }
        }
        return isNeedCreate;
    }

    /**
     * find process instance detail by id
     *
     * @param processId processId
     * @return process instance
     */
    public ProcessInstance findProcessInstanceDetailById(int processId) {
        return processInstanceMapper.queryDetailById(processId);
    }

    /**
     * get task node list by definitionId
     */
    public List<TaskNode> getTaskNodeListByDefinitionId(Integer defineId) {
        ProcessDefinition processDefinition = processDefineMapper.selectById(defineId);
        if (processDefinition == null) {
            logger.info("process define not exists");
            return null;
        }

        String processDefinitionJson = processDefinition.getProcessDefinitionJson();
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        //process data check
        if (null == processData) {
            logger.error("process data is null");
            return new ArrayList<>();
        }

        return processData.getTasks();
    }

    /**
     * find process instance by id
     *
     * @param processId processId
     * @return process instance
     */
    public ProcessInstance findProcessInstanceById(int processId) {
        return processInstanceMapper.selectById(processId);
    }

    /**
     * find process define by id.
     *
     * @param processDefinitionId processDefinitionId
     * @return process definition
     */
    public ProcessDefinition findProcessDefineById(int processDefinitionId) {
        return processDefineMapper.selectById(processDefinitionId);
    }

    /**
     * delete work process instance by id
     *
     * @param processInstanceId processInstanceId
     * @return delete process instance result
     */
    public int deleteWorkProcessInstanceById(int processInstanceId) {
        return processInstanceMapper.deleteById(processInstanceId);
    }

    /**
     * delete all sub process by parent instance id
     *
     * @param processInstanceId processInstanceId
     * @return delete all sub process instance result
     */
    public int deleteAllSubWorkProcessByParentId(int processInstanceId) {

        List<Integer> subProcessIdList = processInstanceMapMapper.querySubIdListByParentId(processInstanceId);

        for (Integer subId : subProcessIdList) {
            deleteAllSubWorkProcessByParentId(subId);
            deleteWorkProcessMapByParentId(subId);
            removeTaskLogFile(subId);
            deleteWorkProcessInstanceById(subId);
        }
        return 1;
    }

    /**
     * remove task log file
     *
     * @param processInstanceId processInstanceId
     */
    public void removeTaskLogFile(Integer processInstanceId) {

        LogClientService logClient = null;

        try {
            logClient = new LogClientService();
            List<TaskInstance> taskInstanceList = findValidTaskListByProcessId(processInstanceId);

            if (CollectionUtils.isEmpty(taskInstanceList)) {
                return;
            }

            for (TaskInstance taskInstance : taskInstanceList) {
                String taskLogPath = taskInstance.getLogPath();
                if (StringUtils.isEmpty(taskInstance.getHost())) {
                    continue;
                }
                int port = Constants.RPC_PORT;
                String ip = "";
                try {
                    ip = Host.of(taskInstance.getHost()).getIp();
                } catch (Exception e) {
                    // compatible old version
                    ip = taskInstance.getHost();
                }

                // remove task log from loggerserver
                logClient.removeTaskLog(ip, port, taskLogPath);
            }
        } finally {
            if (logClient != null) {
                logClient.close();
            }
        }
    }

    /**
     * calculate sub process number in the process define.
     *
     * @param processDefinitionId processDefinitionId
     * @return process thread num count
     */
    private Integer workProcessThreadNumCount(Integer processDefinitionId) {
        List<Integer> ids = new ArrayList<>();
        recurseFindSubProcessId(processDefinitionId, ids);
        return ids.size() + 1;
    }

    /**
     * recursive query sub process definition id by parent id.
     *
     * @param parentId parentId
     * @param ids      ids
     */
    public void recurseFindSubProcessId(int parentId, List<Integer> ids) {
        ProcessDefinition processDefinition = processDefineMapper.selectById(parentId);
        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = processData.getTasks();

        if (taskNodeList != null && !taskNodeList.isEmpty()) {

            for (TaskNode taskNode : taskNodeList) {
                String parameter = taskNode.getParams();
                ObjectNode parameterJson = JSONUtils.parseObject(parameter);
                if (parameterJson.get(CMD_PARAM_SUB_PROCESS_DEFINE_ID) != null) {
                    SubProcessParameters subProcessParam = JSONUtils.parseObject(parameter, SubProcessParameters.class);
                    ids.add(subProcessParam.getProcessDefinitionId());
                    recurseFindSubProcessId(subProcessParam.getProcessDefinitionId(), ids);
                }

            }
        }
    }

    /**
     * create recovery waiting thread command when thread pool is not enough for the process instance.
     * sub work process instance need not to create recovery command.
     * create recovery waiting thread  command and delete origin command at the same time.
     * if the recovery command is exists, only update the field update_time
     *
     * @param originCommand   originCommand
     * @param processInstance processInstance
     */
    public void createRecoveryWaitingThreadCommand(Command originCommand, ProcessInstance processInstance) {

        // sub process does not need to create wait command
        if (processInstance.getIsSubProcess() == Flag.YES) {
            if (originCommand != null) {
                commandMapper.deleteById(originCommand.getId());
            }
            return;
        }
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD, String.valueOf(processInstance.getId()));
        // process instance quit by "waiting thread" state
        if (originCommand == null) {
            Command command = new Command(
                    CommandType.RECOVER_WAITING_THREAD,
                    processInstance.getTaskDependType(),
                    processInstance.getFailureStrategy(),
                    processInstance.getExecutorId(),
                    processInstance.getProcessDefinitionId(),
                    JSONUtils.toJsonString(cmdParam),
                    processInstance.getWarningType(),
                    processInstance.getWarningGroupId(),
                    processInstance.getScheduleTime(),
                    processInstance.getWorkerGroup(),
                    processInstance.getProcessInstancePriority()
            );
            saveCommand(command);
            return;
        }

        // update the command time if current command if recover from waiting
        if (originCommand.getCommandType() == CommandType.RECOVER_WAITING_THREAD) {
            originCommand.setUpdateTime(new Date());
            saveCommand(originCommand);
        } else {
            // delete old command and create new waiting thread command
            commandMapper.deleteById(originCommand.getId());
            originCommand.setId(0);
            originCommand.setCommandType(CommandType.RECOVER_WAITING_THREAD);
            originCommand.setUpdateTime(new Date());
            originCommand.setCommandParam(JSONUtils.toJsonString(cmdParam));
            originCommand.setProcessInstancePriority(processInstance.getProcessInstancePriority());
            saveCommand(originCommand);
        }
    }

    /**
     * get schedule time from command
     *
     * @param command  command
     * @param cmdParam cmdParam map
     * @return date
     */
    private Date getScheduleTime(Command command, Map<String, String> cmdParam) {
        Date scheduleTime = command.getScheduleTime();
        if (scheduleTime == null && cmdParam != null && cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)) {
            scheduleTime = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
        }
        return scheduleTime;
    }

    /**
     * generate a new work process instance from command.
     *
     * @param processDefinition processDefinition
     * @param command           command
     * @param cmdParam          cmdParam map
     * @return process instance
     */
    private ProcessInstance generateNewProcessInstance(ProcessDefinition processDefinition,
                                                       Command command,
                                                       Map<String, String> cmdParam) {
        ProcessInstance processInstance = new ProcessInstance(processDefinition);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setRecovery(Flag.NO);
        processInstance.setStartTime(new Date());
        processInstance.setRunTimes(1);
        processInstance.setMaxTryTimes(0);
        processInstance.setProcessDefinitionId(command.getProcessDefinitionId());
        processInstance.setCommandParam(command.getCommandParam());
        processInstance.setCommandType(command.getCommandType());
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setTaskDependType(command.getTaskDependType());
        processInstance.setFailureStrategy(command.getFailureStrategy());
        processInstance.setExecutorId(command.getExecutorId());
        WarningType warningType = command.getWarningType() == null ? WarningType.NONE : command.getWarningType();
        processInstance.setWarningType(warningType);
        Integer warningGroupId = command.getWarningGroupId() == null ? 0 : command.getWarningGroupId();
        processInstance.setWarningGroupId(warningGroupId);

        // schedule time
        Date scheduleTime = getScheduleTime(command, cmdParam);
        if (scheduleTime != null) {
            processInstance.setScheduleTime(scheduleTime);
        }
        processInstance.setCommandStartTime(command.getStartTime());
        processInstance.setLocations(processDefinition.getLocations());
        processInstance.setConnects(processDefinition.getConnects());

        // reset global params while there are start parameters
        setGlobalParamIfCommanded(processDefinition,cmdParam);

        // curing global params
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                getCommandTypeIfComplement(processInstance, command),
                processInstance.getScheduleTime()));

        //copy process define json to process instance
        processInstance.setProcessInstanceJson(processDefinition.getProcessDefinitionJson());
        // set process instance priority
        processInstance.setProcessInstancePriority(command.getProcessInstancePriority());
        String workerGroup = StringUtils.isBlank(command.getWorkerGroup()) ? Constants.DEFAULT_WORKER_GROUP : command.getWorkerGroup();
        processInstance.setWorkerGroup(workerGroup);
        processInstance.setTimeout(processDefinition.getTimeout());
        processInstance.setTenantId(processDefinition.getTenantId());
        return processInstance;
    }

    private void setGlobalParamIfCommanded(ProcessDefinition processDefinition, Map<String, String> cmdParam) {
        // get start params from command param
        Map<String, String> startParamMap = null;
        if (cmdParam != null && cmdParam.containsKey(Constants.CMD_PARAM_START_PARAMS)) {
            String startParamJson = cmdParam.get(Constants.CMD_PARAM_START_PARAMS);
            startParamMap = JSONUtils.toMap(startParamJson);
        }

        // set start param into global params
        if (startParamMap != null && startParamMap.size() > 0
                && processDefinition.getGlobalParamMap() != null) {
            for (Map.Entry<String, String> param : processDefinition.getGlobalParamMap().entrySet()) {
                String val = startParamMap.get(param.getKey());
                if (val != null) {
                    param.setValue(val);
                }
            }
        }
    }

    /**
     * get process tenant
     * there is tenant id in definition, use the tenant of the definition.
     * if there is not tenant id in the definition or the tenant not exist
     * use definition creator's tenant.
     *
     * @param tenantId tenantId
     * @param userId   userId
     * @return tenant
     */
    public Tenant getTenantForProcess(int tenantId, int userId) {
        Tenant tenant = null;
        if (tenantId >= 0) {
            tenant = tenantMapper.queryById(tenantId);
        }

        if (userId == 0) {
            return null;
        }

        if (tenant == null) {
            User user = userMapper.selectById(userId);
            tenant = tenantMapper.queryById(user.getTenantId());
        }
        return tenant;
    }

    /**
     * check command parameters is valid
     *
     * @param command  command
     * @param cmdParam cmdParam map
     * @return whether command param is valid
     */
    private Boolean checkCmdParam(Command command, Map<String, String> cmdParam) {
        if (command.getTaskDependType() == TaskDependType.TASK_ONLY || command.getTaskDependType() == TaskDependType.TASK_PRE) {
            if (cmdParam == null
                    || !cmdParam.containsKey(Constants.CMD_PARAM_START_NODE_NAMES)
                    || cmdParam.get(Constants.CMD_PARAM_START_NODE_NAMES).isEmpty()) {
                logger.error("command node depend type is {}, but start nodes is null ", command.getTaskDependType());
                return false;
            }
        }
        return true;
    }

    /**
     * construct process instance according to one command.
     *
     * @param command command
     * @param host    host
     * @return process instance
     */
    private ProcessInstance constructProcessInstance(Command command, String host) {

        ProcessInstance processInstance = null;
        CommandType commandType = command.getCommandType();
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());

        ProcessDefinition processDefinition = null;
        if (command.getProcessDefinitionId() != 0) {
            processDefinition = processDefineMapper.selectById(command.getProcessDefinitionId());
            if (processDefinition == null) {
                logger.error("cannot find the work process define! define id : {}", command.getProcessDefinitionId());
                return null;
            }
        }

        if (cmdParam != null) {
            Integer processInstanceId = 0;
            // recover from failure or pause tasks
            if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING)) {
                String processId = cmdParam.get(Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING);
                processInstanceId = Integer.parseInt(processId);
                if (processInstanceId == 0) {
                    logger.error("command parameter is error, [ ProcessInstanceId ] is 0");
                    return null;
                }
            } else if (cmdParam.containsKey(Constants.CMD_PARAM_SUB_PROCESS)) {
                // sub process map
                String pId = cmdParam.get(Constants.CMD_PARAM_SUB_PROCESS);
                processInstanceId = Integer.parseInt(pId);
            } else if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD)) {
                // waiting thread command
                String pId = cmdParam.get(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD);
                processInstanceId = Integer.parseInt(pId);
            }
            if (processInstanceId == 0) {
                processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
            } else {
                processInstance = this.findProcessInstanceDetailById(processInstanceId);
                CommandType commandTypeIfComplement = getCommandTypeIfComplement(processInstance, command);

                // reset global params while repeat running is needed by cmdParam
                if (commandTypeIfComplement == CommandType.REPEAT_RUNNING) {
                    setGlobalParamIfCommanded(processDefinition, cmdParam);
                }

                // Recalculate global parameters after rerun.

                processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                    processDefinition.getGlobalParamMap(),
                    processDefinition.getGlobalParamList(),
                    commandTypeIfComplement,
                    processInstance.getScheduleTime()));
            }
            processDefinition = processDefineMapper.selectById(processInstance.getProcessDefinitionId());
            processInstance.setProcessDefinition(processDefinition);

            //reset command parameter
            if (processInstance.getCommandParam() != null) {
                Map<String, String> processCmdParam = JSONUtils.toMap(processInstance.getCommandParam());
                for (Map.Entry<String, String> entry : processCmdParam.entrySet()) {
                    if (!cmdParam.containsKey(entry.getKey())) {
                        cmdParam.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            // reset command parameter if sub process
            if (cmdParam.containsKey(Constants.CMD_PARAM_SUB_PROCESS)) {
                processInstance.setCommandParam(command.getCommandParam());
            }
        } else {
            // generate one new process instance
            processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
        }
        if (Boolean.FALSE.equals(checkCmdParam(command, cmdParam))) {
            logger.error("command parameter check failed!");
            return null;
        }

        if (command.getScheduleTime() != null) {
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setHost(host);

        ExecutionStatus runStatus = ExecutionStatus.RUNNING_EXECUTION;
        int runTime = processInstance.getRunTimes();
        switch (commandType) {
            case START_PROCESS:
                break;
            case START_FAILURE_TASK_PROCESS:
                // find failed tasks and init these tasks
                List<Integer> failedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.FAILURE);
                List<Integer> toleranceList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.NEED_FAULT_TOLERANCE);
                List<Integer> killedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.KILL);
                cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);

                failedList.addAll(killedList);
                failedList.addAll(toleranceList);
                for (Integer taskId : failedList) {
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING,
                        String.join(Constants.COMMA, convertIntListToString(failedList)));
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                processInstance.setRunTimes(runTime + 1);
                break;
            case START_CURRENT_TASK_PROCESS:
                break;
            case RECOVER_WAITING_THREAD:
                break;
            case RECOVER_SUSPENDED_PROCESS:
                // find pause tasks and init task's state
                cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                List<Integer> suspendedNodeList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.PAUSE);
                List<Integer> stopNodeList = findTaskIdByInstanceState(processInstance.getId(),
                        ExecutionStatus.KILL);
                suspendedNodeList.addAll(stopNodeList);
                for (Integer taskId : suspendedNodeList) {
                    // initialize the pause state
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING, String.join(",", convertIntListToString(suspendedNodeList)));
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                processInstance.setRunTimes(runTime + 1);
                break;
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                // recover tolerance fault process
                processInstance.setRecovery(Flag.YES);
                runStatus = processInstance.getState();
                break;
            case COMPLEMENT_DATA:
                // delete all the valid tasks when complement data
                List<TaskInstance> taskInstanceList = this.findValidTaskListByProcessId(processInstance.getId());
                for (TaskInstance taskInstance : taskInstanceList) {
                    taskInstance.setFlag(Flag.NO);
                    this.updateTaskInstance(taskInstance);
                }
                initComplementDataParam(processDefinition, processInstance, cmdParam);
                break;
            case REPEAT_RUNNING:
                // delete the recover task names from command parameter
                if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
                    cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                    processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                }
                // delete all the valid tasks when repeat running
                List<TaskInstance> validTaskList = findValidTaskListByProcessId(processInstance.getId());
                for (TaskInstance taskInstance : validTaskList) {
                    taskInstance.setFlag(Flag.NO);
                    updateTaskInstance(taskInstance);
                }
                processInstance.setStartTime(new Date());
                processInstance.setEndTime(null);
                processInstance.setRunTimes(runTime + 1);
                initComplementDataParam(processDefinition, processInstance, cmdParam);
                break;
            case SCHEDULER:
                break;
            default:
                break;
        }
        processInstance.setState(runStatus);
        return processInstance;
    }

    /**
     * return complement data if the process start with complement data
     *
     * @param processInstance processInstance
     * @param command         command
     * @return command type
     */
    private CommandType getCommandTypeIfComplement(ProcessInstance processInstance, Command command) {
        if (CommandType.COMPLEMENT_DATA == processInstance.getCmdTypeIfComplement()) {
            return CommandType.COMPLEMENT_DATA;
        } else {
            return command.getCommandType();
        }
    }

    /**
     * initialize complement data parameters
     *
     * @param processDefinition processDefinition
     * @param processInstance   processInstance
     * @param cmdParam          cmdParam
     */
    private void initComplementDataParam(ProcessDefinition processDefinition,
                                         ProcessInstance processInstance,
                                         Map<String, String> cmdParam) {
        if (!processInstance.isComplementData()) {
            return;
        }

        Date startComplementTime = DateUtils.parse(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE),
                YYYY_MM_DD_HH_MM_SS);
        if (Flag.NO == processInstance.getIsSubProcess()) {
            processInstance.setScheduleTime(startComplementTime);
        }
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));

    }

    /**
     * set sub work process parameters.
     * handle sub work process instance, update relation table and command parameters
     * set sub work process flag, extends parent work process command parameters
     *
     * @param subProcessInstance subProcessInstance
     * @return process instance
     */
    public ProcessInstance setSubProcessParam(ProcessInstance subProcessInstance) {
        String cmdParam = subProcessInstance.getCommandParam();
        if (StringUtils.isEmpty(cmdParam)) {
            return subProcessInstance;
        }
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        // write sub process id into cmd param.
        if (paramMap.containsKey(CMD_PARAM_SUB_PROCESS)
                && CMD_PARAM_EMPTY_SUB_PROCESS.equals(paramMap.get(CMD_PARAM_SUB_PROCESS))) {
            paramMap.remove(CMD_PARAM_SUB_PROCESS);
            paramMap.put(CMD_PARAM_SUB_PROCESS, String.valueOf(subProcessInstance.getId()));
            subProcessInstance.setCommandParam(JSONUtils.toJsonString(paramMap));
            subProcessInstance.setIsSubProcess(Flag.YES);
            this.saveProcessInstance(subProcessInstance);
        }
        // copy parent instance user def params to sub process..
        String parentInstanceId = paramMap.get(CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID);
        if (StringUtils.isNotEmpty(parentInstanceId)) {
            ProcessInstance parentInstance = findProcessInstanceDetailById(Integer.parseInt(parentInstanceId));
            if (parentInstance != null) {
                subProcessInstance.setGlobalParams(
                        joinGlobalParams(parentInstance.getGlobalParams(), subProcessInstance.getGlobalParams()));
                this.saveProcessInstance(subProcessInstance);
            } else {
                logger.error("sub process command params error, cannot find parent instance: {} ", cmdParam);
            }
        }
        ProcessInstanceMap processInstanceMap = JSONUtils.parseObject(cmdParam, ProcessInstanceMap.class);
        if (processInstanceMap == null || processInstanceMap.getParentProcessInstanceId() == 0) {
            return subProcessInstance;
        }
        // update sub process id to process map table
        processInstanceMap.setProcessInstanceId(subProcessInstance.getId());

        this.updateWorkProcessInstanceMap(processInstanceMap);
        return subProcessInstance;
    }

    /**
     * join parent global params into sub process.
     * only the keys doesn't in sub process global would be joined.
     *
     * @param parentGlobalParams parentGlobalParams
     * @param subGlobalParams    subGlobalParams
     * @return global params join
     */
    private String joinGlobalParams(String parentGlobalParams, String subGlobalParams) {

        List<Property> parentPropertyList = JSONUtils.toList(parentGlobalParams, Property.class);
        List<Property> subPropertyList = JSONUtils.toList(subGlobalParams, Property.class);

        Map<String, String> subMap = subPropertyList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));

        for (Property parent : parentPropertyList) {
            if (!subMap.containsKey(parent.getProp())) {
                subPropertyList.add(parent);
            }
        }
        return JSONUtils.toJsonString(subPropertyList);
    }

    /**
     * initialize task instance
     *
     * @param taskInstance taskInstance
     */
    private void initTaskInstance(TaskInstance taskInstance) {

        if (!taskInstance.isSubProcess()
                && (taskInstance.getState().typeIsCancel() || taskInstance.getState().typeIsFailure())) {
            taskInstance.setFlag(Flag.NO);
            updateTaskInstance(taskInstance);
            return;
        }
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        updateTaskInstance(taskInstance);
    }

    /**
     * submit task to db
     * submit sub process to command
     *
     * @param taskInstance taskInstance
     * @return task instance
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskInstance submitTask(TaskInstance taskInstance) {
        ProcessInstance processInstance = this.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
        logger.info("start submit task : {}, instance id:{}, state: {}",
                taskInstance.getName(), taskInstance.getProcessInstanceId(), processInstance.getState());
        //submit to db
        TaskInstance task = submitTaskInstanceToDB(taskInstance, processInstance);
        if (task == null) {
            logger.error("end submit task to db error, task name:{}, process id:{} state: {} ",
                    taskInstance.getName(), taskInstance.getProcessInstance(), processInstance.getState());
            return task;
        }
        if (!task.getState().typeIsFinished()) {
            createSubWorkProcess(processInstance, task);
        }

        logger.info("end submit task to db successfully:{} state:{} complete, instance id:{} state: {}  ",
                taskInstance.getName(), task.getState(), processInstance.getId(), processInstance.getState());
        return task;
    }

    /**
     * set work process instance map
     * consider o
     * repeat running  does not generate new sub process instance
     * set map {parent instance id, task instance id, 0(child instance id)}
     *
     * @param parentInstance parentInstance
     * @param parentTask     parentTask
     * @return process instance map
     */
    private ProcessInstanceMap setProcessInstanceMap(ProcessInstance parentInstance, TaskInstance parentTask) {
        ProcessInstanceMap processMap = findWorkProcessMapByParent(parentInstance.getId(), parentTask.getId());
        if (processMap != null) {
            return processMap;
        }
        if (parentInstance.getCommandType() == CommandType.REPEAT_RUNNING) {
            // update current task id to map
            processMap = findPreviousTaskProcessMap(parentInstance, parentTask);
            if (processMap != null) {
                processMap.setParentTaskInstanceId(parentTask.getId());
                updateWorkProcessInstanceMap(processMap);
                return processMap;
            }
        }
        // new task
        processMap = new ProcessInstanceMap();
        processMap.setParentProcessInstanceId(parentInstance.getId());
        processMap.setParentTaskInstanceId(parentTask.getId());
        createWorkProcessInstanceMap(processMap);
        return processMap;
    }

    /**
     * find previous task work process map.
     *
     * @param parentProcessInstance parentProcessInstance
     * @param parentTask            parentTask
     * @return process instance map
     */
    private ProcessInstanceMap findPreviousTaskProcessMap(ProcessInstance parentProcessInstance,
                                                          TaskInstance parentTask) {

        Integer preTaskId = 0;
        List<TaskInstance> preTaskList = this.findPreviousTaskListByWorkProcessId(parentProcessInstance.getId());
        for (TaskInstance task : preTaskList) {
            if (task.getName().equals(parentTask.getName())) {
                preTaskId = task.getId();
                ProcessInstanceMap map = findWorkProcessMapByParent(parentProcessInstance.getId(), preTaskId);
                if (map != null) {
                    return map;
                }
            }
        }
        logger.info("sub process instance is not found,parent task:{},parent instance:{}",
                parentTask.getId(), parentProcessInstance.getId());
        return null;
    }

    /**
     * create sub work process command
     *
     * @param parentProcessInstance parentProcessInstance
     * @param task                  task
     */
    public void createSubWorkProcess(ProcessInstance parentProcessInstance, TaskInstance task) {
        if (!task.isSubProcess()) {
            return;
        }
        //check create sub work flow firstly
        ProcessInstanceMap instanceMap = findWorkProcessMapByParent(parentProcessInstance.getId(), task.getId());
        if (null != instanceMap && CommandType.RECOVER_TOLERANCE_FAULT_PROCESS == parentProcessInstance.getCommandType()) {
            // recover failover tolerance would not create a new command when the sub command already have been created
            return;
        }
        instanceMap = setProcessInstanceMap(parentProcessInstance, task);
        ProcessInstance childInstance = null;
        if (instanceMap.getProcessInstanceId() != 0) {
            childInstance = findProcessInstanceById(instanceMap.getProcessInstanceId());
        }
        Command subProcessCommand = createSubProcessCommand(parentProcessInstance, childInstance, instanceMap, task);
        updateSubProcessDefinitionByParent(parentProcessInstance, subProcessCommand.getProcessDefinitionId());
        initSubInstanceState(childInstance);
        createCommand(subProcessCommand);
        logger.info("sub process command created: {} ", subProcessCommand);
    }

    /**
     * complement data needs transform parent parameter to child.
     */
    private String getSubWorkFlowParam(ProcessInstanceMap instanceMap, ProcessInstance parentProcessInstance) {
        // set sub work process command
        String processMapStr = JSONUtils.toJsonString(instanceMap);
        Map<String, String> cmdParam = JSONUtils.toMap(processMapStr);
        if (parentProcessInstance.isComplementData()) {
            Map<String, String> parentParam = JSONUtils.toMap(parentProcessInstance.getCommandParam());
            String endTime = parentParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
            String startTime = parentParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, endTime);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, startTime);
            processMapStr = JSONUtils.toJsonString(cmdParam);
        }
        return processMapStr;
    }

    /**
     * create sub work process command
     */
    public Command createSubProcessCommand(ProcessInstance parentProcessInstance,
                                           ProcessInstance childInstance,
                                           ProcessInstanceMap instanceMap,
                                           TaskInstance task) {
        CommandType commandType = getSubCommandType(parentProcessInstance, childInstance);
        TaskNode taskNode = JSONUtils.parseObject(task.getTaskJson(), TaskNode.class);
        Map<String, String> subProcessParam = JSONUtils.toMap(taskNode.getParams());
        Integer childDefineId = Integer.parseInt(subProcessParam.get(Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID));
        String processParam = getSubWorkFlowParam(instanceMap, parentProcessInstance);

        return new Command(
                commandType,
                TaskDependType.TASK_POST,
                parentProcessInstance.getFailureStrategy(),
                parentProcessInstance.getExecutorId(),
                childDefineId,
                processParam,
                parentProcessInstance.getWarningType(),
                parentProcessInstance.getWarningGroupId(),
                parentProcessInstance.getScheduleTime(),
                task.getWorkerGroup(),
                parentProcessInstance.getProcessInstancePriority()
        );
    }

    /**
     * initialize sub work flow state
     * child instance state would be initialized when 'recovery from pause/stop/failure'
     */
    private void initSubInstanceState(ProcessInstance childInstance) {
        if (childInstance != null) {
            childInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
            updateProcessInstance(childInstance);
        }
    }

    /**
     * get sub work flow command type
     * child instance exist: child command = fatherCommand
     * child instance not exists: child command = fatherCommand[0]
     */
    private CommandType getSubCommandType(ProcessInstance parentProcessInstance, ProcessInstance childInstance) {
        CommandType commandType = parentProcessInstance.getCommandType();
        if (childInstance == null) {
            String fatherHistoryCommand = parentProcessInstance.getHistoryCmd();
            commandType = CommandType.valueOf(fatherHistoryCommand.split(Constants.COMMA)[0]);
        }
        return commandType;
    }

    /**
     * update sub process definition
     *
     * @param parentProcessInstance parentProcessInstance
     * @param childDefinitionId     childDefinitionId
     */
    private void updateSubProcessDefinitionByParent(ProcessInstance parentProcessInstance, int childDefinitionId) {
        ProcessDefinition fatherDefinition = this.findProcessDefineById(parentProcessInstance.getProcessDefinitionId());
        ProcessDefinition childDefinition = this.findProcessDefineById(childDefinitionId);
        if (childDefinition != null && fatherDefinition != null) {
            childDefinition.setWarningGroupId(fatherDefinition.getWarningGroupId());
            processDefineMapper.updateById(childDefinition);
        }
    }

    /**
     * submit task to mysql
     *
     * @param taskInstance    taskInstance
     * @param processInstance processInstance
     * @return task instance
     */
    public TaskInstance submitTaskInstanceToDB(TaskInstance taskInstance, ProcessInstance processInstance) {
        ExecutionStatus processInstanceState = processInstance.getState();

        if (taskInstance.getState().typeIsFailure()) {
            if (taskInstance.isSubProcess()) {
                taskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1);
            } else {

                if (processInstanceState != ExecutionStatus.READY_STOP
                        && processInstanceState != ExecutionStatus.READY_PAUSE) {
                    // failure task set invalid
                    taskInstance.setFlag(Flag.NO);
                    updateTaskInstance(taskInstance);
                    // crate new task instance
                    if (taskInstance.getState() != ExecutionStatus.NEED_FAULT_TOLERANCE) {
                        taskInstance.setRetryTimes(taskInstance.getRetryTimes() + 1);
                    }
                    taskInstance.setSubmitTime(null);
                    taskInstance.setStartTime(null);
                    taskInstance.setEndTime(null);
                    taskInstance.setFlag(Flag.YES);
                    taskInstance.setHost(null);
                    taskInstance.setId(0);
                }
            }
        }
        taskInstance.setExecutorId(processInstance.getExecutorId());
        taskInstance.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        taskInstance.setState(getSubmitTaskState(taskInstance, processInstanceState));
        if (taskInstance.getSubmitTime() == null) {
            taskInstance.setSubmitTime(new Date());
        }
        if (taskInstance.getFirstSubmitTime() == null) {
            taskInstance.setFirstSubmitTime(taskInstance.getSubmitTime());
        }
        boolean saveResult = saveTaskInstance(taskInstance);
        if (!saveResult) {
            return null;
        }
        return taskInstance;
    }

    /**
     * get submit task instance state by the work process state
     * cannot modify the task state when running/kill/submit success, or this
     * task instance is already exists in task queue .
     * return pause if work process state is ready pause
     * return stop if work process state is ready stop
     * if all of above are not satisfied, return submit success
     *
     * @param taskInstance         taskInstance
     * @param processInstanceState processInstanceState
     * @return process instance state
     */
    public ExecutionStatus getSubmitTaskState(TaskInstance taskInstance, ExecutionStatus processInstanceState) {
        ExecutionStatus state = taskInstance.getState();
        // running, delayed or killed
        // the task already exists in task queue
        // return state
        if (
                state == ExecutionStatus.RUNNING_EXECUTION
                        || state == ExecutionStatus.DELAY_EXECUTION
                        || state == ExecutionStatus.KILL
        ) {
            return state;
        }
        //return pause /stop if process instance state is ready pause / stop
        // or return submit success
        if (processInstanceState == ExecutionStatus.READY_PAUSE) {
            state = ExecutionStatus.PAUSE;
        } else if (processInstanceState == ExecutionStatus.READY_STOP
                || !checkProcessStrategy(taskInstance)) {
            state = ExecutionStatus.KILL;
        } else {
            state = ExecutionStatus.SUBMITTED_SUCCESS;
        }
        return state;
    }

    /**
     * check process instance strategy
     *
     * @param taskInstance taskInstance
     * @return check strategy result
     */
    private boolean checkProcessStrategy(TaskInstance taskInstance) {
        ProcessInstance processInstance = this.findProcessInstanceById(taskInstance.getProcessInstanceId());
        FailureStrategy failureStrategy = processInstance.getFailureStrategy();
        if (failureStrategy == FailureStrategy.CONTINUE) {
            return true;
        }
        List<TaskInstance> taskInstances = this.findValidTaskListByProcessId(taskInstance.getProcessInstanceId());

        for (TaskInstance task : taskInstances) {
            if (task.getState() == ExecutionStatus.FAILURE) {
                return false;
            }
        }
        return true;
    }

    /**
     * create a new process instance
     *
     * @param processInstance processInstance
     */
    public void createProcessInstance(ProcessInstance processInstance) {

        if (processInstance != null) {
            processInstanceMapper.insert(processInstance);
        }
    }

    /**
     * insert or update work process instance to data base
     *
     * @param processInstance processInstance
     */
    public void saveProcessInstance(ProcessInstance processInstance) {

        if (processInstance == null) {
            logger.error("save error, process instance is null!");
            return;
        }
        if (processInstance.getId() != 0) {
            processInstanceMapper.updateById(processInstance);
        } else {
            createProcessInstance(processInstance);
        }
    }

    /**
     * insert or update command
     *
     * @param command command
     * @return save command result
     */
    public int saveCommand(Command command) {
        if (command.getId() != 0) {
            return commandMapper.updateById(command);
        } else {
            return commandMapper.insert(command);
        }
    }

    /**
     * insert or update task instance
     *
     * @param taskInstance taskInstance
     * @return save task instance result
     */
    public boolean saveTaskInstance(TaskInstance taskInstance) {
        if (taskInstance.getId() != 0) {
            return updateTaskInstance(taskInstance);
        } else {
            return createTaskInstance(taskInstance);
        }
    }

    /**
     * insert task instance
     *
     * @param taskInstance taskInstance
     * @return create task instance result
     */
    public boolean createTaskInstance(TaskInstance taskInstance) {
        int count = taskInstanceMapper.insert(taskInstance);
        return count > 0;
    }

    /**
     * update task instance
     *
     * @param taskInstance taskInstance
     * @return update task instance result
     */
    public boolean updateTaskInstance(TaskInstance taskInstance) {
        int count = taskInstanceMapper.updateById(taskInstance);
        return count > 0;
    }

    /**
     * delete a command by id
     *
     * @param id id
     */
    public void delCommandById(int id) {
        commandMapper.deleteById(id);
    }

    /**
     * find task instance by id
     *
     * @param taskId task id
     * @return task instance
     */
    public TaskInstance findTaskInstanceById(Integer taskId) {
        return taskInstanceMapper.selectById(taskId);
    }

    /**
     * package task instance，associate processInstance and processDefine
     *
     * @param taskInstId taskInstId
     * @return task instance
     */
    public TaskInstance getTaskInstanceDetailByTaskId(int taskInstId) {
        // get task instance
        TaskInstance taskInstance = findTaskInstanceById(taskInstId);
        if (taskInstance == null) {
            return taskInstance;
        }
        // get process instance
        ProcessInstance processInstance = findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
        // get process define
        ProcessDefinition processDefine = findProcessDefineById(taskInstance.getProcessDefinitionId());

        taskInstance.setProcessInstance(processInstance);
        taskInstance.setProcessDefine(processDefine);
        return taskInstance;
    }

    /**
     * get id list by task state
     *
     * @param instanceId instanceId
     * @param state      state
     * @return task instance states
     */
    public List<Integer> findTaskIdByInstanceState(int instanceId, ExecutionStatus state) {
        return taskInstanceMapper.queryTaskByProcessIdAndState(instanceId, state.ordinal());
    }

    /**
     * find valid task list by process definition id
     *
     * @param processInstanceId processInstanceId
     * @return task instance list
     */
    public List<TaskInstance> findValidTaskListByProcessId(Integer processInstanceId) {
        return taskInstanceMapper.findValidTaskListByProcessId(processInstanceId, Flag.YES);
    }

    /**
     * find previous task list by work process id
     *
     * @param processInstanceId processInstanceId
     * @return task instance list
     */
    public List<TaskInstance> findPreviousTaskListByWorkProcessId(Integer processInstanceId) {
        return taskInstanceMapper.findValidTaskListByProcessId(processInstanceId, Flag.NO);
    }

    /**
     * update work process instance map
     *
     * @param processInstanceMap processInstanceMap
     * @return update process instance result
     */
    public int updateWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap) {
        return processInstanceMapMapper.updateById(processInstanceMap);
    }

    /**
     * create work process instance map
     *
     * @param processInstanceMap processInstanceMap
     * @return create process instance result
     */
    public int createWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap) {
        int count = 0;
        if (processInstanceMap != null) {
            return processInstanceMapMapper.insert(processInstanceMap);
        }
        return count;
    }

    /**
     * find work process map by parent process id and parent task id.
     *
     * @param parentWorkProcessId parentWorkProcessId
     * @param parentTaskId        parentTaskId
     * @return process instance map
     */
    public ProcessInstanceMap findWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId) {
        return processInstanceMapMapper.queryByParentId(parentWorkProcessId, parentTaskId);
    }

    /**
     * delete work process map by parent process id
     *
     * @param parentWorkProcessId parentWorkProcessId
     * @return delete process map result
     */
    public int deleteWorkProcessMapByParentId(int parentWorkProcessId) {
        return processInstanceMapMapper.deleteByParentProcessId(parentWorkProcessId);

    }

    /**
     * find sub process instance
     *
     * @param parentProcessId parentProcessId
     * @param parentTaskId    parentTaskId
     * @return process instance
     */
    public ProcessInstance findSubProcessInstance(Integer parentProcessId, Integer parentTaskId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryByParentId(parentProcessId, parentTaskId);
        if (processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0) {
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getProcessInstanceId());
        return processInstance;
    }

    /**
     * find parent process instance
     *
     * @param subProcessId subProcessId
     * @return process instance
     */
    public ProcessInstance findParentProcessInstance(Integer subProcessId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryBySubProcessId(subProcessId);
        if (processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0) {
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getParentProcessInstanceId());
        return processInstance;
    }

    /**
     * change task state
     *
     * @param state       state
     * @param startTime   startTime
     * @param host        host
     * @param executePath executePath
     * @param logPath     logPath
     * @param taskInstId  taskInstId
     */
    public void changeTaskState(TaskInstance taskInstance, ExecutionStatus state, Date startTime, String host,
                                String executePath,
                                String logPath,
                                int taskInstId) {
        taskInstance.setState(state);
        taskInstance.setStartTime(startTime);
        taskInstance.setHost(host);
        taskInstance.setExecutePath(executePath);
        taskInstance.setLogPath(logPath);
        saveTaskInstance(taskInstance);
    }

    /**
     * update process instance
     *
     * @param processInstance processInstance
     * @return update process instance result
     */
    public int updateProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.updateById(processInstance);
    }

    /**
     * update the process instance
     *
     * @param processInstanceId processInstanceId
     * @param processJson       processJson
     * @param globalParams      globalParams
     * @param scheduleTime      scheduleTime
     * @param flag              flag
     * @param locations         locations
     * @param connects          connects
     * @return update process instance result
     */
    public int updateProcessInstance(Integer processInstanceId, String processJson,
                                     String globalParams, Date scheduleTime, Flag flag,
                                     String locations, String connects) {
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);
        if (processInstance != null) {
            processInstance.setProcessInstanceJson(processJson);
            processInstance.setGlobalParams(globalParams);
            processInstance.setScheduleTime(scheduleTime);
            processInstance.setLocations(locations);
            processInstance.setConnects(connects);
            return processInstanceMapper.updateById(processInstance);
        }
        return 0;
    }

    /**
     * change task state
     *
     * @param state      state
     * @param endTime    endTime
     * @param taskInstId taskInstId
     * @param varPool    varPool
     */
    public void changeTaskState(TaskInstance taskInstance, ExecutionStatus state,
                                Date endTime,
                                int processId,
                                String appIds,
                                int taskInstId,
                                String varPool,
                                String result) {
        taskInstance.setPid(processId);
        taskInstance.setAppLink(appIds);
        taskInstance.setState(state);
        taskInstance.setEndTime(endTime);
        taskInstance.setVarPool(varPool);
        changeOutParam(result, taskInstance);
        saveTaskInstance(taskInstance);
    }

    public void changeOutParam(String result, TaskInstance taskInstance) {
        if (StringUtils.isEmpty(result)) {
            return;
        }
        List<Map<String, String>> workerResultParam = getListMapByString(result);
        if (CollectionUtils.isEmpty(workerResultParam)) {
            return;
        }
        //if the result more than one line,just get the first .
        Map<String, String> row = workerResultParam.get(0);
        if (row == null || row.size() == 0) {
            return;
        }
        TaskNode taskNode = JSONUtils.parseObject(taskInstance.getTaskJson(), TaskNode.class);
        Map<String, Object> taskParams = JSONUtils.toMap(taskNode.getParams(), String.class, Object.class);
        Object localParams = taskParams.get(LOCAL_PARAMS);
        if (localParams == null) {
            return;
        }
        ProcessInstance processInstance = this.processInstanceMapper.queryDetailById(taskInstance.getProcessInstanceId());
        List<Property> params4Property = JSONUtils.toList(processInstance.getGlobalParams(), Property.class);
        Map<String, Property> allParamMap = params4Property.stream().collect(Collectors.toMap(Property::getProp, Property -> Property));

        List<Property> allParam = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
        for (Property info : allParam) {
            if (info.getDirect() == Direct.OUT) {
                String paramName = info.getProp();
                Property property = allParamMap.get(paramName);
                if (property == null) {
                    continue;
                }
                String value = row.get(paramName);
                if (StringUtils.isNotEmpty(value)) {
                    property.setValue(value);
                    info.setValue(value);
                }
            }
        }
        taskParams.put(LOCAL_PARAMS, allParam);
        taskNode.setParams(JSONUtils.toJsonString(taskParams));
        // task instance node json
        taskInstance.setTaskJson(JSONUtils.toJsonString(taskNode));
        String params4ProcessString = JSONUtils.toJsonString(params4Property);
        int updateCount = this.processInstanceMapper.updateGlobalParamsById(params4ProcessString, processInstance.getId());
        logger.info("updateCount:{}, params4Process:{}, processInstanceId:{}", updateCount, params4ProcessString, processInstance.getId());
    }

    public List<Map<String, String>> getListMapByString(String json) {
        List<Map<String, String>> allParams = new ArrayList<>();
        ArrayNode paramsByJson = JSONUtils.parseArray(json);
        Iterator<JsonNode> listIterator = paramsByJson.iterator();
        while (listIterator.hasNext()) {
            Map<String, String> param = JSONUtils.toMap(listIterator.next().toString(), String.class, String.class);
            allParams.add(param);
        }
        return allParams;
    }

    /**
     * convert integer list to string list
     *
     * @param intList intList
     * @return string list
     */
    public List<String> convertIntListToString(List<Integer> intList) {
        if (intList == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>(intList.size());
        for (Integer intVar : intList) {
            result.add(String.valueOf(intVar));
        }
        return result;
    }

    /**
     * query schedule by id
     *
     * @param id id
     * @return schedule
     */
    public Schedule querySchedule(int id) {
        return scheduleMapper.selectById(id);
    }

    /**
     * query Schedule by processDefinitionId
     *
     * @param processDefinitionId processDefinitionId
     * @see Schedule
     */
    public List<Schedule> queryReleaseSchedulerListByProcessDefinitionId(int processDefinitionId) {
        return scheduleMapper.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId);
    }

    /**
     * query need failover process instance
     *
     * @param host host
     * @return process instance list
     */
    public List<ProcessInstance> queryNeedFailoverProcessInstances(String host) {
        return processInstanceMapper.queryByHostAndStatus(host, stateArray);
    }

    /**
     * process need failover process instance
     *
     * @param processInstance processInstance
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void processNeedFailoverProcessInstances(ProcessInstance processInstance) {
        //1 update processInstance host is null
        processInstance.setHost(Constants.NULL);
        processInstanceMapper.updateById(processInstance);

        //2 insert into recover command
        Command cmd = new Command();
        cmd.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        cmd.setCommandParam(String.format("{\"%s\":%d}", Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING, processInstance.getId()));
        cmd.setExecutorId(processInstance.getExecutorId());
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        createCommand(cmd);
    }

    /**
     * query all need failover task instances by host
     *
     * @param host host
     * @return task instance list
     */
    public List<TaskInstance> queryNeedFailoverTaskInstances(String host) {
        return taskInstanceMapper.queryByHostAndStatus(host,
                stateArray);
    }

    /**
     * find data source by id
     *
     * @param id id
     * @return datasource
     */
    public DataSource findDataSourceById(int id) {
        return dataSourceMapper.selectById(id);
    }

    /**
     * update process instance state by id
     *
     * @param processInstanceId processInstanceId
     * @param executionStatus   executionStatus
     * @return update process result
     */
    public int updateProcessInstanceState(Integer processInstanceId, ExecutionStatus executionStatus) {
        ProcessInstance instance = processInstanceMapper.selectById(processInstanceId);
        instance.setState(executionStatus);
        return processInstanceMapper.updateById(instance);

    }

    /**
     * find process instance by the task id
     *
     * @param taskId taskId
     * @return process instance
     */
    public ProcessInstance findProcessInstanceByTaskId(int taskId) {
        TaskInstance taskInstance = taskInstanceMapper.selectById(taskId);
        if (taskInstance != null) {
            return processInstanceMapper.selectById(taskInstance.getProcessInstanceId());
        }
        return null;
    }

    /**
     * find udf function list by id list string
     *
     * @param ids ids
     * @return udf function list
     */
    public List<UdfFunc> queryUdfFunListByIds(int[] ids) {
        return udfFuncMapper.queryUdfByIdStr(ids, null);
    }

    /**
     * find tenant code by resource name
     *
     * @param resName      resource name
     * @param resourceType resource type
     * @return tenant code
     */
    public String queryTenantCodeByResName(String resName, ResourceType resourceType) {
        // in order to query tenant code successful although the version is older
        String fullName = resName.startsWith("/") ? resName : String.format("/%s", resName);
        return resourceMapper.queryTenantCodeByResourceName(fullName, resourceType.ordinal());
    }

    /**
     * find schedule list by process define id.
     *
     * @param ids ids
     * @return schedule list
     */
    public List<Schedule> selectAllByProcessDefineId(int[] ids) {
        return scheduleMapper.selectAllByProcessDefineArray(
                ids);
    }

    /**
     * get dependency cycle by work process define id and scheduler fire time
     *
     * @param masterId            masterId
     * @param processDefinitionId processDefinitionId
     * @param scheduledFireTime   the time the task schedule is expected to trigger
     * @return CycleDependency
     * @throws Exception if error throws Exception
     */
    public CycleDependency getCycleDependency(int masterId, int processDefinitionId, Date scheduledFireTime) throws Exception {
        List<CycleDependency> list = getCycleDependencies(masterId, new int[]{processDefinitionId}, scheduledFireTime);
        return !list.isEmpty() ? list.get(0) : null;

    }

    /**
     * get dependency cycle list by work process define id list and scheduler fire time
     *
     * @param masterId          masterId
     * @param ids               ids
     * @param scheduledFireTime the time the task schedule is expected to trigger
     * @return CycleDependency list
     * @throws Exception if error throws Exception
     */
    public List<CycleDependency> getCycleDependencies(int masterId, int[] ids, Date scheduledFireTime) throws Exception {
        List<CycleDependency> cycleDependencyList = new ArrayList<>();
        if (null == ids || ids.length == 0) {
            logger.warn("ids[] is empty!is invalid!");
            return cycleDependencyList;
        }
        if (scheduledFireTime == null) {
            logger.warn("scheduledFireTime is null!is invalid!");
            return cycleDependencyList;
        }

        String strCrontab = "";
        CronExpression depCronExpression;
        Cron depCron;
        List<Date> list;
        List<Schedule> schedules = this.selectAllByProcessDefineId(ids);
        // for all scheduling information
        for (Schedule depSchedule : schedules) {
            strCrontab = depSchedule.getCrontab();
            depCronExpression = CronUtils.parse2CronExpression(strCrontab);
            depCron = CronUtils.parse2Cron(strCrontab);
            CycleEnum cycleEnum = CronUtils.getMiniCycle(depCron);
            if (cycleEnum == null) {
                logger.error("{} is not valid", strCrontab);
                continue;
            }
            Calendar calendar = Calendar.getInstance();
            switch (cycleEnum) {
                case HOUR:
                    calendar.add(Calendar.HOUR, -25);
                    break;
                case DAY:
                case WEEK:
                    calendar.add(Calendar.DATE, -32);
                    break;
                case MONTH:
                    calendar.add(Calendar.MONTH, -13);
                    break;
                default:
                    String cycleName = cycleEnum.name();
                    logger.warn("Dependent process definition's  cycleEnum is {},not support!!", cycleName);
                    continue;
            }
            Date start = calendar.getTime();

            if (depSchedule.getProcessDefinitionId() == masterId) {
                list = CronUtils.getSelfFireDateList(start, scheduledFireTime, depCronExpression);
            } else {
                list = CronUtils.getFireDateList(start, scheduledFireTime, depCronExpression);
            }
            if (!list.isEmpty()) {
                start = list.get(list.size() - 1);
                CycleDependency dependency = new CycleDependency(depSchedule.getProcessDefinitionId(), start, CronUtils.getExpirationTime(start, cycleEnum), cycleEnum);
                cycleDependencyList.add(dependency);
            }

        }
        return cycleDependencyList;
    }

    /**
     * find last scheduler process instance in the date interval
     *
     * @param definitionId definitionId
     * @param dateInterval dateInterval
     * @return process instance
     */
    public ProcessInstance findLastSchedulerProcessInterval(int definitionId, DateInterval dateInterval) {
        return processInstanceMapper.queryLastSchedulerProcess(definitionId,
                dateInterval.getStartTime(),
                dateInterval.getEndTime());
    }

    /**
     * find last manual process instance interval
     *
     * @param definitionId process definition id
     * @param dateInterval dateInterval
     * @return process instance
     */
    public ProcessInstance findLastManualProcessInterval(int definitionId, DateInterval dateInterval) {
        return processInstanceMapper.queryLastManualProcess(definitionId,
                dateInterval.getStartTime(),
                dateInterval.getEndTime());
    }

    /**
     * find last running process instance
     *
     * @param definitionId process definition id
     * @param startTime    start time
     * @param endTime      end time
     * @return process instance
     */
    public ProcessInstance findLastRunningProcess(int definitionId, Date startTime, Date endTime) {
        return processInstanceMapper.queryLastRunningProcess(definitionId,
                startTime,
                endTime,
                stateArray);
    }

    /**
     * query user queue by process instance id
     *
     * @param processInstanceId processInstanceId
     * @return queue
     */
    public String queryUserQueueByProcessInstanceId(int processInstanceId) {

        String queue = "";
        ProcessInstance processInstance = processInstanceMapper.selectById(processInstanceId);
        if (processInstance == null) {
            return queue;
        }
        User executor = userMapper.selectById(processInstance.getExecutorId());
        if (executor != null) {
            queue = executor.getQueue();
        }
        return queue;
    }

    /**
     * query project name and user name by processInstanceId.
     *
     * @param processInstanceId processInstanceId
     * @return projectName and userName
     */
    public ProjectUser queryProjectWithUserByProcessInstanceId(int processInstanceId) {
        return projectMapper.queryProjectWithUserByProcessInstanceId(processInstanceId);
    }

    /**
     * get task worker group
     *
     * @param taskInstance taskInstance
     * @return workerGroupId
     */
    public String getTaskWorkerGroup(TaskInstance taskInstance) {
        String workerGroup = taskInstance.getWorkerGroup();

        if (StringUtils.isNotBlank(workerGroup)) {
            return workerGroup;
        }
        int processInstanceId = taskInstance.getProcessInstanceId();
        ProcessInstance processInstance = findProcessInstanceById(processInstanceId);

        if (processInstance != null) {
            return processInstance.getWorkerGroup();
        }
        logger.info("task : {} will use default worker group", taskInstance.getId());
        return Constants.DEFAULT_WORKER_GROUP;
    }

    /**
     * get have perm project list
     *
     * @param userId userId
     * @return project list
     */
    public List<Project> getProjectListHavePerm(int userId) {
        List<Project> createProjects = projectMapper.queryProjectCreatedByUser(userId);
        List<Project> authedProjects = projectMapper.queryAuthedProjectListByUserId(userId);

        if (createProjects == null) {
            createProjects = new ArrayList<>();
        }

        if (authedProjects != null) {
            createProjects.addAll(authedProjects);
        }
        return createProjects;
    }

    /**
     * get have perm project ids
     *
     * @param userId userId
     * @return project ids
     */
    public List<Integer> getProjectIdListHavePerm(int userId) {

        List<Integer> projectIdList = new ArrayList<>();
        for (Project project : getProjectListHavePerm(userId)) {
            projectIdList.add(project.getId());
        }
        return projectIdList;
    }

    /**
     * list unauthorized udf function
     *
     * @param userId     user id
     * @param needChecks data source id array
     * @return unauthorized udf function list
     */
    public <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType) {
        List<T> resultList = new ArrayList<>();

        if (Objects.nonNull(needChecks) && needChecks.length > 0) {
            Set<T> originResSet = new HashSet<>(Arrays.asList(needChecks));

            switch (authorizationType) {
                case RESOURCE_FILE_ID:
                case UDF_FILE:
                    Set<Integer> authorizedResourceFiles = resourceMapper.listAuthorizedResourceById(userId, needChecks).stream().map(Resource::getId).collect(toSet());
                    originResSet.removeAll(authorizedResourceFiles);
                    break;
                case RESOURCE_FILE_NAME:
                    Set<String> authorizedResources = resourceMapper.listAuthorizedResource(userId, needChecks).stream().map(Resource::getFullName).collect(toSet());
                    originResSet.removeAll(authorizedResources);
                    break;
                case DATASOURCE:
                    Set<Integer> authorizedDatasources = dataSourceMapper.listAuthorizedDataSource(userId, needChecks).stream().map(DataSource::getId).collect(toSet());
                    originResSet.removeAll(authorizedDatasources);
                    break;
                case UDF:
                    Set<Integer> authorizedUdfs = udfFuncMapper.listAuthorizedUdfFunc(userId, needChecks).stream().map(UdfFunc::getId).collect(toSet());
                    originResSet.removeAll(authorizedUdfs);
                    break;
                default:
                    break;
            }

            resultList.addAll(originResSet);
        }

        return resultList;
    }

    /**
     * get user by user id
     *
     * @param userId user id
     * @return User
     */
    public User getUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * get resource by resource id
     *
     * @param resourceId resource id
     * @return Resource
     */
    public Resource getResourceById(int resourceId) {
        return resourceMapper.selectById(resourceId);
    }

    /**
     * list resources by ids
     *
     * @param resIds resIds
     * @return resource list
     */
    public List<Resource> listResourceByIds(Integer[] resIds) {
        return resourceMapper.listResourceByIds(resIds);
    }

    /**
     * format task app id in task instance
     */
    public String formatTaskAppId(TaskInstance taskInstance) {
        ProcessDefinition definition = this.findProcessDefineById(taskInstance.getProcessDefinitionId());
        ProcessInstance processInstanceById = this.findProcessInstanceById(taskInstance.getProcessInstanceId());

        if (definition == null || processInstanceById == null) {
            return "";
        }
        return String.format("%s_%s_%s",
                definition.getId(),
                processInstanceById.getId(),
                taskInstance.getId());
    }

    /**
     * solve the branch rename bug
     *
     * @param processData
     * @param oldJson
     * @return String
     */
    public String changeJson(ProcessData processData, String oldJson) {
        ProcessData oldProcessData = JSONUtils.parseObject(oldJson, ProcessData.class);
        HashMap<String, String> oldNameTaskId = new HashMap<>();
        List<TaskNode> oldTasks = oldProcessData.getTasks();
        for (int i = 0; i < oldTasks.size(); i++) {
            TaskNode taskNode = oldTasks.get(i);
            String oldName = taskNode.getName();
            String oldId = taskNode.getId();
            oldNameTaskId.put(oldName, oldId);
        }

        // take the processdefinitionjson saved this time, and then save the taskid and name
        HashMap<String, String> newNameTaskId = new HashMap<>();
        List<TaskNode> newTasks = processData.getTasks();
        for (int i = 0; i < newTasks.size(); i++) {
            TaskNode taskNode = newTasks.get(i);
            String newId = taskNode.getId();
            String newName = taskNode.getName();
            newNameTaskId.put(newId, newName);
        }

        // replace the previous conditionresult with a new one
        List<TaskNode> tasks = processData.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            TaskNode taskNode = newTasks.get(i);
            String type = taskNode.getType();
            if (TaskType.CONDITIONS.getDescp().equalsIgnoreCase(type)) {
                ConditionsParameters conditionsParameters = JSONUtils.parseObject(taskNode.getConditionResult(), ConditionsParameters.class);
                String oldSuccessNodeName = conditionsParameters.getSuccessNode().get(0);
                String oldFailedNodeName = conditionsParameters.getFailedNode().get(0);
                String newSuccessNodeName = newNameTaskId.get(oldNameTaskId.get(oldSuccessNodeName));
                String newFailedNodeName = newNameTaskId.get(oldNameTaskId.get(oldFailedNodeName));
                if (newSuccessNodeName != null) {
                    ArrayList<String> successNode = new ArrayList<>();
                    successNode.add(newSuccessNodeName);
                    conditionsParameters.setSuccessNode(successNode);
                }
                if (newFailedNodeName != null) {
                    ArrayList<String> failedNode = new ArrayList<>();
                    failedNode.add(newFailedNodeName);
                    conditionsParameters.setFailedNode(failedNode);
                }
                String conditionResultStr = conditionsParameters.getConditionResult();
                taskNode.setConditionResult(conditionResultStr);
                tasks.set(i, taskNode);
            }
        }
        return JSONUtils.toJsonString(processData);
    }
}
