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
package org.apache.dolphinscheduler.api.dto;

/**
 * ProcessMeta
 */
public class ProcessMeta {

    /**
     * project name
     */
    private String projectName;

    /**
     * process definition name
     */
    private String processDefinitionName;

    /**
     * process definition json
     */
    private String processDefinitionJson;

    /**
     * process definition desc
     */
    private String processDefinitionDescription;

    /**
     * process definition locations
     */
    private String processDefinitionLocations;

    /**
     * process definition connects
     */
    private String processDefinitionConnects;

    /**
     * warning type
     */
    private String scheduleWarningType;

    /**
     * warning group id
     */
    private Integer scheduleWarningGroupId;

    /**
     * warning group name
     */
    private String scheduleWarningGroupName;

    /**
     * start time
     */
    private String scheduleStartTime;

    /**
     * end time
     */
    private String scheduleEndTime;

    /**
     * crontab
     */
    private String scheduleCrontab;

    /**
     * failure strategy
     */
    private String scheduleFailureStrategy;

    /**
     * release state
     */
    private String scheduleReleaseState;

    /**
     * process instance priority
     */
    private String scheduleProcessInstancePriority;

    /**
     * worker group name
     */
    private String scheduleWorkerGroupName;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProcessDefinitionName() {
        return processDefinitionName;
    }

    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public String getProcessDefinitionJson() {
        return processDefinitionJson;
    }

    public void setProcessDefinitionJson(String processDefinitionJson) {
        this.processDefinitionJson = processDefinitionJson;
    }

    public String getProcessDefinitionDescription() {
        return processDefinitionDescription;
    }

    public void setProcessDefinitionDescription(String processDefinitionDescription) {
        this.processDefinitionDescription = processDefinitionDescription;
    }

    public String getProcessDefinitionLocations() {
        return processDefinitionLocations;
    }

    public void setProcessDefinitionLocations(String processDefinitionLocations) {
        this.processDefinitionLocations = processDefinitionLocations;
    }

    public String getProcessDefinitionConnects() {
        return processDefinitionConnects;
    }

    public void setProcessDefinitionConnects(String processDefinitionConnects) {
        this.processDefinitionConnects = processDefinitionConnects;
    }

    public String getScheduleWarningType() {
        return scheduleWarningType;
    }

    public void setScheduleWarningType(String scheduleWarningType) {
        this.scheduleWarningType = scheduleWarningType;
    }

    public Integer getScheduleWarningGroupId() {
        return scheduleWarningGroupId;
    }

    public void setScheduleWarningGroupId(int scheduleWarningGroupId) {
        this.scheduleWarningGroupId = scheduleWarningGroupId;
    }

    public String getScheduleWarningGroupName() {
        return scheduleWarningGroupName;
    }

    public void setScheduleWarningGroupName(String scheduleWarningGroupName) {
        this.scheduleWarningGroupName = scheduleWarningGroupName;
    }

    public String getScheduleStartTime() {
        return scheduleStartTime;
    }

    public void setScheduleStartTime(String scheduleStartTime) {
        this.scheduleStartTime = scheduleStartTime;
    }

    public String getScheduleEndTime() {
        return scheduleEndTime;
    }

    public void setScheduleEndTime(String scheduleEndTime) {
        this.scheduleEndTime = scheduleEndTime;
    }

    public String getScheduleCrontab() {
        return scheduleCrontab;
    }

    public void setScheduleCrontab(String scheduleCrontab) {
        this.scheduleCrontab = scheduleCrontab;
    }

    public String getScheduleFailureStrategy() {
        return scheduleFailureStrategy;
    }

    public void setScheduleFailureStrategy(String scheduleFailureStrategy) {
        this.scheduleFailureStrategy = scheduleFailureStrategy;
    }

    public String getScheduleReleaseState() {
        return scheduleReleaseState;
    }

    public void setScheduleReleaseState(String scheduleReleaseState) {
        this.scheduleReleaseState = scheduleReleaseState;
    }

    public String getScheduleProcessInstancePriority() {
        return scheduleProcessInstancePriority;
    }

    public void setScheduleProcessInstancePriority(String scheduleProcessInstancePriority) {
        this.scheduleProcessInstancePriority = scheduleProcessInstancePriority;
    }

    public String getScheduleWorkerGroupName() {
        return scheduleWorkerGroupName;
    }

    public void setScheduleWorkerGroupName(String scheduleWorkerGroupName) {
        this.scheduleWorkerGroupName = scheduleWorkerGroupName;
    }
}
