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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProcessDefinitionServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * process definition service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessDefinitionServiceTest {

    private static final String SHELL_JSON = "{\n"
            + "    \"globalParams\": [\n"
            + "        \n"
            + "    ],\n"
            + "    \"tasks\": [\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9527\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \n"
            + "            ]\n"
            + "        }\n"
            + "    ],\n"
            + "    \"tenantId\": 1,\n"
            + "    \"timeout\": 0\n"
            + "}";

    private static final String CYCLE_SHELL_JSON = "{\n"
            + "    \"globalParams\": [\n"
            + "        \n"
            + "    ],\n"
            + "    \"tasks\": [\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9527\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9529\"\n"
            + "            ]\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9528\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9527\"\n"
            + "            ]\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"SHELL\",\n"
            + "            \"id\": \"tasks-9529\",\n"
            + "            \"name\": \"shell-1\",\n"
            + "            \"params\": {\n"
            + "                \"resourceList\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"localParams\": [\n"
            + "                    \n"
            + "                ],\n"
            + "                \"rawScript\": \"#!/bin/bash\\necho \\\"shell-1\\\"\"\n"
            + "            },\n"
            + "            \"description\": \"\",\n"
            + "            \"runFlag\": \"NORMAL\",\n"
            + "            \"dependence\": {\n"
            + "                \n"
            + "            },\n"
            + "            \"maxRetryTimes\": \"0\",\n"
            + "            \"retryInterval\": \"1\",\n"
            + "            \"timeout\": {\n"
            + "                \"strategy\": \"\",\n"
            + "                \"interval\": 1,\n"
            + "                \"enable\": false\n"
            + "            },\n"
            + "            \"taskInstancePriority\": \"MEDIUM\",\n"
            + "            \"workerGroupId\": -1,\n"
            + "            \"preTasks\": [\n"
            + "                \"tasks-9528\"\n"
            + "            ]\n"
            + "        }\n"
            + "    ],\n"
            + "    \"tenantId\": 1,\n"
            + "    \"timeout\": 0\n"
            + "}";

    @InjectMocks
    private ProcessDefinitionServiceImpl processDefinitionService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessInstanceService processInstanceService;

    @Mock
    private TaskInstanceMapper taskInstanceMapper;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Test
    public void testQueryProcessDefinitionList() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionList(loginUser, "project_test1");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        List<ProcessDefinition> resourceList = new ArrayList<>();
        resourceList.add(getProcessDefinition());
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(project.getCode())).thenReturn(resourceList);
        Map<String, Object> checkSuccessRes = processDefinitionService.queryProcessDefinitionList(loginUser, "project_test1");
        Assert.assertEquals(Status.SUCCESS, checkSuccessRes.get(Constants.STATUS));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryProcessDefinitionListPaging() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project not found
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, "project_test1", "", 1, 5, 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        putMsg(result, Status.SUCCESS, projectName);
        loginUser.setId(1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Page<ProcessDefinition> page = new Page<>(1, 10);
        page.setTotal(30);
        Mockito.when(processDefinitionMapper.queryDefineListPaging(
                Mockito.any(IPage.class)
                , Mockito.eq("")
                , Mockito.eq(loginUser.getId())
                , Mockito.eq(project.getCode())
                , Mockito.anyBoolean())).thenReturn(page);

        Map<String, Object> map1 = processDefinitionService.queryProcessDefinitionListPaging(
                loginUser, projectName, "", 1, 10, loginUser.getId());

        Assert.assertEquals(Status.SUCCESS, map1.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionById() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefinitionMapper.selectById(1)).thenReturn(null);

        String processDefinitionJson = "{\"globalParams\":[],\"tasks\":[{\"conditionResult\":"
                + "{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\",\"dependence\":{}"
                + ",\"description\":\"\",\"id\":\"tasks-3011\",\"maxRetryTimes\":\"0\",\"name\":\"tsssss\""
                + ",\"params\":{\"localParams\":[],\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[]}"
                + ",\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\""
                + ",\"timeout\":{\"enable\":false,\"interval\":null,\"strategy\":\"\"},\"type\":\"SHELL\""
                + ",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],\"tenantId\":4,\"timeout\":0}";
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Mockito.when(processService.genProcessData(Mockito.any())).thenReturn(processData);

        Map<String, Object> instanceNotexitRes = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        //instance exit
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(getProcessDefinition());
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testQueryProcessDefinitionByName() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project check auth fail
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionByName(loginUser,
                "project_test1", "test_def");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), "test_def")).thenReturn(null);

        ProcessData processData = getProcessData();
        Mockito.when(processService.genProcessData(Mockito.any())).thenReturn(processData);
        Map<String, Object> instanceNotexitRes = processDefinitionService.queryProcessDefinitionByName(loginUser,
                "project_test1", "test_def");
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        //instance exit
        Mockito.when(processDefinitionMapper.queryByDefineName(project.getCode(), "test")).thenReturn(getProcessDefinition());
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionByName(loginUser,
                "project_test1", "test");
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testBatchCopyProcessDefinition() {

        String projectName = "project_test1";
        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        // copy project definition ids empty test
        Map<String, Object> map = processDefinitionService.batchCopyProcessDefinition(loginUser, projectName, StringUtils.EMPTY, 0);
        Assert.assertEquals(Status.PROCESS_DEFINITION_IDS_IS_EMPTY, map.get(Constants.STATUS));

        Map<String, Object> result = new HashMap<>();

        // project check auth fail
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map1 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, String.valueOf(project.getId()), 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map1.get(Constants.STATUS));

        // project check auth success, target project is null
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(projectMapper.queryDetailById(0)).thenReturn(null);
        Map<String, Object> map2 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, String.valueOf(project.getId()), 0);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map2.get(Constants.STATUS));

        // project check auth success, target project name not equal project name, check auth target project fail
        Project project1 = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project1);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);

        putMsg(result, Status.SUCCESS, projectName);
        String projectName2 = "project_test2";
        Project project2 = getProject(projectName2);
        Mockito.when(projectMapper.queryByName(projectName2)).thenReturn(project2);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project2, projectName2)).thenReturn(result);
        Mockito.when(projectMapper.queryDetailById(1)).thenReturn(project2);
        // instance exit
        ProcessDefinition definition = getProcessDefinition();
        definition.setLocations("{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}");
        definition.setConnects("[]");

        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(definition);
        Mockito.when(processService.genProcessData(Mockito.any())).thenReturn(getProcessData());

        Map<String, Object> map3 = processDefinitionService.batchCopyProcessDefinition(
                loginUser, projectName, "46", 1);
        Assert.assertEquals(Status.COPY_PROCESS_DEFINITION_ERROR, map3.get(Constants.STATUS));
    }

    @Test
    public void testBatchMoveProcessDefinition() {
        String projectName = "project_test1";
        Project project1 = getProject(projectName);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project1);

        String projectName2 = "project_test2";
        Project project2 = getProject(projectName2);
        Mockito.when(projectMapper.queryByName(projectName2)).thenReturn(project2);

        int targetProjectId = 2;
        Mockito.when(projectMapper.queryDetailById(targetProjectId)).thenReturn(getProjectById(targetProjectId));

        Project project = getProject(projectName);
        Project targetProject = getProjectById(targetProjectId);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectName);

        Map<String, Object> result2 = new HashMap<>();
        putMsg(result2, Status.SUCCESS, targetProject.getName());

        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project2, projectName2)).thenReturn(result);

        ProcessDefinition definition = getProcessDefinition();
        definition.setLocations("{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}");
        definition.setConnects("[]");

        putMsg(result, Status.SUCCESS);

        Map<String, Object> successRes = processDefinitionService.batchMoveProcessDefinition(
                loginUser, "project_test1", "46", 2);

        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, "project_test1", 6);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, instance not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processDefinitionMapper.selectById(1)).thenReturn(null);
        Map<String, Object> instanceNotexitRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 1);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, instanceNotexitRes.get(Constants.STATUS));

        ProcessDefinition processDefinition = getProcessDefinition();
        //user no auth
        loginUser.setUserType(UserType.GENERAL_USER);
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> userNoAuthRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM, userNoAuthRes.get(Constants.STATUS));

        //process definition online
        loginUser.setUserType(UserType.ADMIN_USER);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition);
        Map<String, Object> dfOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.PROCESS_DEFINE_STATE_ONLINE, dfOnlineRes.get(Constants.STATUS));

        //scheduler list elements > 1
        processDefinition.setReleaseState(ReleaseState.OFFLINE);
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition);
        List<Schedule> schedules = new ArrayList<>();
        schedules.add(getSchedule());
        schedules.add(getSchedule());
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerGreaterThanOneRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, schedulerGreaterThanOneRes.get(Constants.STATUS));

        //scheduler online
        schedules.clear();
        Schedule schedule = getSchedule();
        schedule.setReleaseState(ReleaseState.ONLINE);
        schedules.add(schedule);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Map<String, Object> schedulerOnlineRes = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SCHEDULE_CRON_STATE_ONLINE, schedulerOnlineRes.get(Constants.STATUS));

        //delete fail
        schedules.clear();
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedules.add(schedule);
        Mockito.when(scheduleMapper.queryByProcessDefinitionId(46)).thenReturn(schedules);
        Mockito.when(processDefinitionMapper.deleteById(46)).thenReturn(0);
        Map<String, Object> deleteFail = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR, deleteFail.get(Constants.STATUS));

        //delete success
        Mockito.when(processDefinitionMapper.deleteById(46)).thenReturn(1);
        Map<String, Object> deleteSuccess = processDefinitionService.deleteProcessDefinitionById(loginUser,
                "project_test1", 46);
        Assert.assertEquals(Status.SUCCESS, deleteSuccess.get(Constants.STATUS));
    }

    @Test
    public void testReleaseProcessDefinition() {

        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.releaseProcessDefinition(loginUser, "project_test1",
                6, ReleaseState.OFFLINE);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        // project check auth success, processs definition online
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(getProcessDefinition());
        Map<String, Object> onlineRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineRes.get(Constants.STATUS));

        // project check auth success, processs definition online
        ProcessDefinition processDefinition1 = getProcessDefinition();
        processDefinition1.setResourceIds("1,2");
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition1);
        Mockito.when(processService.getUserById(1)).thenReturn(loginUser);
        Map<String, Object> onlineWithResourceRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, ReleaseState.ONLINE);
        Assert.assertEquals(Status.SUCCESS, onlineWithResourceRes.get(Constants.STATUS));

        // release error code
        Map<String, Object> failRes = processDefinitionService.releaseProcessDefinition(
                loginUser, "project_test1", 46, ReleaseState.getEnum(2));
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, failRes.get(Constants.STATUS));

    }

    @Test
    public void testVerifyProcessDefinitionName() {
        String projectName = "project_test1";
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));

        Project project = getProject(projectName);
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        //project check auth fail
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> map = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));

        //project check auth success, process not exist
        putMsg(result, Status.SUCCESS, projectName);
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.SUCCESS, processNotExistRes.get(Constants.STATUS));

        //process exist
        Mockito.when(processDefinitionMapper.verifyByDefineName(project.getCode(), "test_pdf")).thenReturn(getProcessDefinition());
        Map<String, Object> processExistRes = processDefinitionService.verifyProcessDefinitionName(loginUser,
                "project_test1", "test_pdf");
        Assert.assertEquals(Status.PROCESS_DEFINITION_NAME_EXIST, processExistRes.get(Constants.STATUS));
    }

    @Test
    public void testCheckProcessNodeList() {

        Map<String, Object> dataNotValidRes = processDefinitionService.checkProcessNodeList(null, "");
        Assert.assertEquals(Status.DATA_IS_NOT_VALID, dataNotValidRes.get(Constants.STATUS));

        // task not empty
        String processDefinitionJson = SHELL_JSON;
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Assert.assertNotNull(processData);
        Map<String, Object> taskEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.SUCCESS, taskEmptyRes.get(Constants.STATUS));

        // task empty
        processData.setTasks(null);
        Map<String, Object> taskNotEmptyRes = processDefinitionService.checkProcessNodeList(processData, processDefinitionJson);
        Assert.assertEquals(Status.PROCESS_DAG_IS_EMPTY, taskNotEmptyRes.get(Constants.STATUS));

        // task cycle
        String processDefinitionJsonCycle = CYCLE_SHELL_JSON;
        ProcessData processDataCycle = JSONUtils.parseObject(processDefinitionJsonCycle, ProcessData.class);
        Map<String, Object> taskCycleRes = processDefinitionService.checkProcessNodeList(processDataCycle, processDefinitionJsonCycle);
        Assert.assertEquals(Status.PROCESS_NODE_HAS_CYCLE, taskCycleRes.get(Constants.STATUS));

        //json abnormal
        String abnormalJson = processDefinitionJson.replaceAll(TaskType.SHELL.getDesc(), "");
        processData = JSONUtils.parseObject(abnormalJson, ProcessData.class);
        Map<String, Object> abnormalTaskRes = processDefinitionService.checkProcessNodeList(processData, abnormalJson);
        Assert.assertEquals(Status.PROCESS_NODE_S_PARAMETER_INVALID, abnormalTaskRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionId() {
        //process definition not exist
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.getTaskNodeListByDefinitionCode(46L);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        //process data null
        ProcessDefinition processDefinition = getProcessDefinition();
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        Map<String, Object> successRes = processDefinitionService.getTaskNodeListByDefinitionCode(46L);
        Assert.assertEquals(Status.DATA_IS_NOT_VALID, successRes.get(Constants.STATUS));

        //success
        Mockito.when(processService.genProcessData(Mockito.any())).thenReturn(new ProcessData());
        Mockito.when(processDefinitionMapper.queryByCode(46L)).thenReturn(processDefinition);
        Map<String, Object> dataNotValidRes = processDefinitionService.getTaskNodeListByDefinitionCode(46L);
        Assert.assertEquals(Status.SUCCESS, dataNotValidRes.get(Constants.STATUS));
    }

    @Test
    public void testGetTaskNodeListByDefinitionIdList() {
        //process definition not exist
        String defineCodeList = "46";
        Long[] codeArray = {46L};
        List<Long> codeList = Arrays.asList(codeArray);
        Mockito.when(processDefinitionMapper.queryByCodes(codeList)).thenReturn(null);
        Map<String, Object> processNotExistRes = processDefinitionService.getTaskNodeListByDefinitionCodeList(defineCodeList);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processNotExistRes.get(Constants.STATUS));

        //process definition exist
        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Mockito.when(processDefinitionMapper.queryByCodes(codeList)).thenReturn(processDefinitionList);
        ProcessData processData = getProcessData();
        Mockito.when(processService.genProcessData(processDefinition)).thenReturn(processData);

        Map<String, Object> successRes = processDefinitionService.getTaskNodeListByDefinitionCodeList(defineCodeList);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    private ProcessData getProcessData() {
        ProcessData processData = new ProcessData();
        List<TaskNode> taskNodeList = new ArrayList<>();
        processData.setTasks(taskNodeList);
        List<Property> properties = new ArrayList<>();
        processData.setGlobalParams(properties);
        processData.setTenantId(10);
        processData.setTimeout(100);
        return processData;
    }

    @Test
    public void testQueryProcessDefinitionAllByProjectId() {
        int projectId = 1;
        Long projectCode = 2L;
        Project project = new Project();
        project.setId(projectId);
        project.setCode(projectCode);
        Mockito.when(projectMapper.selectById(projectId)).thenReturn(project);

        ProcessDefinition processDefinition = getProcessDefinition();
        List<ProcessDefinition> processDefinitionList = new ArrayList<>();
        processDefinitionList.add(processDefinition);
        Project test = getProject("test");
        Mockito.when(projectMapper.selectById(projectId)).thenReturn(test);
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(test.getCode())).thenReturn(processDefinitionList);
        Map<String, Object> successRes = processDefinitionService.queryProcessDefinitionAllByProjectId(projectId);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }

    @Test
    public void testViewTree() throws Exception {
        //process definition not exist
        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(null);
        Map<String, Object> processDefinitionNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionNullRes.get(Constants.STATUS));

        List<ProcessInstance> processInstanceList = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_instance");
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setHost("192.168.xx.xx");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstanceList.add(processInstance);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setTaskType(TaskType.SHELL.getDesc());
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("192.168.xx.xx");

        //task instance not exist
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNullRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNullRes.get(Constants.STATUS));

        //task instance exist
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));

    }

    @Test
    public void testSubProcessViewTree() throws Exception {

        ProcessDefinition processDefinition = getProcessDefinition();
        processDefinition.setProcessDefinitionJson(SHELL_JSON);
        List<ProcessInstance> processInstanceList = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test_instance");
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setHost("192.168.xx.xx");
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstanceList.add(processInstance);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setStartTime(new Date());
        taskInstance.setEndTime(new Date());
        taskInstance.setTaskType(TaskType.SUB_PROCESS.getDesc());
        taskInstance.setId(1);
        taskInstance.setName("test_task_instance");
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setHost("192.168.xx.xx");
        taskInstance.setTaskParams("\"processDefinitionId\": \"222\",\n");
        Mockito.when(processDefinitionMapper.selectById(46)).thenReturn(processDefinition);
        Mockito.when(processService.genDagGraph(processDefinition)).thenReturn(new DAG<>());
        Map<String, Object> taskNotNuLLRes = processDefinitionService.viewTree(46, 10);
        Assert.assertEquals(Status.SUCCESS, taskNotNuLLRes.get(Constants.STATUS));

    }

    @Test
    public void testImportProcessDefinitionById() throws IOException {

        String processJson = "[\n"
                + "    {\n"
                + "        \"projectName\": \"testProject\",\n"
                + "        \"processDefinitionName\": \"shell-4\",\n"
                + "        \"processDefinitionJson\": \"{\\\"tenantId\\\":1"
                + ",\\\"globalParams\\\":[],\\\"tasks\\\":[{\\\"workerGroupId\\\":\\\"3\\\",\\\"description\\\""
                + ":\\\"\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SHELL\\\",\\\"params\\\":{\\\"rawScript\\\""
                + ":\\\"#!/bin/bash\\\\necho \\\\\\\"shell-4\\\\\\\"\\\",\\\"localParams\\\":[],\\\"resourceList\\\":[]}"
                + ",\\\"timeout\\\":{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"},\\\"maxRetryTimes\\\":\\\"0\\\""
                + ",\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-4\\\",\\\"dependence\\\":{}"
                + ",\\\"retryInterval\\\":\\\"1\\\",\\\"preTasks\\\":[],\\\"id\\\":\\\"tasks-84090\\\"}"
                + ",{\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"name\\\":\\\"shell-5\\\",\\\"workerGroupId\\\""
                + ":\\\"3\\\",\\\"description\\\":\\\"\\\",\\\"dependence\\\":{},\\\"preTasks\\\":[\\\"shell-4\\\"]"
                + ",\\\"id\\\":\\\"tasks-87364\\\",\\\"runFlag\\\":\\\"NORMAL\\\",\\\"type\\\":\\\"SUB_PROCESS\\\""
                + ",\\\"params\\\":{\\\"processDefinitionId\\\":46},\\\"timeout\\\":{\\\"enable\\\":false"
                + ",\\\"strategy\\\":\\\"\\\"}}],\\\"timeout\\\":0}\",\n"
                + "        \"processDefinitionDescription\": \"\",\n"
                + "        \"processDefinitionLocations\": \"{\\\"tasks-84090\\\":{\\\"name\\\":\\\"shell-4\\\""
                + ",\\\"targetarr\\\":\\\"\\\",\\\"x\\\":128,\\\"y\\\":114},\\\"tasks-87364\\\":{\\\"name\\\""
                + ":\\\"shell-5\\\",\\\"targetarr\\\":\\\"tasks-84090\\\",\\\"x\\\":266,\\\"y\\\":115}}\",\n"
                + "        \"processDefinitionConnects\": \"[{\\\"endPointSourceId\\\":\\\"tasks-84090\\\""
                + ",\\\"endPointTargetId\\\":\\\"tasks-87364\\\"}]\"\n"
                + "    }\n"
                + "]";

        String subProcessJson = "{\n"
                + "    \"globalParams\": [\n"
                + "        \n"
                + "    ],\n"
                + "    \"tasks\": [\n"
                + "        {\n"
                + "            \"type\": \"SHELL\",\n"
                + "            \"id\": \"tasks-52423\",\n"
                + "            \"name\": \"shell-5\",\n"
                + "            \"params\": {\n"
                + "                \"resourceList\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"localParams\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"rawScript\": \"echo \\\"shell-5\\\"\"\n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"interval\": null,\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": \"3\",\n"
                + "            \"preTasks\": [\n"
                + "                \n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"tenantId\": 1,\n"
                + "    \"timeout\": 0\n"
                + "}";

        FileUtils.writeStringToFile(new File("/tmp/task.json"), processJson);

        File file = new File("/tmp/task.json");

        FileInputStream fileInputStream = new FileInputStream("/tmp/task.json");

        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        String currentProjectName = "testProject";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, currentProjectName);

        ProcessDefinition shellDefinition2 = new ProcessDefinition();
        shellDefinition2.setId(46);
        shellDefinition2.setName("shell-5");
        shellDefinition2.setProjectId(2);
        shellDefinition2.setProcessDefinitionJson(subProcessJson);

        Mockito.when(projectMapper.queryByName(currentProjectName)).thenReturn(getProject(currentProjectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, getProject(currentProjectName), currentProjectName)).thenReturn(result);

        Map<String, Object> importProcessResult = processDefinitionService.importProcessDefinition(loginUser, multipartFile, currentProjectName);

        Assert.assertEquals(Status.SUCCESS, importProcessResult.get(Constants.STATUS));

        boolean delete = file.delete();

        Assert.assertTrue(delete);
    }

    @Test
    public void testUpdateProcessDefinition() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        ProcessDefinition processDefinition = getProcessDefinition();

        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Mockito.when(processService.findProcessDefineById(1)).thenReturn(processDefinition);

        String sqlDependentJson = "{\n"
                + "    \"globalParams\": [\n"
                + "        \n"
                + "    ],\n"
                + "    \"tasks\": [\n"
                + "        {\n"
                + "            \"type\": \"SQL\",\n"
                + "            \"id\": \"tasks-27297\",\n"
                + "            \"name\": \"sql\",\n"
                + "            \"params\": {\n"
                + "                \"type\": \"MYSQL\",\n"
                + "                \"datasource\": 1,\n"
                + "                \"sql\": \"select * from test\",\n"
                + "                \"udfs\": \"\",\n"
                + "                \"sqlType\": \"1\",\n"
                + "                \"title\": \"\",\n"
                + "                \"receivers\": \"\",\n"
                + "                \"receiversCc\": \"\",\n"
                + "                \"showType\": \"TABLE\",\n"
                + "                \"localParams\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"connParams\": \"\",\n"
                + "                \"preStatements\": [\n"
                + "                    \n"
                + "                ],\n"
                + "                \"postStatements\": [\n"
                + "                    \n"
                + "                ]\n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": -1,\n"
                + "            \"preTasks\": [\n"
                + "                \"dependent\"\n"
                + "            ]\n"
                + "        },\n"
                + "        {\n"
                + "            \"type\": \"DEPENDENT\",\n"
                + "            \"id\": \"tasks-33787\",\n"
                + "            \"name\": \"dependent\",\n"
                + "            \"params\": {\n"
                + "                \n"
                + "            },\n"
                + "            \"description\": \"\",\n"
                + "            \"runFlag\": \"NORMAL\",\n"
                + "            \"dependence\": {\n"
                + "                \"relation\": \"AND\",\n"
                + "                \"dependTaskList\": [\n"
                + "                    {\n"
                + "                        \"relation\": \"AND\",\n"
                + "                        \"dependItemList\": [\n"
                + "                            {\n"
                + "                                \"projectId\": 2,\n"
                + "                                \"definitionId\": 46,\n"
                + "                                \"depTasks\": \"ALL\",\n"
                + "                                \"cycle\": \"day\",\n"
                + "                                \"dateValue\": \"today\"\n"
                + "                            }\n"
                + "                        ]\n"
                + "                    }\n"
                + "                ]\n"
                + "            },\n"
                + "            \"maxRetryTimes\": \"0\",\n"
                + "            \"retryInterval\": \"1\",\n"
                + "            \"timeout\": {\n"
                + "                \"strategy\": \"\",\n"
                + "                \"enable\": false\n"
                + "            },\n"
                + "            \"taskInstancePriority\": \"MEDIUM\",\n"
                + "            \"workerGroupId\": -1,\n"
                + "            \"preTasks\": [\n"
                + "                \n"
                + "            ]\n"
                + "        }\n"
                + "    ],\n"
                + "    \"tenantId\": 1,\n"
                + "    \"timeout\": 0\n"
                + "}";

        Map<String, Object> updateResult = processDefinitionService.updateProcessDefinition(loginUser, projectName, 1, "test",
                sqlDependentJson, "", "", "");

        Assert.assertEquals(Status.UPDATE_PROCESS_DEFINITION_ERROR, updateResult.get(Constants.STATUS));
    }

    @Test
    public void testBatchExportProcessDefinitionByIds() throws IOException {
        processDefinitionService.batchExportProcessDefinitionByIds(
                null, null, null, null);

        String processDefinitionJson = "{\"globalParams\":[],\"tasks\":[{\"conditionResult\":"
                + "{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\",\"dependence\":{}"
                + ",\"description\":\"\",\"id\":\"tasks-3011\",\"maxRetryTimes\":\"0\",\"name\":\"tsssss\""
                + ",\"params\":{\"localParams\":[],\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[]}"
                + ",\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\""
                + ",\"timeout\":{\"enable\":false,\"interval\":null,\"strategy\":\"\"},\"type\":\"SHELL\""
                + ",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],\"tenantId\":4,\"timeout\":0}";
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);

        String projectName = "project_test1";
        Project project = getProject(projectName);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject(projectName));
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);

        processDefinitionService.batchExportProcessDefinitionByIds(
                loginUser, projectName, "1", null);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        processDefinition.setProcessDefinitionJson(processDefinitionJson);
        Map<String, Object> checkResult = new HashMap<>();
        checkResult.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(checkResult);
        Mockito.when(processDefinitionMapper.queryByDefineId(1)).thenReturn(processDefinition);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        Mockito.when(processService.genProcessData(processDefinition)).thenReturn(processData);

        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        processDefinitionService.batchExportProcessDefinitionByIds(
                loginUser, projectName, "1", response);
        Assert.assertNotNull(processDefinitionService.exportProcessMetaData(processDefinition));
    }

    /**
     * get mock datasource
     *
     * @return DataSource
     */
    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setId(2);
        dataSource.setName("test");
        return dataSource;
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
        processDefinition.setCode(9999L);

        return processDefinition;
    }

    /**
     * get mock Project
     *
     * @param projectName projectName
     * @return Project
     */
    private Project getProject(String projectName) {
        Project project = new Project();
        project.setCode(1L);
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    /**
     * get mock Project
     *
     * @param projectId projectId
     * @return Project
     */
    private Project getProjectById(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setName("project_test2");
        project.setUserId(1);
        return project;
    }

    /**
     * get mock schedule
     *
     * @return schedule
     */
    private Schedule getSchedule() {
        Date date = new Date();
        Schedule schedule = new Schedule();
        schedule.setId(46);
        schedule.setProcessDefinitionId(1);
        schedule.setStartTime(date);
        schedule.setEndTime(date);
        schedule.setCrontab("0 0 5 * * ? *");
        schedule.setFailureStrategy(FailureStrategy.END);
        schedule.setUserId(1);
        schedule.setReleaseState(ReleaseState.OFFLINE);
        schedule.setProcessInstancePriority(Priority.MEDIUM);
        schedule.setWarningType(WarningType.NONE);
        schedule.setWarningGroupId(1);
        schedule.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        return schedule;
    }

    /**
     * get mock processMeta
     *
     * @return processMeta
     */
    private ProcessMeta getProcessMeta() {
        ProcessMeta processMeta = new ProcessMeta();
        Schedule schedule = getSchedule();
        processMeta.setScheduleCrontab(schedule.getCrontab());
        processMeta.setScheduleStartTime(DateUtils.dateToString(schedule.getStartTime()));
        processMeta.setScheduleEndTime(DateUtils.dateToString(schedule.getEndTime()));
        processMeta.setScheduleWarningType(String.valueOf(schedule.getWarningType()));
        processMeta.setScheduleWarningGroupId(schedule.getWarningGroupId());
        processMeta.setScheduleFailureStrategy(String.valueOf(schedule.getFailureStrategy()));
        processMeta.setScheduleReleaseState(String.valueOf(schedule.getReleaseState()));
        processMeta.setScheduleProcessInstancePriority(String.valueOf(schedule.getProcessInstancePriority()));
        processMeta.setScheduleWorkerGroupName("workgroup1");
        return processMeta;
    }

    private List<Schedule> getSchedulerList() {
        List<Schedule> scheduleList = new ArrayList<>();
        scheduleList.add(getSchedule());
        return scheduleList;
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public void testImportProcessSchedule() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        Integer processDefinitionId = 111;
        String processDefinitionName = "testProcessDefinition";
        String projectName = "project_test1";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT);
        ProcessMeta processMeta = new ProcessMeta();
        Assert.assertEquals(0, processDefinitionService.importProcessSchedule(loginUser, projectName, processMeta, processDefinitionName, processDefinitionId));
    }

    @Test
    public void testAddExportTaskNodeSpecialParam() {
        String sqlDependentJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SQL\",\"id\":\"tasks-27297\",\"name\":"
                + "\"sql\",\"params\":{\"type\":\"MYSQL\",\"datasource\":1,\"sql\":\"select * from test\",\"udfs\":\"\""
                + ",\"sqlType\":\"1\",\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"showType\":\"TABLE\","
                + "\"localParams\":[],\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[]},\"description\":"
                + "\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\","
                + "\"timeout\":{\"strategy\":\"\",\"enable\":false},\"taskInstancePriority\":\"MEDIUM\","
                + "\"workerGroupId\":-1,\"preTasks\":[\"dependent\"]},{\"type\":\"DEPENDENT\",\"id\":\"tasks-33787\","
                + "\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":"
                + "{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"projectId\":"
                + "2,\"definitionId\":46,\"depTasks\":\"ALL\",\"cycle\":\"day\",\"dateValue\":\"today\"}]}]},"
                + "\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":1,"
                + "\"timeout\":0}";

        ProcessData processData = JSONUtils.parseObject(sqlDependentJson, ProcessData.class);

        DataSource dataSource = new DataSource();
        dataSource.setName("testDataSource");
        when(dataSourceMapper.selectById(1)).thenReturn(dataSource);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectName("testProjectName");
        processDefinition.setName("testDefinitionName");
        when(processDefinitionMapper.queryByDefineId(46)).thenReturn(processDefinition);

        try {
            Class clazz = ProcessDefinitionServiceImpl.class;
            Method method = clazz.getDeclaredMethod("addExportTaskNodeSpecialParam", ProcessData.class);
            method.setAccessible(true);
            method.invoke(processDefinitionService, processData);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }

        List<TaskNode> taskNodeList = processData.getTasks();
        ObjectNode sqlParameters = JSONUtils.parseObject(taskNodeList.get(0).getParams());
        Assert.assertEquals("testDataSource", sqlParameters.get(Constants.TASK_PARAMS_DATASOURCE_NAME).asText());

        ObjectNode dependentParameters = JSONUtils.parseObject(taskNodeList.get(1).getDependence());
        ArrayNode dependTaskList = (ArrayNode)dependentParameters.get(Constants.TASK_DEPENDENCE_DEPEND_TASK_LIST);
        JsonNode dependentTaskModel = dependTaskList.path(0);
        ArrayNode dependItemList = (ArrayNode)dependentTaskModel.get(Constants.TASK_DEPENDENCE_DEPEND_ITEM_LIST);
        ObjectNode dependentItem = (ObjectNode)dependItemList.path(0);
        Assert.assertEquals("testProjectName", dependentItem.get(Constants.TASK_DEPENDENCE_PROJECT_NAME).asText());
        Assert.assertEquals("testDefinitionName", dependentItem.get(Constants.TASK_DEPENDENCE_DEFINITION_NAME).asText());
    }

    @Test
    public void testAddImportTaskNodeSpecialParam() {
        String definitionJson = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SQL\",\"id\":\"tasks-27297\",\"name\":\"sql\","
                + "\"params\":{\"type\":\"MYSQL\",\"datasourceName\":\"testDataSource\",\"sql\":\"select * from test\","
                + "\"udfs\":\"\",\"sqlType\":\"1\",\"title\":\"\",\"receivers\":\"\",\"receiversCc\":\"\",\"showType\":"
                + "\"TABLE\",\"localParams\":[],\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[]},"
                + "\"description\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\","
                + "\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"enable\":false},\"taskInstancePriority\":"
                + "\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[\"dependent\"]},{\"type\":\"DEPENDENT\",\"id\":"
                + "\"tasks-33787\",\"name\":\"dependent\",\"params\":{},\"description\":\"\",\"runFlag\":\"NORMAL\","
                + "\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":"
                + "[{\"projectName\":\"testProjectName\",\"definitionName\":\"testDefinitionName\",\"depTasks\":\"ALL\""
                + ",\"cycle\":\"day\",\"dateValue\":\"today\"}]}]},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\""
                + "timeout\":{\"strategy\":\"\",\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\""
                + ":-1,\"preTasks\":[]}],\"tenantId\":1,\"timeout\":0}";

        ObjectNode jsonObject = JSONUtils.parseObject(definitionJson);
        ArrayNode jsonArray = (ArrayNode) jsonObject.get("tasks");

        List<DataSource> dataSources = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setId(1);
        dataSources.add(dataSource);
        when(dataSourceMapper.queryDataSourceByName("testDataSource")).thenReturn(dataSources);

        Project project = new Project();
        project.setId(1);
        project.setCode(1L);
        when(projectMapper.queryByName("testProjectName")).thenReturn(project);

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(1);
        when(processDefinitionMapper.queryByDefineName(1L, "testDefinitionName")).thenReturn(processDefinition);

        try {
            Class clazz = ProcessDefinitionServiceImpl.class;
            Method method = clazz.getDeclaredMethod("addImportTaskNodeSpecialParam", ArrayNode.class);
            method.setAccessible(true);
            method.invoke(processDefinitionService, jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.toString());
        }

        ObjectNode sqlParameters = (ObjectNode)jsonArray.path(0).path(Constants.TASK_PARAMS);
        Assert.assertEquals(1, sqlParameters.get(Constants.TASK_PARAMS_DATASOURCE).asInt());

        ObjectNode dependentParameters = (ObjectNode)jsonArray.path(1).path(Constants.DEPENDENCE);
        ArrayNode dependTaskList = (ArrayNode)dependentParameters.get(Constants.TASK_DEPENDENCE_DEPEND_TASK_LIST);
        JsonNode dependentTaskModel = dependTaskList.path(0);
        ArrayNode dependItemList = (ArrayNode)dependentTaskModel.get(Constants.TASK_DEPENDENCE_DEPEND_ITEM_LIST);
        ObjectNode dependentItem = (ObjectNode)dependItemList.path(0);
        Assert.assertEquals(1, dependentItem.get(Constants.TASK_DEPENDENCE_PROJECT_ID).asInt());
        Assert.assertEquals(1, dependentItem.get(Constants.TASK_DEPENDENCE_DEFINITION_ID).asInt());
    }

}
