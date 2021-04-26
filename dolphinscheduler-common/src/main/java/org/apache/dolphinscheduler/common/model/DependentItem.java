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
package org.apache.dolphinscheduler.common.model;

import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;

/**
 * dependent item
 */
public class DependentItem {

    private int definitionId;
    private String depTasks;
    private String cycle;
    private String dateValue;
    private DependResult dependResult;
    private ExecutionStatus status;


    public String getKey(){
        return String.format("%d-%s-%s-%s",
                getDefinitionId(),
                getDepTasks(),
                getCycle(),
                getDateValue());
    }

    public int getDefinitionId() {
        return definitionId;
    }

    public void setDefinitionId(int definitionId) {
        this.definitionId = definitionId;
    }

    public String getDepTasks() {
        return depTasks;
    }

    public void setDepTasks(String depTasks) {
        this.depTasks = depTasks;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getDateValue() {
        return dateValue;
    }

    public void setDateValue(String dateValue) {
        this.dateValue = dateValue;
    }

    public DependResult getDependResult() {
        return dependResult;
    }

    public void setDependResult(DependResult dependResult) {
        this.dependResult = dependResult;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DependItems{"
                + "definitionId=" + definitionId
                + ", depTasks=" + depTasks
                + ", cycle=" + cycle
                + ", dateValue=" + dateValue
                + '}';
    }
}
