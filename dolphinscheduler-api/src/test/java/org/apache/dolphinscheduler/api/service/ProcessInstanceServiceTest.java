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

package org.apache.dolphinscheduler.api.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.LoggerServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProcessInstanceServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * process instance service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessInstanceServiceTest {

    @InjectMocks
    ProcessInstanceServiceImpl processInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessService processService;

    @Mock
    ProcessInstanceMapper processInstanceMapper;

    @Mock
    ProcessDefinitionMapper processDefineMapper;

    @Mock
    ProcessDefinitionService processDefinitionService;

    @Mock
    ProcessDefinitionVersionService processDefinitionVersionService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    LoggerServiceImpl loggerService;

    @Mock
    UsersService usersService;

    private String shellJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-9527\",\"name\":\"shell-1\","
            + "\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"#!/bin/bash\\necho \\\"shell-1\\\"\"},"
            + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
            + "\"timeout\":{\"strategy\":\"\",\"interval\":1,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
            + "\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

    @Test
    public void testQueryProcessInstanceList() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<PageListVO<ProcessInstance>> proejctAuthFailRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 46, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "test_user", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        // data parameter check
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Result<PageListVO<ProcessInstance>> dataParameterRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "20200101 00:00:00",
                "20200102 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) dataParameterRes.getCode());

        //project auth success
        putMsg(result, Status.SUCCESS, projectName);
        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        Page<ProcessInstance> pageReturn = new Page<>(1, 10);
        processInstanceList.add(processInstance);
        pageReturn.setRecords(processInstanceList);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(-1), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        Result<PageListVO<ProcessInstance>> successRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        // data parameter empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(-1), Mockito.any(),
                eq("192.168.xx.xx"), eq(null), eq(null))).thenReturn(pageReturn);
        successRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "",
                "", "", loginUser.getUserName(), ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        //executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Result<PageListVO<ProcessInstance>> executorExistRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "admin", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) executorExistRes.getCode());

        //executor name empty
        when(processInstanceMapper.queryProcessInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(0), Mockito.any(),
                eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Result<PageListVO<ProcessInstance>> executorEmptyRes = processInstanceService.queryProcessInstanceList(loginUser, projectName, 1, "2020-01-01 00:00:00",
                "2020-01-02 00:00:00", "", "", ExecutionStatus.SUBMITTED_SUCCESS,
                "192.168.xx.xx", 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) executorEmptyRes.getCode());

    }

    @Test
    public void testQueryTopNLongestRunningProcessInstance() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        int size = 10;
        String startTime = "2020-01-01 00:00:00";
        String endTime = "2020-08-02 00:00:00";
        Date start = DateUtils.getScheduleDate(startTime);
        Date end = DateUtils.getScheduleDate(endTime);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<List<ProcessInstance>> proejctAuthFailRes = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectName, size, startTime, endTime);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //project auth success
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        Result<List<ProcessInstance>> successRes = processInstanceService.queryTopNLongestRunningProcessInstance(loginUser, projectName, size, startTime, endTime);

        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    @Test
    public void testQueryProcessInstanceById() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<ProcessInstance> proejctAuthFailRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //project auth success
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setProcessDefinitionId(46);
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        ProcessDefinition processDefinition = getProcessDefinition();
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findProcessDefineById(processInstance.getProcessDefinitionId())).thenReturn(processDefinition);
        Result<ProcessInstance> successRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());

        //worker group null
        Result<ProcessInstance> workerNullRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) workerNullRes.getCode());

        //worker group exist
        WorkerGroup workerGroup = getWorkGroup();
        Result<ProcessInstance> workerExistRes = processInstanceService.queryProcessInstanceById(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) workerExistRes.getCode());
    }

    @Test
    public void testQueryTaskListByProcessId() throws IOException {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<Map<String, Object>> proejctAuthFailRes = processInstanceService.queryTaskListByProcessId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //project auth success
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setState(ExecutionStatus.SUCCESS);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType(TaskType.SHELL.getDescp());
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);
        Result res = new Result();
        res.setCode(Status.SUCCESS.ordinal());
        res.setData("xxx");
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(processInstance.getId())).thenReturn(processInstance);
        when(processService.findValidTaskListByProcessId(processInstance.getId())).thenReturn(taskInstanceList);
        when(loggerService.queryLog(taskInstance.getId(), 0, 4098)).thenReturn(res);
        Result<Map<String, Object>> successRes = processInstanceService.queryTaskListByProcessId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    @Test
    public void testParseLogForDependentResult() throws IOException {
        String logString = "[INFO] 2019-03-19 17:11:08.475 org.apache.dolphinscheduler.server.worker.log.TaskLogger:[172]"
                + " - [taskAppId=TASK_223_10739_452334] dependent item complete :|| 223-ALL-day-last1Day,SUCCESS\n"
                + "[INFO] 2019-03-19 17:11:08.476 org.apache.dolphinscheduler.server.worker.runner.TaskScheduleThread:[172]"
                + " - task : 223_10739_452334 exit status code : 0\n"
                + "[root@node2 current]# ";
        Map<String, DependResult> resultMap =
                processInstanceService.parseLogForDependentResult(logString);
        Assert.assertEquals(1, resultMap.size());
    }

    @Test
    public void testQuerySubProcessInstanceByTaskId() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<Map<String, Object>> proejctAuthFailRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //task null
        Project project = getProject(projectName);
        putMsg(result, Status.SUCCESS, projectName);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findTaskInstanceById(1)).thenReturn(null);
        Result<Map<String, Object>> taskNullRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, 1);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS.getCode(), (int) taskNullRes.getCode());

        //task not sub process
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setTaskType(TaskType.HTTP.toString());
        taskInstance.setProcessInstanceId(1);
        when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);
        Result<Map<String, Object>> notSubprocessRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, 1);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE.getCode(), (int) notSubprocessRes.getCode());

        //sub process not exist
        TaskInstance subTask = getTaskInstance();
        subTask.setTaskType(TaskType.SUB_PROCESS.toString());
        subTask.setProcessInstanceId(1);
        when(processService.findTaskInstanceById(subTask.getId())).thenReturn(subTask);
        when(processService.findSubProcessInstance(subTask.getProcessInstanceId(), subTask.getId())).thenReturn(null);
        Result<Map<String, Object>> subprocessNotExistRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST.getCode(), (int) subprocessNotExistRes.getCode());

        //sub process exist
        ProcessInstance processInstance = getProcessInstance();
        when(processService.findSubProcessInstance(taskInstance.getProcessInstanceId(), taskInstance.getId())).thenReturn(processInstance);
        Result<Map<String, Object>> subprocessExistRes = processInstanceService.querySubProcessInstanceByTaskId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) subprocessExistRes.getCode());
    }

    @Test
    public void testUpdateProcessInstance() throws ParseException {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<Void> proejctAuthFailRes = processInstanceService.updateProcessInstance(loginUser, projectName, 1,
                shellJson, "2020-02-21 00:00:00", true, Flag.YES, "", "");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //process instance null
        Project project = getProject(projectName);
        putMsg(result, Status.SUCCESS, projectName);
        ProcessInstance processInstance = getProcessInstance();
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
        Result<Void> processInstanceNullRes = processInstanceService.updateProcessInstance(loginUser, projectName, 1,
                shellJson, "2020-02-21 00:00:00", true, Flag.YES, "", "");
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST.getCode(), (int) processInstanceNullRes.getCode());

        //process instance not finish
        when(processService.findProcessInstanceDetailById(1)).thenReturn(processInstance);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        Result<Void> processInstanceNotFinishRes = processInstanceService.updateProcessInstance(loginUser, projectName, 1,
                shellJson, "2020-02-21 00:00:00", true, Flag.YES, "", "");
        Assert.assertEquals(Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR.getCode(), (int) processInstanceNotFinishRes.getCode());

        //process instance finish
        processInstance.setState(ExecutionStatus.SUCCESS);
        processInstance.setTimeout(3000);
        processInstance.setCommandType(CommandType.STOP);
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setUserId(1);
        Tenant tenant = new Tenant();
        tenant.setId(1);
        tenant.setTenantCode("test_tenant");
        when(processService.findProcessDefineById(processInstance.getProcessDefinitionId())).thenReturn(processDefinition);
        when(processService.getTenantForProcess(Mockito.anyInt(), Mockito.anyInt())).thenReturn(tenant);
        when(processService.updateProcessInstance(processInstance)).thenReturn(1);
        when(processDefinitionService.checkProcessNodeList(Mockito.any(), eq(shellJson))).thenReturn(result);
        when(processDefinitionVersionService.addProcessDefinitionVersion(processDefinition)).thenReturn(1L);
        Result<Void> processInstanceFinishRes = processInstanceService.updateProcessInstance(loginUser, projectName, 1,
                shellJson, "2020-02-21 00:00:00", true, Flag.YES, "", "");
        Assert.assertEquals(Status.UPDATE_PROCESS_INSTANCE_ERROR.getCode(), (int) processInstanceFinishRes.getCode());

        //success
        when(processDefineMapper.updateById(processDefinition)).thenReturn(1);
        Result<Void> successRes = processInstanceService.updateProcessInstance(loginUser, projectName, 1,
                shellJson, "2020-02-21 00:00:00", true, Flag.YES, "", "");
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    @Test
    public void testQueryParentInstanceBySubId() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Result<Map<String, Object>> proejctAuthFailRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) proejctAuthFailRes.getCode());

        //process instance null
        Project project = getProject(projectName);
        putMsg(result, Status.SUCCESS, projectName);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Result<Map<String, Object>> processInstanceNullRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_EXIST.getCode(), (int) processInstanceNullRes.getCode());

        //not sub process
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setIsSubProcess(Flag.NO);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(processInstance);
        Result<Map<String, Object>> notSubProcessRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, 1);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE.getCode(), (int) notSubProcessRes.getCode());

        //sub process
        processInstance.setIsSubProcess(Flag.YES);
        when(processService.findParentProcessInstance(1)).thenReturn(null);
        Result<Map<String, Object>> subProcessNullRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUB_PROCESS_INSTANCE_NOT_EXIST.getCode(), (int) subProcessNullRes.getCode());

        //success
        when(processService.findParentProcessInstance(1)).thenReturn(processInstance);
        Result<Map<String, Object>> successRes = processInstanceService.queryParentInstanceBySubId(loginUser, projectName, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    @Test
    public void testDeleteProcessInstanceById() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        CheckParamResult result = new CheckParamResult();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);

        //process instance null
        Project project = getProject(projectName);
        putMsg(result, Status.SUCCESS, projectName);
        when(projectMapper.queryByName(projectName)).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(processService.findProcessInstanceDetailById(1)).thenReturn(null);
    }

    @Test
    public void testViewVariables() throws Exception {
        //process instance not null
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setCommandType(CommandType.SCHEDULER);
        processInstance.setScheduleTime(new Date());
        processInstance.setProcessInstanceJson(shellJson);
        processInstance.setGlobalParams("");
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        Result<Map<String, Object>> successRes = processInstanceService.viewVariables(1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    @Test
    public void testViewGantt() throws Exception {
        ProcessInstance processInstance = getProcessInstance();
        processInstance.setProcessInstanceJson(shellJson);
        TaskInstance taskInstance = getTaskInstance();
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        when(processInstanceMapper.queryDetailById(1)).thenReturn(processInstance);
        when(taskInstanceMapper.queryByInstanceIdAndName(Mockito.anyInt(), Mockito.any())).thenReturn(taskInstance);
        Result<GanttDto> successRes = processInstanceService.viewGantt(1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) successRes.getCode());
    }

    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    /**
     * get Mock process instance
     *
     * @return process instance
     */
    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_process_instance");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        return processInstance;
    }

    /**
     * get mock processDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(46);
        processDefinition.setName("test_pdf");
        processDefinition.setProjectId(2);
        processDefinition.setTenantId(1);
        processDefinition.setDescription("");
        return processDefinition;
    }

    /**
     * get Mock worker group
     *
     * @return worker group
     */
    private WorkerGroup getWorkGroup() {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName("test_workergroup");
        return workerGroup;
    }

    /**
     * get Mock task instance
     *
     * @return task instance
     */
    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setExecutorId(-1);
        return taskInstance;
    }

    private void putMsg(CheckParamResult result, Status status, Object... statusParams) {
        result.setStatus(status);
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

}