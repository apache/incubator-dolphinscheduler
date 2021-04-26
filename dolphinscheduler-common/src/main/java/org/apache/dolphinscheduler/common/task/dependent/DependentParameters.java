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
package org.apache.dolphinscheduler.common.task.dependent;

import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;

import java.util.ArrayList;
import java.util.List;

public class DependentParameters extends AbstractParameters {

    private List<DependentTaskModel> dependTaskList;
    private DependentRelation relation;



    @Override
    public boolean checkParameters() {
        return true;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    public List<DependentTaskModel> getDependTaskList() {
        return dependTaskList;
    }

    public void setDependTaskList(List<DependentTaskModel> dependTaskList) {
        this.dependTaskList = dependTaskList;
    }

    public DependentRelation getRelation() {
        return relation;
    }

    public void setRelation(DependentRelation relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "DependParams{"
                + "relation=" + relation
                + ", dependTaskList=" + dependTaskList
                + '}';
    }
}
