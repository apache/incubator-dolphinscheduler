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

package org.apache.dolphinscheduler.common.task.conditions;

import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

public class SwhichParameters extends AbstractParameters {

    private DependentRelation dependRelation;
    private String relation;
    private List<String> nextNode;

    @Override
    public boolean checkParameters() {
        return true;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    private int resultConditionLocation;
    private List<SwhichResultVo> dependTaskList;

    public DependentRelation getDependRelation() {
        return dependRelation;
    }

    public void setDependRelation(DependentRelation dependRelation) {
        this.dependRelation = dependRelation;
    }

    public int getResultConditionLocation() {
        return resultConditionLocation;
    }

    public void setResultConditionLocation(int resultConditionLocation) {
        this.resultConditionLocation = resultConditionLocation;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public List<SwhichResultVo> getDependTaskList() {
        return dependTaskList;
    }

    public void setDependTaskList(List<SwhichResultVo> dependTaskList) {
        this.dependTaskList = dependTaskList;
    }

    public List<String> getNextNode() {
        return nextNode;
    }

    public void setNextNode(List<String> nextNode) {
        this.nextNode = nextNode;
    }
}
