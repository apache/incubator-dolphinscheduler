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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class FakeRunTaskTest {

    private ProcessService processService;

    private ProcessInstance processInstance;

    @Before
    public void before() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        MasterConfig config = new MasterConfig();
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        processInstance = getProcessInstance();
        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);

        // for ConditionsTaskExecThread.initTaskParameters
        Mockito.when(processService
                .saveTaskInstance(Mockito.any()))
                .thenReturn(true);
        // for ConditionsTaskExecThread.updateTaskState
        Mockito.when(processService
                .updateTaskInstance(Mockito.any()))
                .thenReturn(true);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(Mockito.any()))
                .thenAnswer(t -> t.getArgument(0));
    }

    @Test
    public void testConditionsTask() throws Exception {
        TaskNode taskNode = getTaskNode(TaskType.CONDITIONS);
        TaskInstance taskInstance = getTaskInstance(taskNode, processInstance);

        ConditionsTaskExecThread taskExecThread = new ConditionsTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testDependentTask() throws Exception {
        TaskNode taskNode = getTaskNode(TaskType.DEPENDENT);
        TaskInstance taskInstance = getTaskInstance(taskNode, processInstance);

        DependentTaskExecThread taskExecThread = new DependentTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testSubProcessTask() throws Exception {
        TaskNode taskNode = getTaskNode(TaskType.SUB_PROCESS);
        TaskInstance taskInstance = getTaskInstance(taskNode, processInstance);

        SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    private TaskNode getTaskNode(TaskType taskType) {
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-1000");
        taskNode.setName("whatever");
        taskNode.setType(taskType.toString());
        taskNode.setRunFlag(Constants.FLOWNODE_RUN_FLAG_FAKERUN);
        return taskNode;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1000);
        processInstance.setProcessDefinitionId(1000);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);

        return processInstance;
    }

    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        taskInstance.setTaskJson(JSONUtils.toJsonString(taskNode));
        taskInstance.setName(taskNode.getName());
        taskInstance.setTaskType(taskNode.getType());
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        return taskInstance;
    }
}
