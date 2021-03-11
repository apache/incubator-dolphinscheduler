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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.Silent.class)
@PrepareForTest(MasterTaskExecThread.class)
public class MasterTaskExecThreadTest {

    private MasterTaskExecThread masterTaskExecThread;

    private SpringApplicationContext springApplicationContext;

    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    @Before
    public void setUp() {

        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
        this.springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        this.zookeeperRegistryCenter = PowerMockito.mock(ZookeeperRegistryCenter.class);
        PowerMockito.when(SpringApplicationContext.getBean(ZookeeperRegistryCenter.class))
                .thenReturn(this.zookeeperRegistryCenter);
        this.masterTaskExecThread = new MasterTaskExecThread(getTaskInstance());
    }

    @Test
    public void testExistsValidWorkerGroup1(){

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(Sets.newHashSet());
        boolean b = masterTaskExecThread.existsValidWorkerGroup("default");
        Assert.assertFalse(b);
    }
    @Test
    public void testExistsValidWorkerGroup2(){
        Set<String> workerGroups = new HashSet<>();
        workerGroups.add("test1");
        workerGroups.add("test2");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGroups);
        boolean b = masterTaskExecThread.existsValidWorkerGroup("default");
        Assert.assertFalse(b);
    }

    @Test
    public void testExistsValidWorkerGroup3(){
        Set<String> workerGroups = new HashSet<>();
        workerGroups.add("test1");

        Mockito.when(zookeeperRegistryCenter.getWorkerGroupDirectly()).thenReturn(workerGroups);
        Mockito.when(zookeeperRegistryCenter.getWorkerGroupNodesDirectly("test1")).thenReturn(workerGroups);
        boolean b = masterTaskExecThread.existsValidWorkerGroup("test1");
        Assert.assertTrue(b);
    }

    @Test
    public void testPauseTask(){


        ProcessService processService = Mockito.mock(ProcessService.class);
        Mockito.when(this.springApplicationContext.getBean(ProcessService.class))
                .thenReturn(processService);

        TaskInstance taskInstance = getTaskInstance();
        Mockito.when(processService.findTaskInstanceById(252612))
                .thenReturn(taskInstance);

        Mockito.when(processService.updateTaskInstance(taskInstance))
                .thenReturn(true);

        MasterTaskExecThread masterTaskExecThread = new MasterTaskExecThread(taskInstance);
        masterTaskExecThread.pauseTask();
        org.junit.Assert.assertEquals(ExecutionStatus.PAUSE, taskInstance.getState());
    }

    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("SHELL");
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setTaskJson("{}");
        taskInstance.setProcessInstanceId(10111);
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }

}
