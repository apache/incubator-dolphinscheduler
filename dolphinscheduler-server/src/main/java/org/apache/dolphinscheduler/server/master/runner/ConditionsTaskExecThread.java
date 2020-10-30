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
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

public class ConditionsTaskExecThread extends MasterBaseTaskExecThread {


    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     * complete task map
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    /**
     * condition result
     */
    private DependResult conditionResult;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance    task instance
     */
    public ConditionsTaskExecThread(TaskInstance taskInstance) {
        super(taskInstance);
        taskInstance.setStartTime(new Date());
    }

    @Override
    public Boolean submitWaitComplete() {
        try{
            this.taskInstance = submit();
            logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskInstance.getProcessDefinitionId(),
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getId()));
            String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
            Thread.currentThread().setName(threadLoggerInfoName);
            initTaskParameters();
            logger.info("dependent task start");
            waitTaskQuit();
            updateTaskState();
        }catch (Exception e){
            logger.error("conditions task run exception" , e);
        }
        return true;
    }

    private void waitTaskQuit() {
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(
                taskInstance.getProcessInstanceId()
        );
        for(TaskInstance task : taskInstances){
            completeTaskList.putIfAbsent(task.getName(), task.getState());
        }

        List<DependResult> modelResultList = new ArrayList<>();
        for(DependentTaskModel dependentTaskModel : dependentParameters.getDependTaskList()){

            List<DependResult> itemDependResult = new ArrayList<>();
            for(DependentItem item : dependentTaskModel.getDependItemList()){
                itemDependResult.add(getDependResultForItem(item));
            }
            DependResult modelResult = DependentUtils.getDependResultForRelation(dependentTaskModel.getRelation(), itemDependResult);
            modelResultList.add(modelResult);
        }
        conditionResult = DependentUtils.getDependResultForRelation(
                dependentParameters.getRelation(), modelResultList
        );
        logger.info("the conditions task depend result : {}", conditionResult);
    }

    /**
     *
     */
    private void updateTaskState() {
        ExecutionStatus status;
        if(this.cancel){
            status = ExecutionStatus.KILL;
        }else{
            status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        }
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    private void initTaskParameters() {
        this.taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance));
        this.taskInstance.setHost(NetUtils.getHost() + Constants.COLON + masterConfig.getListenPort());
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        this.processService.saveTaskInstance(taskInstance);

        this.dependentParameters = JSONUtils.parseObject(this.taskInstance.getDependency(), DependentParameters.class);
    }


    /**
     * depend result for depend item
     * @param item
     * @return
     */
    private DependResult getDependResultForItem(DependentItem item){

        DependResult dependResult = DependResult.SUCCESS;
        if(!completeTaskList.containsKey(item.getDepTasks())){
            logger.info("depend item: {} have not completed yet.", item.getDepTasks());
            dependResult = DependResult.FAILED;
            return dependResult;
        }
        ExecutionStatus executionStatus = completeTaskList.get(item.getDepTasks());
        if(executionStatus != item.getStatus()){
            logger.info("depend item : {} expect status: {}, actual status: {}" ,item.getDepTasks(), item.getStatus(), executionStatus);
            dependResult = DependResult.FAILED;
        }
        logger.info("dependent item complete {} {},{}",
                Constants.DEPENDENT_SPLIT, item.getDepTasks(), dependResult);
        return dependResult;
    }


}
