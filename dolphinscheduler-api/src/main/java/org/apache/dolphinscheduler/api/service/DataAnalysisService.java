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

import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * data analysis service
 */
public interface DataAnalysisService {

    /**
     * statistical task instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return task state count data
     */
    Result<TaskCountDto> countTaskStateByProject(User loginUser, int projectId, String startDate, String endDate);

    /**
     * statistical process instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return process instance state count data
     */
    Result<TaskCountDto> countProcessInstanceStateByProject(User loginUser, int projectId, String startDate, String endDate);

    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
     * @param projectId project id
     * @return definition count data
     */
    Result<DefineUserDto> countDefinitionByUser(User loginUser, int projectId);

    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return command state count data
     */
    Result<List<CommandStateCount>> countCommandState(User loginUser, int projectId, String startDate, String endDate);

    /**
     * count queue state
     *
     * @param loginUser login user
     * @param projectId project id
     * @return queue state count data
     */
    Result<Map<String, Integer>> countQueueState(User loginUser, int projectId);

}
