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

package org.apache.dolphinscheduler.server.master;

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODE_NAMES;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.MasterExecThread;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

/**
 * test for MasterExecThread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MasterExecThread.class})
public class MasterExecThreadTest {

    private MasterExecThread masterExecThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private int processDefinitionId = 1;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        processService = mock(ProcessService.class);

        applicationContext = mock(ApplicationContext.class);
        config = new MasterConfig();
        config.setMasterExecTaskNum(1);
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);

        processInstance = mock(ProcessInstance.class);
        Mockito.when(processInstance.getProcessDefinitionId()).thenReturn(processDefinitionId);
        Mockito.when(processInstance.getState()).thenReturn(ExecutionStatus.SUCCESS);
        Mockito.when(processInstance.getHistoryCmd()).thenReturn(CommandType.COMPLEMENT_DATA.toString());
        Mockito.when(processInstance.getIsSubProcess()).thenReturn(Flag.NO);
        Mockito.when(processInstance.getScheduleTime()).thenReturn(DateUtils.stringToDate("2020-01-01 00:00:00"));
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, "2020-01-01 00:00:00");
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, "2020-01-20 23:00:00");
        Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGlobalParamMap(Collections.EMPTY_MAP);
        processDefinition.setGlobalParamList(Collections.EMPTY_LIST);
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        masterExecThread = PowerMockito.spy(new MasterExecThread(processInstance, processService, null, null, config));
        // prepareProcess init dag
        Field dag = MasterExecThread.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(masterExecThread, new DAG());
        PowerMockito.doNothing().when(masterExecThread, "executeProcess");
        PowerMockito.doNothing().when(masterExecThread, "prepareProcess");
        PowerMockito.doNothing().when(masterExecThread, "runProcess");
        PowerMockito.doNothing().when(masterExecThread, "endProcess");
    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {
        try {
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 1-30 for next save, and last day 20 no save
            verify(processService, times(20)).saveProcessInstance(processInstance);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() {
        try {
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(oneSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 9(1 to 20 step 2) for next save, and last day 31 no save
            verify(processService, times(9)).saveProcessInstance(processInstance);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testParseStartNodeName() throws ParseException {
        try {
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_START_NODE_NAMES, "t1,t2,t3");
            Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("parseStartNodeName", String.class);
            method.setAccessible(true);
            List<String> nodeNames = (List<String>) method.invoke(masterExecThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(3, nodeNames.size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRetryTaskIntervalOverTime() {
        try {
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setId(0);
            taskInstance.setMaxRetryTimes(0);
            taskInstance.setRetryInterval(0);
            taskInstance.setState(ExecutionStatus.FAILURE);
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("retryTaskIntervalOverTime", TaskInstance.class);
            method.setAccessible(true);
            Assert.assertTrue((Boolean) method.invoke(masterExecThread, taskInstance));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetMasterExecThread() {
        try {
            MasterExecThread masterExecThread1 = new MasterExecThread(processInstance, processService, null, null, config);
            Assert.assertNotNull(masterExecThread1);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRemoveCompleteList4RandomNode() {
        try {
            Map<String, TaskInstance> completeTaskList = new HashMap<>();
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setId(0);
            taskInstance.setName("test");
            taskInstance.setMaxRetryTimes(0);
            taskInstance.setRetryInterval(0);
            taskInstance.setState(ExecutionStatus.FAILURE);
            completeTaskList.put("test",taskInstance);
            Set<String> allNode = new HashSet<>();
            allNode.add("test");
            Mockito.when(processInstance.getCommandType()).thenReturn(CommandType.START_RANDOM_TASK_PROCESS);
            Mockito.when(processService.updateTaskInstance(taskInstance)).thenReturn(true);
            masterExecThread.removeCompleteList4RandomNode(completeTaskList,allNode);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testMasterExecThread() {
        try {
            TaskInstance taskInstance1 = new TaskInstance();
            taskInstance1.setId(1);
            TaskInstance taskInstance2 = new TaskInstance();
            taskInstance2.setId(2);
            TaskInstance taskInstance3 = new TaskInstance();
            taskInstance3.setId(3);
            TaskInstance taskInstance4 = new TaskInstance();
            taskInstance4.setId(4);
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_RECOVERY_START_NODE_STRING, "1,2,3,4");
            Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance1);
            Mockito.when(processService.findTaskInstanceById(2)).thenReturn(taskInstance2);
            Mockito.when(processService.findTaskInstanceById(3)).thenReturn(taskInstance3);
            Mockito.when(processService.findTaskInstanceById(4)).thenReturn(taskInstance4);
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("getStartTaskInstanceList", String.class);
            method.setAccessible(true);
            List<TaskInstance> taskInstances = (List<TaskInstance>) method.invoke(masterExecThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(4, taskInstances.size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    private List<Schedule> zeroSchedulerList() {
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList() {
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

}