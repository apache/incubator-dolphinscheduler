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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

/**
 * test for dependent task execute thread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DependentTaskExecThread.class, SpringApplicationContext.class, ProcessService.class})
public class DependentTaskExecThreadTest {

    private ProcessInstance processInstance;

    private ProcessService processService;

    private MasterConfig masterConfig;

    @Before
    public void init() throws Exception {
        masterConfig = new MasterConfig();
        masterConfig.setMasterTaskCommitRetryTimes(1);
        masterConfig.setMasterTaskCommitInterval(100);
        processInstance = PowerMockito.mock(ProcessInstance.class);
        processService = PowerMockito.mock(ProcessService.class);

        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(ProcessService.class))
                .thenReturn(processService);
        PowerMockito.when(SpringApplicationContext.getBean(MasterConfig.class))
                .thenReturn(masterConfig);

        PowerMockito.when(processInstance.getState()).thenReturn(ExecutionStatus.FAILURE);

        PowerMockito.when(processService.updateTaskInstance(Mockito.any(TaskInstance.class)))
                .thenReturn(true);
        PowerMockito.when(processService.saveTaskInstance(Mockito.any(TaskInstance.class)))
                .thenReturn(true);
        PowerMockito.when(processService.findProcessInstanceById(Mockito.anyInt()))
                .thenReturn(processInstance);

        JoranConfigurator configurator = new JoranConfigurator();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        configurator.setContext(lc);
        lc.reset();
        configurator.doConfigure("src/main/resources/logback-master.xml");
    }

    @Test
    public void testWaitDependentProcessesStartTimeout_1() throws Exception {
        TaskInstance taskInstance = getTaskInstance();
        DependentTaskExecThread execThread = PowerMockito.spy(new DependentTaskExecThread(taskInstance));
        PowerMockito.when(execThread, "allDependentTaskFinish")
                .thenReturn(true);
        PowerMockito.when(processService.findTaskInstanceById(Mockito.anyInt()))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.submitTask(Mockito.any(TaskInstance.class)))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.findLastRunningProcess(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class)))
                .thenReturn(processInstance);

        // Wait dependent process successful.
        execThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, execThread.getTaskInstance().getState());
    }

    @Test
    public void testWaitDependentProcessesStartTimeout_2() throws Exception {
        TaskInstance taskInstance = getTaskInstance();
        DependentTaskExecThread execThread = PowerMockito.spy(new DependentTaskExecThread(taskInstance));
        PowerMockito.when(execThread, "allDependentTaskFinish")
                .thenReturn(true);
        PowerMockito.when(processService.findTaskInstanceById(Mockito.anyInt()))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.submitTask(Mockito.any(TaskInstance.class)))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.findLastRunningProcess(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class)))
                .thenReturn(null).thenReturn(processInstance);

        // Note that the interval here is a negative number, and the task will directly time out.
        taskInstance.setTaskJson(taskInstance.getTaskJson().replaceAll("\"interval\":3", "\"interval\":-1"));
        execThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, execThread.taskInstance.getState());
    }

    @Test
    public void testWaitDependentProcessesStartTimeout_3() throws Exception {
        TaskInstance taskInstance = getTaskInstance();
        DependentTaskExecThread execThread = PowerMockito.spy(new DependentTaskExecThread(taskInstance));
        PowerMockito.when(execThread, "allDependentTaskFinish")
                .thenReturn(true);
        PowerMockito.when(processService.findTaskInstanceById(Mockito.anyInt()))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.submitTask(Mockito.any(TaskInstance.class)))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.findLastRunningProcess(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class)))
                .thenReturn(processInstance);

        // Don't check timeout.
        taskInstance.setTaskJson(taskInstance.getTaskJson().replaceAll("true", "false"));
        execThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, execThread.taskInstance.getState());
    }

    @Test
    public void testWaitDependentProcessesStartTimeout_4() throws Exception {
        TaskInstance taskInstance = getTaskInstance();
        DependentTaskExecThread execThread = PowerMockito.spy(new DependentTaskExecThread(taskInstance));
        PowerMockito.when(execThread, "allDependentTaskFinish")
                .thenReturn(false).thenReturn(true);
        PowerMockito.when(processService.findTaskInstanceById(Mockito.anyInt()))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.submitTask(Mockito.any(TaskInstance.class)))
                .thenReturn(taskInstance);
        PowerMockito.when(processService.findLastRunningProcess(Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class)))
                .thenReturn(processInstance);

        execThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, execThread.getTaskInstance().getState());
    }

    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("DEPENDENT");
        taskInstance.setId(252612);
        taskInstance.setName("ABC");
        taskInstance.setProcessInstanceId(10111);
        taskInstance.setTaskJson("{\"type\":\"DEPENDENT\",\"id\":\"tasks-4455\",\"name\":\"d_node\",\"params\":{},"
                + "\"description\":\"d_node\",\"runFlag\":\"NORMAL\",\"conditionResult\":{"
                + "\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{"
                + "\"relation\":\"AND\",\"dependItemList\":[{\"projectId\":1,\"definitionId\":15,\"depTasks\":\"py_1\",\"cycle\":\"day\",\"dateValue\":\"today\"}]}"
                + "]},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"FAILED\",\"interval\":30,\"enable\":true},"
                + "\"waitStartTimeout\":{\"strategy\":\"\",\"interval\":5,\"enable\":true,\"checkInterval\":1},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"preTasks\":[]}");
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }
}