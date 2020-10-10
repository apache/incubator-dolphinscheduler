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

import static org.apache.dolphinscheduler.common.Constants.DEPENDENT_SPLIT;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.DependentUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.utils.DependentExecute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DependentTaskExecThread extends MasterBaseTaskExecThread {

    private DependentParameters dependentParameters;

    /**
     * dependent task list
     */
    private List<DependentExecute> dependentTaskList = new ArrayList<>();

    /**
     * depend item result map
     * save the result to log file
     */
    private Map<String, DependResult> dependResultMap = new HashMap<>();


    /**
     * dependent date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date dependentDate;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance    task instance
     */
    public DependentTaskExecThread(TaskInstance taskInstance) {
        super(taskInstance);
        taskInstance.setStartTime(new Date());
    }


    @Override
    public Boolean submitWaitComplete() {
        try{
            logger.info("dependent task start");
            this.taskInstance = submit();
            logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskInstance.getProcessDefinitionId(),
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getId()));
            String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
            Thread.currentThread().setName(threadLoggerInfoName);
            initTaskParameters();
            if (!fakeRun) {
                initDependParameters();
            }
            waitTaskQuit();
            updateTaskState();
        }catch (Exception e){
            logger.error("dependent task run exception" , e);
        }
        return true;
    }

    /**
     * init dependent parameters
     */
    private void initDependParameters() {

        this.dependentParameters = JSONUtils.parseObject(this.taskInstance.getDependency(),
                DependentParameters.class);

        for(DependentTaskModel taskModel : dependentParameters.getDependTaskList()){
            this.dependentTaskList.add(new DependentExecute(
                    taskModel.getDependItemList(), taskModel.getRelation()));
        }
        if(this.processInstance.getScheduleTime() != null){
            this.dependentDate = this.processInstance.getScheduleTime();
        }else{
            this.dependentDate = new Date();
        }
    }

    private void updateTaskState() {
        ExecutionStatus status;
        if (cancel) {
            status = ExecutionStatus.KILL;
        } else if (fakeRun) {
            status = ExecutionStatus.SUCCESS;
        } else {
            DependResult result = getTaskDependResult();
            status = (result == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        }
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
    }

    /**
     * wait dependent tasks quit
     */
    private Boolean waitTaskQuit() {
        logger.info("wait depend task : {} complete", this.taskInstance.getName());

        if (fakeRun) {
            logger.info("dependent task :{} id:{}, as fake-run",
                    taskInstance.getName(),
                    taskInstance.getId());
            return true;
        }

        if (taskInstance.getState().typeIsFinished()) {
            logger.info("task {} already complete. task state:{}",
                    this.taskInstance.getName(),
                    this.taskInstance.getState());
            return true;
        }
        while (Stopper.isRunning()) {
            try {
                if (this.processInstance == null) {
                    logger.error("process instance not exists , master task exec thread exit");
                    return true;
                }
                if (this.cancel || this.processInstance.getState() == ExecutionStatus.READY_STOP) {
                    cancelTaskInstance();
                    break;
                }

                if (allDependentTaskFinish() || taskInstance.getState().typeIsFinished()) {
                    break;
                }
                // update process task
                taskInstance = processService.findTaskInstanceById(taskInstance.getId());
                processInstance = processService.findProcessInstanceById(processInstance.getId());
                Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            } catch (Exception e) {
                logger.error("exception", e);
                if (processInstance != null) {
                    logger.error("wait task quit failed, instance id:{}, task id:{}",
                            processInstance.getId(), taskInstance.getId());
                }
            }
        }
        return true;
    }

    /**
     * cancel dependent task
     */
    private void cancelTaskInstance() {
        this.cancel = true;
    }

    private void initTaskParameters() {
        taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance));
        taskInstance.setHost(NetUtils.getHost() + Constants.COLON + masterConfig.getListenPort());
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    /**
     * judge all dependent tasks finish
     * @return whether all dependent tasks finish
     */
    private boolean allDependentTaskFinish(){
        boolean finish = true;
        for(DependentExecute dependentExecute : dependentTaskList){
            for(Map.Entry<String, DependResult> entry: dependentExecute.getDependResultMap().entrySet()) {
                if(!dependResultMap.containsKey(entry.getKey())){
                    dependResultMap.put(entry.getKey(), entry.getValue());
                    //save depend result to log
                    logger.info("dependent item complete {} {},{}",
                            DEPENDENT_SPLIT, entry.getKey(), entry.getValue());
                }
            }
            if(!dependentExecute.finish(dependentDate)){
                finish = false;
            }
        }
        return finish;
    }

    /**
     * get dependent result
     * @return DependResult
     */
    private DependResult getTaskDependResult(){
        List<DependResult> dependResultList = new ArrayList<>();
        for(DependentExecute dependentExecute : dependentTaskList){
            DependResult dependResult = dependentExecute.getModelDependResult(dependentDate);
            dependResultList.add(dependResult);
        }
        DependResult result = DependentUtils.getDependResultForRelation(
                this.dependentParameters.getRelation(), dependResultList
        );
        logger.info("dependent task completed, dependent result:{}", result);
        return result;
    }
}