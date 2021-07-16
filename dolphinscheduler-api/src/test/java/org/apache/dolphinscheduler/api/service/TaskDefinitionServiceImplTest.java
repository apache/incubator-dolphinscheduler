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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TaskDefinitionServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.MessageFormat;
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

@RunWith(MockitoJUnitRunner.class)
public class TaskDefinitionServiceImplTest {

    String taskDefinitionJson = "[{\n"
            +   "\"name\": \"test12111\",\n"
            +   "\"description\": \"test\",\n"
            +   "\"taskType\": \"SHELL\",\n"
            +   "\"flag\": 0,\n"
            +   "\"taskParams\": \n"
            +   "\"{\\\"resourceList\\\":[],\n"
            +   "\\\"localParams\\\":[],\n"
            +   "\\\"rawScript\\\":\\\"echo 11\\\",\n"
            +   "\\\"conditionResult\\\":\n"
            +   "{\\\"successNode\\\":[\\\"\\\"],\n"
            +   "\\\"failedNode\\\":[\\\"\\\"]},\n"
            +   "\\\"dependence\\\":{}}\",\n"
            +   "\"taskPriority\": 0,\n"
            +   "\"workerGroup\": \"default\",\n"
            +   "\"failRetryTimes\": 0,\n"
            +   "\"failRetryInterval\": 1,\n"
            +   "\"timeoutFlag\": 1, \n"
            +   "\"timeoutNotifyStrategy\": 0,\n"
            +   "\"timeout\": 0, \n"
            +   "\"delayTime\": 0,\n"
            +   "\"resourceIds\":\"\" \n"
            +   "}] ";

    @InjectMocks
    private TaskDefinitionServiceImpl taskDefinitionService;

    @Mock
    private TaskDefinitionMapper taskDefinitionMapper;

    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Mock
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private ProcessService processService;

    @Test
    public void createTaskDefinition() {
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);


        List<TaskDefinition> taskDefinitions = JSONUtils.toList(taskDefinitionJson, TaskDefinition.class);
        Mockito.when(taskDefinitionMapper.batchInsert(Mockito.anyList())).thenReturn(1);
        Mockito.when(taskDefinitionLogMapper.batchInsert(Mockito.anyList())).thenReturn(1);
        Map<String, Object> relation = taskDefinitionService
                .createTaskDefinition(loginUser, projectCode, taskDefinitionJson);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

    }

    @Test
    public void queryTaskDefinitionByName() {
        String taskName = "task";
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionMapper.queryByDefinitionName(project.getCode(), taskName))
                .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
                .queryTaskDefinitionByName(loginUser, projectCode, taskName);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    @Test
    public void deleteTaskDefinitionByCode() {
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionMapper.deleteByCode(Mockito.anyLong()))
                .thenReturn(1);

        Map<String, Object> relation = taskDefinitionService
                .deleteTaskDefinitionByCode(loginUser, projectCode, 11L);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));

    }

    /*
    @Test
    public void updateTaskDefinition() {
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(processService.updateTaskDefinition(Mockito.eq(loginUser)
                , Mockito.eq(project.getCode())
                , Mockito.eq(taskNode)
                , Mockito.any(TaskDefinition.class)))
                .thenReturn(1);

        Mockito.when(taskDefinitionMapper.queryByDefinitionCode(11L))
                .thenReturn(new TaskDefinition());

        Map<String, Object> relation = taskDefinitionService
                .updateTaskDefinition(loginUser, projectCode, 11L, taskDefinitionJson);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }*/

    @Test
    public void switchVersion() {
        int version = 1;
        long taskCode = 11L;
        long projectCode = 1L;

        Project project = getProject(projectCode);
        Mockito.when(projectMapper.queryByCode(projectCode)).thenReturn(project);

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS, projectCode);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, project.getName())).thenReturn(result);

        TaskNode taskNode = JSONUtils.parseObject(taskDefinitionJson, TaskNode.class);

        Mockito.when(taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, version))
                .thenReturn(new TaskDefinitionLog());

        Mockito.when(taskDefinitionMapper.queryByDefinitionCode(taskCode))
                .thenReturn(new TaskDefinition());
        
        Map<String, Object> relation = taskDefinitionService
                .switchVersion(loginUser, projectCode, taskCode, version);

        Assert.assertEquals(Status.SUCCESS, relation.get(Constants.STATUS));
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * get mock Project
     *
     * @param projectCode projectCode
     * @return Project
     */
    private Project getProject(long projectCode) {
        Project project = new Project();
        project.setId(1);
        project.setCode(projectCode);
        project.setName("test");
        project.setUserId(1);
        return project;
    }

}
