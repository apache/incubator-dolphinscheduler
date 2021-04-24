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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * task instance service
 */
public interface TaskInstanceService {

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    Result<PageListVO<Map<String, Object>>> queryTaskListPaging(User loginUser, String projectName,
                                                         Integer processInstanceId, String processInstanceName, String taskName, String executorName, String startDate,
                                                         String endDate, String searchVal, ExecutionStatus stateType, String host,
                                                         Integer pageNo, Integer pageSize);

    /**
     * change one task instance's state from failure to forced success
     *
     * @param loginUser      login user
     * @param projectName    project name
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    Result<Void> forceTaskSuccess(User loginUser, String projectName, Integer taskInstanceId);

}
