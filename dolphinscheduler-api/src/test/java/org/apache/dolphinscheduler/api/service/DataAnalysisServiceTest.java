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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.dto.TaskStateCount;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DataAnalysisServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * data analysis service test
 */
@RunWith(PowerMockRunner.class)
public class DataAnalysisServiceTest {

    @InjectMocks
    private DataAnalysisServiceImpl dataAnalysisService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessInstanceMapper processInstanceMapper;

    @Mock
    ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    CommandMapper commandMapper;

    @Mock
    ErrorCommandMapper errorCommandMapper;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    ProcessService processService;

    private User user;

    @Before
    public void setUp() {

        user = new User();
        Project project = new Project();
        project.setId(1);
        Mockito.when(projectMapper.selectById(1)).thenReturn(project);
        Mockito.when(projectService.hasProjectAndPerm(user, project)).thenReturn(new CheckParamResult(Status.SUCCESS));

    }


    @After
    public void after() {

        user = null;
        projectMapper = null;
    }

    @Test
    public void testCountTaskStateByProject() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";

        //checkProject false
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.USER_NO_OPERATION_PROJECT_PERM));
        Result<TaskCountDto> result = dataAnalysisService.countTaskStateByProject(user, 2, startDate, endDate);
        Assert.assertNotEquals(Status.SUCCESS.getCode(), (int) result.getCode());


        //SUCCESS
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(getTaskInstanceStateCounts());

        result = dataAnalysisService.countTaskStateByProject(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

        // when date in illegal format then return error message
        String startDate2 = "illegalDateString";
        String endDate2 = "illegalDateString";
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate2, endDate2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when one of date in illegal format then return error message
        String startDate3 = "2020-08-28 14:13:40";
        String endDate3 = "illegalDateString";
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate3, endDate3);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when one of date in illegal format then return error message
        String startDate4 = "illegalDateString";
        String endDate4 = "2020-08-28 14:13:40";
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate4, endDate4);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when counting general user's task status then return user's task status count
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(Collections.singletonList(123));
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        executeStatusCount.setCount(10);
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.singletonList(executeStatusCount));
        result = dataAnalysisService.countTaskStateByProject(user, 0, startDate, null);
        assertThat(result.getData()).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.getData()).extracting("totalCount").first().isEqualTo(10);
        TaskStateCount taskStateCount = new TaskStateCount(ExecutionStatus.RUNNING_EXECUTION, 10);
        assertThat(result.getData()).extracting("taskCountDtos").first().asList().containsOnlyOnce(taskStateCount);

        // when general user doesn't have any task then return all count are 0
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result.getData()).extracting("totalCount").first().isEqualTo(0);
        assertThat(result.getData()).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.getData()).extracting("taskCountDtos").first().asList()
                .extracting("count").allMatch(count -> count.equals(0));

        // when general user doesn't have any task then return all count are 0
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result.getData()).extracting("totalCount").first().isEqualTo(0);
        assertThat(result.getData()).extracting("taskCountDtos").first().asList()
                .hasSameSizeAs(ExecutionStatus.values());
        assertThat(result.getData()).extracting("taskCountDtos").first().asList()
                .extracting("count").allMatch(count -> count.equals(0));

        // when instanceStateCounter return null, then return nothing
        user.setUserType(UserType.GENERAL_USER);
        Mockito.when(processService.getProjectIdListHavePerm(anyInt()))
                .thenReturn(new ArrayList<>());
        Mockito.when(taskInstanceMapper.countTaskInstanceStateByUser(any(), any(), any()))
                .thenReturn(null);
        result = dataAnalysisService.countTaskStateByProject(user, 0, null, null);
        assertThat(result.getData()).isNull();
    }

    @Test
    public void testCountProcessInstanceStateByProject() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.USER_NO_OPERATION_PROJECT_PERM));
        Result<TaskCountDto> result = dataAnalysisService.countProcessInstanceStateByProject(user, 2, startDate, endDate);
        Assert.assertNull(result.getData());

        //SUCCESS
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        Mockito.when(processInstanceMapper.countInstanceStateByUser(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(getTaskInstanceStateCounts());
        result = dataAnalysisService.countProcessInstanceStateByProject(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        Assert.assertNotNull(result.getData());
    }

    @Test
    public void testCountDefinitionByUser() {

        Result<DefineUserDto> result = dataAnalysisService.countDefinitionByUser(user, 1);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testCountCommandState() {

        String startDate = "2020-02-11 16:02:18";
        String endDate = "2020-02-11 16:03:18";
        //checkProject false
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.USER_NO_OPERATION_PROJECT_PERM));
        Result<List<CommandStateCount>> result = dataAnalysisService.countCommandState(user, 2, startDate, endDate);
        Assert.assertTrue(CollectionUtils.isEmpty(result.getData()));

        List<CommandCount> commandCounts = new ArrayList<>(1);
        CommandCount commandCount = new CommandCount();
        commandCount.setCommandType(CommandType.START_PROCESS);
        commandCounts.add(commandCount);
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        Mockito.when(commandMapper.countCommandState(0, DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(commandCounts);

        Mockito.when(errorCommandMapper.countCommandState(DateUtils.getScheduleDate(startDate),
                DateUtils.getScheduleDate(endDate), new Integer[]{1})).thenReturn(commandCounts);

        result = dataAnalysisService.countCommandState(user, 1, startDate, endDate);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

        // when project check fail then return nothing
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.USER_NO_OPERATION_PROJECT_PERM));
        result = dataAnalysisService.countCommandState(user, 2, null, null);
        Assert.assertTrue(CollectionUtils.isEmpty(result.getData()));

        // when all date in illegal format then return error message
        String startDate2 = "illegalDateString";
        String endDate2 = "illegalDateString";
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        result = dataAnalysisService.countCommandState(user, 0, startDate2, endDate2);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when one of date in illegal format then return error message
        String startDate3 = "2020-08-22 09:23:10";
        String endDate3 = "illegalDateString";
        result = dataAnalysisService.countCommandState(user, 0, startDate3, endDate3);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when one of date in illegal format then return error message
        String startDate4 = "illegalDateString";
        String endDate4 = "2020-08-22 09:23:10";
        result = dataAnalysisService.countCommandState(user, 0, startDate4, endDate4);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        // when no command found then return all count are 0
        Mockito.when(commandMapper.countCommandState(anyInt(), any(), any(), any())).thenReturn(Collections.emptyList());
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.emptyList());
        result = dataAnalysisService.countCommandState(user, 0, startDate, null);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        assertThat(result.getData()).asList().extracting("errorCount").allMatch(count -> count.equals(0));
        assertThat(result.getData()).asList().extracting("normalCount").allMatch(count -> count.equals(0));

        // when command found then return combination result
        CommandCount normalCommandCount = new CommandCount();
        normalCommandCount.setCommandType(CommandType.START_PROCESS);
        normalCommandCount.setCount(10);
        CommandCount errorCommandCount = new CommandCount();
        errorCommandCount.setCommandType(CommandType.START_PROCESS);
        errorCommandCount.setCount(5);
        Mockito.when(commandMapper.countCommandState(anyInt(), any(), any(), any())).thenReturn(Collections.singletonList(normalCommandCount));
        Mockito.when(errorCommandMapper.countCommandState(any(), any(), any())).thenReturn(Collections.singletonList(errorCommandCount));

        result = dataAnalysisService.countCommandState(user, 0, null, null);

        assertThat(result.getCode()).isEqualTo(Status.SUCCESS.getCode());
        CommandStateCount commandStateCount = new CommandStateCount();
        commandStateCount.setCommandState(CommandType.START_PROCESS);
        commandStateCount.setNormalCount(10);
        commandStateCount.setErrorCount(5);
        assertThat(result.getData()).asList().containsOnlyOnce(commandStateCount);
    }

    @Test
    public void testCountQueueState() {
        // when project check fail then return nothing
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(new Project());
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.USER_NO_OPERATION_PROJECT_PERM));
        Result<Map<String, Integer>> result = dataAnalysisService.countQueueState(user, 2);
        Assert.assertTrue(MapUtils.isEmpty(result.getData()));

        // when project check success when return all count are 0
        Mockito.when(projectMapper.selectById(Mockito.any())).thenReturn(new Project());
        Mockito.when(projectService.hasProjectAndPerm(Mockito.any(), Mockito.any())).thenReturn(new CheckParamResult(Status.SUCCESS));
        result = dataAnalysisService.countQueueState(user, 1);
        assertThat(result.getData()).extracting("taskQueue", "taskKill")
                .isNotEmpty()
                .allMatch(count -> count.equals(0));
    }

    /**
     * get list
     */
    private List<ExecuteStatusCount> getTaskInstanceStateCounts() {

        List<ExecuteStatusCount> taskInstanceStateCounts = new ArrayList<>(1);
        ExecuteStatusCount executeStatusCount = new ExecuteStatusCount();
        executeStatusCount.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        taskInstanceStateCounts.add(executeStatusCount);

        return taskInstanceStateCounts;
    }

}