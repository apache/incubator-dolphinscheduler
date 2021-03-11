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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.server.utils.AlertManager;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


/**
 *  alert manager test
 */
@Ignore
public class AlertManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertManagerTest.class);

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    ProjectMapper projectMapper;

    AlertManager alertManager;

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarningWorkerToleranceFaultTest(){
        // process instance
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(13028);

        // set process definition
        ProcessDefinition processDefinition = processDefinitionMapper.selectById(47);
        processInstance.setProcessDefinition(processDefinition);


        // fault task instance
        TaskInstance toleranceTask1 = taskInstanceMapper.selectById(5038);
        TaskInstance toleranceTask2 = taskInstanceMapper.selectById(5039);

        List<TaskInstance> toleranceTaskList = new ArrayList<>(2);
        toleranceTaskList.add(toleranceTask1);
        toleranceTaskList.add(toleranceTask2);

        alertManager.sendAlertWorkerToleranceFault(processInstance, toleranceTaskList);
    }


    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarningOfProcessInstanceTest(){
        // process instance
        ProcessInstance processInstance = processInstanceMapper.queryDetailById(13028);

        // set process definition
        ProcessDefinition processDefinition = processDefinitionMapper.selectById(47);
        processInstance.setProcessDefinition(processDefinition);

        // fault task instance
        TaskInstance toleranceTask1 = taskInstanceMapper.selectById(5038);
        toleranceTask1.setState(ExecutionStatus.FAILURE);
        TaskInstance toleranceTask2 = taskInstanceMapper.selectById(5039);
        toleranceTask2.setState(ExecutionStatus.FAILURE);

        List<TaskInstance> toleranceTaskList = new ArrayList<>(2);
        toleranceTaskList.add(toleranceTask1);
        toleranceTaskList.add(toleranceTask2);

        ProjectUser projectUser = projectMapper.queryProjectWithUserByProcessInstanceId(processInstance.getId());

        alertManager.sendAlertProcessInstance(processInstance, toleranceTaskList, projectUser);
    }

}
