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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ExecutorServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * executor service 2 test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ExecutorService2Test {

    @InjectMocks
    private ExecutorServiceImpl executorService;

    @Mock
    private ProcessService processService;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectServiceImpl projectService;

    @Mock
    private MonitorService monitorService;

    private int processDefinitionId = 1;

    private int processInstanceId = 1;

    private int tenantId = 1;

    private int userId = 1;

    private ProcessDefinition processDefinition = new ProcessDefinition();

    private ProcessInstance processInstance = new ProcessInstance();

    private User loginUser = new User();

    private String projectName = "projectName";

    private Project project = new Project();

    private String cronTime;

    @Before
    public void init() {
        // user
        loginUser.setId(userId);

        // processDefinition
        processDefinition.setId(processDefinitionId);
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinition.setTenantId(tenantId);
        processDefinition.setUserId(userId);

        // processInstance
        processInstance.setId(processInstanceId);
        processInstance.setProcessDefinitionId(processDefinitionId);
        processInstance.setState(ExecutionStatus.FAILURE);
        processInstance.setExecutorId(userId);
        processInstance.setTenantId(tenantId);

        // project
        project.setName(projectName);

        // cronRangeTime
        cronTime = "2020-01-01 00:00:00,2020-01-31 23:00:00";

        // mock
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectService.checkProjectAndAuth(loginUser, project, projectName)).thenReturn(checkProjectAndAuth());
        Mockito.when(processDefinitionMapper.selectById(processDefinitionId)).thenReturn(processDefinition);
        Mockito.when(processService.getTenantForProcess(tenantId, userId)).thenReturn(new Tenant());
        Mockito.when(processService.createCommand(any(Command.class))).thenReturn(1);
        Mockito.when(monitorService.getServerListFromZK(true)).thenReturn(getMasterServersList());
        Mockito.when(processService.findProcessInstanceDetailById(processInstanceId)).thenReturn(processInstance);
        Mockito.when(processService.findProcessDefineById(processDefinitionId)).thenReturn(processDefinition);
    }

    /**
     * not complement
     */
    @Test
    public void testNoComplement() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.START_PROCESS,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));

    }

    /**
     * not complement
     */
    @Test
    public void testComplementWithStartNodeList() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.START_PROCESS,
                null, "n1,n2",
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));

    }


    /**
     * date error
     */
    @Test
    public void testDateError() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, "2020-01-31 23:00:00,2020-01-01 00:00:00", CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.START_PROCESS_INSTANCE_ERROR, result.get(Constants.STATUS));
        verify(processService, times(0)).createCommand(any(Command.class));
    }

    /**
     * serial
     */
    @Test
    public void testSerial() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_SERIAL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(1)).createCommand(any(Command.class));

    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(31)).createCommand(any(Command.class));

    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() throws ParseException {

        Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(oneSchedulerList());
        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
        verify(processService, times(15)).createCommand(any(Command.class));

    }

    @Test
    public void testNoMsterServers() throws ParseException {
        Mockito.when(monitorService.getServerListFromZK(true)).thenReturn(new ArrayList<>());

        Map<String, Object> result = executorService.execProcessInstance(loginUser, projectName,
                processDefinitionId, cronTime, CommandType.COMPLEMENT_DATA,
                null, null,
                null, null, 0,
                RunMode.RUN_MODE_PARALLEL,
                Priority.LOW, Constants.DEFAULT_WORKER_GROUP, 110, null);
        Assert.assertEquals(result.get(Constants.STATUS), Status.MASTER_NOT_EXISTS);

    }

    @Test
    public void testExecuteRepeatRunning() throws Exception {
        Mockito.when(processService.verifyIsNeedCreateCommand(any(Command.class))).thenReturn(true);

        Map<String, Object> result = executorService.execute(loginUser, projectName, processInstanceId, ExecuteType.REPEAT_RUNNING);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    private List<Server> getMasterServersList() {
        List<Server> masterServerList = new ArrayList<>();
        Server masterServer1 = new Server();
        masterServer1.setId(1);
        masterServer1.setHost("192.168.220.188");
        masterServer1.setPort(1121);
        masterServerList.add(masterServer1);

        Server masterServer2 = new Server();
        masterServer2.setId(2);
        masterServer2.setHost("192.168.220.189");
        masterServer2.setPort(1122);
        masterServerList.add(masterServer2);

        return masterServerList;

    }

    private List zeroSchedulerList() {
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList() {
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

    private Map<String, Object> checkProjectAndAuth() {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
    }
}
