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

import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;
import static org.apache.dolphinscheduler.common.Constants.GLOBAL_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.PROCESS_INSTANCE_STATE;
import static org.apache.dolphinscheduler.common.Constants.TASK_LIST;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.dto.CheckParamResultWithInfo;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.dto.gantt.Task;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionVersionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process instance service impl
 */
@Service
public class ProcessInstanceServiceImpl extends BaseServiceImpl implements ProcessInstanceService {

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessService processService;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    ProcessDefinitionMapper processDefineMapper;

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Autowired
    ProcessDefinitionVersionService processDefinitionVersionService;

    @Autowired
    ExecutorService execService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    LoggerService loggerService;


    @Autowired
    UsersService usersService;

    /**
     * return top n SUCCESS process instance order by running time which started between startTime and endTime
     */
    @Override
    public Result<List<ProcessInstance>> queryTopNLongestRunningProcessInstance(User loginUser, String projectName, int size, String startTime, String endTime) {

        Project project = projectMapper.queryByName(projectName);
        CheckParamResult chechResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(chechResult.getStatus())) {
            return Result.error(chechResult);
        }

        if (0 > size) {
            putMsg(chechResult, Status.NEGTIVE_SIZE_NUMBER_ERROR, size);
            return Result.error(chechResult);
        }
        if (Objects.isNull(startTime)) {
            putMsg(chechResult, Status.DATA_IS_NULL, Constants.START_TIME);
            return Result.error(chechResult);
        }
        Date start = DateUtils.stringToDate(startTime);
        if (Objects.isNull(endTime)) {
            putMsg(chechResult, Status.DATA_IS_NULL, Constants.END_TIME);
            return Result.error(chechResult);
        }
        Date end = DateUtils.stringToDate(endTime);
        if (start == null || end == null) {
            putMsg(chechResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
            return Result.error(chechResult);
        }
        if (start.getTime() > end.getTime()) {
            putMsg(chechResult, Status.START_TIME_BIGGER_THAN_END_TIME_ERROR, startTime, endTime);
            return Result.error(chechResult);
        }

        List<ProcessInstance> processInstances = processInstanceMapper.queryTopNProcessInstance(size, start, end, ExecutionStatus.SUCCESS);
        return Result.success(processInstances);
    }

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process instance id
     * @return process instance detail
     */
    @Override
    public Result<ProcessInstance> queryProcessInstanceById(User loginUser, String projectName, Integer processId) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId);

        ProcessDefinition processDefinition = processService.findProcessDefineById(processInstance.getProcessDefinitionId());
        processInstance.setWarningGroupId(processDefinition.getWarningGroupId());

        return Result.success(processInstance);
    }

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser login user
     * @param projectName project name
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefineId process definition id
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @return process instance list
     */
    @Override
    public Result<PageListVO<ProcessInstance>> queryProcessInstanceList(User loginUser, String projectName, Integer processDefineId,
                                                                        String startDate, String endDate,
                                                                        String searchVal, String executorName, ExecutionStatus stateType, String host,
                                                                        Integer pageNo, Integer pageSize) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        int[] statusArray = null;
        // filter by state
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }

        CheckParamResultWithInfo<Map<String, Date>> checkAndParseDateResult = checkAndParseDateParameters(startDate, endDate);
        if (!Status.SUCCESS.equals(checkAndParseDateResult.getStatus())) {
            return Result.error(checkAndParseDateResult);
        }
        Date start = checkAndParseDateResult.getInfo().get(Constants.START_TIME);
        Date end = checkAndParseDateResult.getInfo().get(Constants.END_TIME);

        Page<ProcessInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<ProcessInstance> pageInfo = new PageInfo<>(pageNo, pageSize);
        int executorId = usersService.getUserIdByName(executorName);

        IPage<ProcessInstance> processInstanceList =
                processInstanceMapper.queryProcessInstanceListPaging(page,
                        project.getId(), processDefineId, searchVal, executorId, statusArray, host, start, end);

        List<ProcessInstance> processInstances = processInstanceList.getRecords();
        List<Integer> userIds = CollectionUtils.transformToList(processInstances, ProcessInstance::getExecutorId);
        Map<Integer, User> idToUserMap = CollectionUtils.collectionToMap(usersService.queryUser(userIds), User::getId);

        for (ProcessInstance processInstance : processInstances) {
            processInstance.setDuration(DateUtils.format2Duration(processInstance.getStartTime(), processInstance.getEndTime()));
            User executor = idToUserMap.get(processInstance.getExecutorId());
            if (null != executor) {
                processInstance.setExecutorName(executor.getUserName());
            }
        }

        pageInfo.setTotalCount((int) processInstanceList.getTotal());
        pageInfo.setLists(processInstances);
        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process instance id
     * @return task list for the process instance
     * @throws IOException io exception
     */
    @Override
    public Result<Map<String, Object>> queryTaskListByProcessId(User loginUser, String projectName, Integer processId) throws IOException {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processId);
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(processId);
        addDependResultForTaskList(taskInstanceList);
        // todo: improve
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(PROCESS_INSTANCE_STATE, processInstance.getState().toString());
        resultMap.put(TASK_LIST, taskInstanceList);

        return Result.success(resultMap);
    }

    /**
     * add dependent result for dependent task
     */
    private void addDependResultForTaskList(List<TaskInstance> taskInstanceList) throws IOException {
        for (TaskInstance taskInstance : taskInstanceList) {
            if (taskInstance.getTaskType().equalsIgnoreCase(TaskType.DEPENDENT.toString())) {
                Result<String> logResult = loggerService.queryLog(
                        taskInstance.getId(), Constants.LOG_QUERY_SKIP_LINE_NUMBER, Constants.LOG_QUERY_LIMIT);
                if (logResult.getCode() == Status.SUCCESS.ordinal()) {
                    String log = logResult.getData();
                    Map<String, DependResult> resultMap = parseLogForDependentResult(log);
                    taskInstance.setDependentResult(JSONUtils.toJsonString(resultMap));
                }
            }
        }
    }

    @Override
    public Map<String, DependResult> parseLogForDependentResult(String log) throws IOException {
        Map<String, DependResult> resultMap = new HashMap<>();
        if (StringUtils.isEmpty(log)) {
            return resultMap;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(log.getBytes(
                StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(DEPENDENT_SPLIT)) {
                String[] tmpStringArray = line.split(":\\|\\|");
                if (tmpStringArray.length != 2) {
                    continue;
                }
                String dependResultString = tmpStringArray[1];
                String[] dependStringArray = dependResultString.split(",");
                if (dependStringArray.length != 2) {
                    continue;
                }
                String key = dependStringArray[0].trim();
                DependResult dependResult = DependResult.valueOf(dependStringArray[1].trim());
                resultMap.put(key, dependResult);
            }
        }
        return resultMap;
    }

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskId task id
     * @return sub process instance detail
     */
    @Override
    public Result<Map<String, Object>> querySubProcessInstanceByTaskId(User loginUser, String projectName, Integer taskId) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        TaskInstance taskInstance = processService.findTaskInstanceById(taskId);
        if (taskInstance == null) {
            return Result.errorWithArgs(Status.TASK_INSTANCE_NOT_EXISTS, taskId);
        }
        if (!taskInstance.isSubProcess()) {
            return Result.errorWithArgs(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE, taskInstance.getName());
        }

        ProcessInstance subWorkflowInstance = processService.findSubProcessInstance(
                taskInstance.getProcessInstanceId(), taskInstance.getId());
        if (subWorkflowInstance == null) {
            return Result.errorWithArgs(Status.SUB_PROCESS_INSTANCE_NOT_EXIST, taskId);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.SUBPROCESS_INSTANCE_ID, subWorkflowInstance.getId());
        return Result.success(dataMap);
    }

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceJson process instance json
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param flag flag
     * @param locations locations
     * @param connects connects
     * @return update result code
     * @throws ParseException parse exception for json parse
     */
    @Override
    public Result<Void> updateProcessInstance(User loginUser, String projectName, Integer processInstanceId,
                                              String processInstanceJson, String scheduleTime, Boolean syncDefine,
                                              Flag flag, String locations, String connects) throws ParseException {
        Project project = projectMapper.queryByName(projectName);

        //check project permission
        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        //check process instance exists
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
        }

        //check process instance status
        if (!processInstance.getState().typeIsFinished()) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR,
                    processInstance.getName(), processInstance.getState().toString(), "update");
        }
        Date schedule = null;
        schedule = processInstance.getScheduleTime();
        if (scheduleTime != null) {
            schedule = DateUtils.getScheduleDate(scheduleTime);
        }
        processInstance.setScheduleTime(schedule);
        processInstance.setLocations(locations);
        processInstance.setConnects(connects);
        String globalParams = null;
        String originDefParams = null;
        int timeout = processInstance.getTimeout();
        ProcessDefinition processDefinition = processService.findProcessDefineById(processInstance.getProcessDefinitionId());
        if (StringUtils.isNotEmpty(processInstanceJson)) {
            ProcessData processData = JSONUtils.parseObject(processInstanceJson, ProcessData.class);
            //check workflow json is valid
            CheckParamResult checkProcessNodeResult = processDefinitionService.checkProcessNodeList(processData, processInstanceJson);
            if (!Status.SUCCESS.equals(checkProcessNodeResult.getStatus())) {
                return Result.error(checkProcessNodeResult);
            }

            originDefParams = JSONUtils.toJsonString(processData.getGlobalParams());
            List<Property> globalParamList = processData.getGlobalParams();
            Map<String, String> globalParamMap = Optional.ofNullable(globalParamList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
            globalParams = ParameterUtils.curingGlobalParams(globalParamMap, globalParamList,
                    processInstance.getCmdTypeIfComplement(), schedule);
            timeout = processData.getTimeout();
            processInstance.setTimeout(timeout);
            Tenant tenant = processService.getTenantForProcess(processData.getTenantId(),
                    processDefinition.getUserId());
            if (tenant != null) {
                processInstance.setTenantCode(tenant.getTenantCode());
            }
            // get the processinstancejson before saving,and then save the name and taskid
            String oldJson = processInstance.getProcessInstanceJson();
            if (StringUtils.isNotEmpty(oldJson)) {
                processInstanceJson = processService.changeJson(processData,oldJson);
            }
            processInstance.setProcessInstanceJson(processInstanceJson);
            processInstance.setGlobalParams(globalParams);
        }

        int update = processService.updateProcessInstance(processInstance);
        int updateDefine = 1;
        if (Boolean.TRUE.equals(syncDefine)) {
            processDefinition.setProcessDefinitionJson(processInstanceJson);
            processDefinition.setGlobalParams(originDefParams);
            processDefinition.setLocations(locations);
            processDefinition.setConnects(connects);
            processDefinition.setTimeout(timeout);
            processDefinition.setUpdateTime(new Date());

            // add process definition version
            long version = processDefinitionVersionService.addProcessDefinitionVersion(processDefinition);
            processDefinition.setVersion(version);
            updateDefine = processDefineMapper.updateById(processDefinition);
        }
        if (update > 0 && updateDefine > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.UPDATE_PROCESS_INSTANCE_ERROR);
        }
    }

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param subId sub process id
     * @return parent instance detail
     */
    @Override
    public Result<Map<String, Object>> queryParentInstanceBySubId(User loginUser, String projectName, Integer subId) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        ProcessInstance subInstance = processService.findProcessInstanceDetailById(subId);
        if (subInstance == null) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_NOT_EXIST, subId);
        }
        if (subInstance.getIsSubProcess() == Flag.NO) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE, subInstance.getName());
        }

        ProcessInstance parentWorkflowInstance = processService.findParentProcessInstance(subId);
        if (parentWorkflowInstance == null) {
            return Result.error(Status.SUB_PROCESS_INSTANCE_NOT_EXIST);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(Constants.PARENT_WORKFLOW_INSTANCE, parentWorkflowInstance.getId());
        return Result.success(dataMap);
    }

    /**
     * delete process instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> deleteProcessInstanceById(User loginUser, String projectName, Integer processInstanceId) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (null == processInstance) {
            return Result.errorWithArgs(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
        }

        processService.removeTaskLogFile(processInstanceId);
        // delete database cascade
        int delete = processService.deleteWorkProcessInstanceById(processInstanceId);

        processService.deleteAllSubWorkProcessByParentId(processInstanceId);
        processService.deleteWorkProcessMapByParentId(processInstanceId);

        if (delete > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR);
        }
    }

    /**
     * view process instance variables
     *
     * @param processInstanceId process instance id
     * @return variables data
     */
    @Override
    public Result<Map<String, Object>> viewVariables(Integer processInstanceId) {

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        Map<String, String> timeParams = BusinessTimeUtils
                .getBusinessTime(processInstance.getCmdTypeIfComplement(),
                        processInstance.getScheduleTime());

        String workflowInstanceJson = processInstance.getProcessInstanceJson();

        ProcessData workflowData = JSONUtils.parseObject(workflowInstanceJson, ProcessData.class);

        String userDefinedParams = processInstance.getGlobalParams();

        // global params
        List<Property> globalParams = new ArrayList<>();

        if (userDefinedParams != null && userDefinedParams.length() > 0) {
            globalParams = JSONUtils.toList(userDefinedParams, Property.class);
        }

        List<TaskNode> taskNodeList = workflowData.getTasks();

        // global param string
        String globalParamStr = JSONUtils.toJsonString(globalParams);
        globalParamStr = ParameterUtils.convertParameterPlaceholders(globalParamStr, timeParams);
        globalParams = JSONUtils.toList(globalParamStr, Property.class);
        for (Property property : globalParams) {
            timeParams.put(property.getProp(), property.getValue());
        }

        // local params
        Map<String, Map<String, Object>> localUserDefParams = new HashMap<>();
        for (TaskNode taskNode : taskNodeList) {
            String parameter = taskNode.getParams();
            Map<String, String> map = JSONUtils.toMap(parameter);
            String localParams = map.get(LOCAL_PARAMS);
            if (localParams != null && !localParams.isEmpty()) {
                localParams = ParameterUtils.convertParameterPlaceholders(localParams, timeParams);
                List<Property> localParamsList = JSONUtils.toList(localParams, Property.class);

                Map<String, Object> localParamsMap = new HashMap<>();
                localParamsMap.put(Constants.TASK_TYPE, taskNode.getType());
                localParamsMap.put(Constants.LOCAL_PARAMS_LIST, localParamsList);
                if (CollectionUtils.isNotEmpty(localParamsList)) {
                    localUserDefParams.put(taskNode.getName(), localParamsMap);
                }
            }

        }

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put(GLOBAL_PARAMS, globalParams);
        resultMap.put(LOCAL_PARAMS, localUserDefParams);

        return Result.success(resultMap);
    }

    /**
     * encapsulation gantt structure
     *
     * @param processInstanceId process instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    @Override
    public Result<GanttDto> viewGantt(Integer processInstanceId) throws Exception {

        ProcessInstance processInstance = processInstanceMapper.queryDetailById(processInstanceId);

        if (processInstance == null) {
            throw new RuntimeException("workflow instance is null");
        }

        GanttDto ganttDto = new GanttDto();

        DAG<String, TaskNode, TaskNodeRelation> dag = processInstance2DAG(processInstance);
        //topological sort
        List<String> nodeList = dag.topologicalSort();

        ganttDto.setTaskNames(nodeList);

        List<Task> taskList = new ArrayList<>();
        for (String node : nodeList) {
            TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(processInstanceId, node);
            if (taskInstance == null) {
                continue;
            }
            Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
            Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();
            Task task = new Task();
            task.setTaskName(taskInstance.getName());
            task.getStartDate().add(startTime.getTime());
            task.getEndDate().add(endTime.getTime());
            task.setIsoStart(startTime);
            task.setIsoEnd(endTime);
            task.setStatus(taskInstance.getState().toString());
            task.setExecutionDate(taskInstance.getStartTime());
            task.setDuration(DateUtils.format2Readable(endTime.getTime() - startTime.getTime()));
            taskList.add(task);
        }
        ganttDto.setTasks(taskList);

        return Result.success(ganttDto);
    }

    /**
     * process instance to DAG
     *
     * @param processInstance input process instance
     * @return process instance dag.
     */
    private static DAG<String, TaskNode, TaskNodeRelation> processInstance2DAG(ProcessInstance processInstance) {

        String processDefinitionJson = processInstance.getProcessInstanceJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = processData.getTasks();

        ProcessDag processDag = DagHelper.getProcessDag(taskNodeList);

        return DagHelper.buildDagGraph(processDag);
    }

    /**
     * query process instance by processDefinitionId and stateArray
     * @param processDefinitionId processDefinitionId
     * @param states states array
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryByProcessDefineIdAndStatus(int processDefinitionId, int[] states) {
        return processInstanceMapper.queryByProcessDefineIdAndStatus(processDefinitionId, states);
    }

    /**
     * query process instance by processDefinitionId
     * @param processDefinitionId processDefinitionId
     * @param size size
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryByProcessDefineId(int processDefinitionId, int size) {
        return processInstanceMapper.queryByProcessDefineId(processDefinitionId, size);
    }

}
