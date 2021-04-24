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

package org.apache.dolphinscheduler.api.service;

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.impl.WorkFlowLineageServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * work flow lineage service test
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkFlowLineageServiceTest {

    @InjectMocks
    private WorkFlowLineageServiceImpl workFlowLineageService;

    @Mock
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Test
    public void testQueryWorkFlowLineageByName() {
        String searchVal = "test";
        when(workFlowLineageMapper.queryByName(searchVal, 1)).thenReturn(getWorkFlowLineages());
        Result<List<WorkFlowLineage>> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal, 1);
        List<WorkFlowLineage> workFlowLineageList = result.getData();
        Assert.assertTrue(workFlowLineageList.size() > 0);
    }

    @Test
    public void testQueryWorkFlowLineageByIds() {

        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        ids.add(2);

        when(workFlowLineageMapper.queryByIds(ids, 1)).thenReturn(getWorkFlowLineages());
        when(workFlowLineageMapper.querySourceTarget(1)).thenReturn(getWorkFlowRelation());
        Result<Map<String, Object>> result = workFlowLineageService.queryWorkFlowLineageByIds(ids, 1);
        Map<String, Object> workFlowLists = result.getData();
        List<WorkFlowLineage> workFlowLineages = (List<WorkFlowLineage>) workFlowLists.get("workFlowList");
        List<WorkFlowRelation> workFlowRelations = (List<WorkFlowRelation>) workFlowLists.get("workFlowRelationList");
        Assert.assertTrue(workFlowLineages.size() > 0);
        Assert.assertTrue(workFlowRelations.size() > 0);
    }

    private List<WorkFlowLineage> getWorkFlowLineages() {
        List<WorkFlowLineage> workFlowLineages = new ArrayList<>();
        WorkFlowLineage workFlowLineage = new WorkFlowLineage();
        workFlowLineage.setWorkFlowId(1);
        workFlowLineage.setWorkFlowName("testdag");
        workFlowLineages.add(workFlowLineage);
        return workFlowLineages;
    }

    private List<WorkFlowRelation> getWorkFlowRelation(){
        List<WorkFlowRelation> workFlowRelations = new ArrayList<>();
        WorkFlowRelation workFlowRelation = new WorkFlowRelation();
        workFlowRelation.setSourceWorkFlowId(1);
        workFlowRelation.setTargetWorkFlowId(2);
        workFlowRelations.add(workFlowRelation);
        return workFlowRelations;
    }

}
