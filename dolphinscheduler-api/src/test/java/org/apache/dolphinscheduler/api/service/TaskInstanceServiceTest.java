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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskInstanceServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class TaskInstanceServiceTest {

    @InjectMocks
    private TaskInstanceServiceImpl taskInstanceService;

    @Mock
    ProjectMapper projectMapper;

    @Mock
    ProjectServiceImpl projectService;

    @Mock
    ProcessService processService;

    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Mock
    UsersService usersService;

    @Test
    public void queryTaskListPaging() {
        String projectName = "project_test1";
        User loginUser = getAdminUser();
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.PROJECT_NOT_FOUNT, projectName);

        //project auth fail
        when(projectMapper.queryByName(projectName)).thenReturn(null);
        when(projectService.checkProjectAndAuth(loginUser, null, projectName)).thenReturn(result);
        Map<String, Object> projectAuthFailRes = taskInstanceService.queryTaskListPaging(loginUser, "project_test1", 0, "", "",
                "test_user", "2019-02-26 19:48:00", "2019-02-26 19:48:22", "", null, "", 1, 20);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, projectAuthFailRes.get(Constants.STATUS));

        // data parameter check
        putMsg(result, Status.SUCCESS, projectName);
        Project project = getProject(projectName);
        when(projectMapper.queryByName(Mockito.anyString())).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        Map<String, Object> dataParameterRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "test_user", "20200101 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, dataParameterRes.get(Constants.STATUS));

        //project
        putMsg(result, Status.SUCCESS, projectName);
        Date start = DateUtils.getScheduleDate("2020-01-01 00:00:00");
        Date end = DateUtils.getScheduleDate("2020-01-02 00:00:00");
        ProcessInstance processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        Page<TaskInstance> pageReturn = new Page<>(1, 10);
        taskInstanceList.add(taskInstance);
        pageReturn.setRecords(taskInstanceList);
        when(projectMapper.queryByName(Mockito.anyString())).thenReturn(project);
        when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(result);
        when(usersService.queryUser(loginUser.getId())).thenReturn(loginUser);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(loginUser.getId());
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        when(usersService.queryUser(processInstance.getExecutorId())).thenReturn(loginUser);
        when(processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);

        Map<String, Object> successRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));

        //executor name empty
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), eq(start), eq(end))).thenReturn(pageReturn);
        Map<String, Object> executorEmptyRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, executorEmptyRes.get(Constants.STATUS));

        //executor null
        when(usersService.queryUser(loginUser.getId())).thenReturn(null);
        when(usersService.getUserIdByName(loginUser.getUserName())).thenReturn(-1);
        Map<String, Object> executorNullRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "test_user", "2020-01-01 00:00:00", "2020-01-02 00:00:00", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, executorNullRes.get(Constants.STATUS));

        //start/end date null
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), any(), any())).thenReturn(pageReturn);
        Map<String, Object> executorNullDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "", null, null, "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.SUCCESS, executorNullDateRes.get(Constants.STATUS));

        //start date error format
        when(taskInstanceMapper.queryTaskInstanceListPaging(Mockito.any(Page.class), eq(project.getId()), eq(1), eq(""), eq(""), eq(""),
                eq(0), Mockito.any(), eq("192.168.xx.xx"), any(), any())).thenReturn(pageReturn);
        Map<String, Object> executorErrorStartDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "", "error date", null, "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, executorErrorStartDateRes.get(Constants.STATUS));
        Map<String, Object> executorErrorEndDateRes = taskInstanceService.queryTaskListPaging(loginUser, projectName, 1, "", "",
                "", null, "error date", "", ExecutionStatus.SUCCESS, "192.168.xx.xx", 1, 20);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR, executorErrorEndDateRes.get(Constants.STATUS));
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
        processInstance.setExecutorId(-1);
        return processInstance;
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

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    @Test
    public void forceTaskSuccess() {
        User user = getAdminUser();
        String projectName = "test";
        Project project = getProject(projectName);
        int taskId = 1;
        TaskInstance task = getTaskInstance();

        Map<String, Object> mockSuccess = new HashMap<>(5);
        putMsg(mockSuccess, Status.SUCCESS);
        when(projectMapper.queryByName(projectName)).thenReturn(project);

        // user auth failed
        Map<String, Object> mockFailure = new HashMap<>(5);
        putMsg(mockFailure, Status.USER_NO_OPERATION_PROJECT_PERM, user.getUserName(), projectName);
        when(projectService.checkProjectAndAuth(user, project, projectName)).thenReturn(mockFailure);
        Map<String, Object> authFailRes = taskInstanceService.forceTaskSuccess(user, projectName, taskId);
        Assert.assertNotSame(Status.SUCCESS, authFailRes.get(Constants.STATUS));

        // test task not found
        when(projectService.checkProjectAndAuth(user, project, projectName)).thenReturn(mockSuccess);
        when(taskInstanceMapper.selectById(Mockito.anyInt())).thenReturn(null);
        Map<String, Object> taskNotFoundRes = taskInstanceService.forceTaskSuccess(user, projectName, taskId);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_FOUND, taskNotFoundRes.get(Constants.STATUS));

        // test task instance state error
        task.setState(ExecutionStatus.SUCCESS);
        when(taskInstanceMapper.selectById(1)).thenReturn(task);
        Map<String, Object> taskStateErrorRes = taskInstanceService.forceTaskSuccess(user, projectName, taskId);
        Assert.assertEquals(Status.TASK_INSTANCE_STATE_OPERATION_ERROR, taskStateErrorRes.get(Constants.STATUS));

        // test error
        task.setState(ExecutionStatus.FAILURE);
        when(taskInstanceMapper.updateById(task)).thenReturn(0);
        Map<String, Object> errorRes = taskInstanceService.forceTaskSuccess(user, projectName, taskId);
        Assert.assertEquals(Status.FORCE_TASK_SUCCESS_ERROR, errorRes.get(Constants.STATUS));

        // test success
        task.setState(ExecutionStatus.FAILURE);
        when(taskInstanceMapper.updateById(task)).thenReturn(1);
        Map<String, Object> successRes = taskInstanceService.forceTaskSuccess(user, projectName, taskId);
        Assert.assertEquals(Status.SUCCESS, successRes.get(Constants.STATUS));
    }
}
