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
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * scheduler service
 */
public interface SchedulerService {

    /**
     * save schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefineId process definition id
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @return create result code
     */
    Result<Schedule> insertSchedule(User loginUser, String projectName,
                                    Integer processDefineId,
                                    String schedule,
                                    WarningType warningType,
                                    int warningGroupId,
                                    FailureStrategy failureStrategy,
                                    Priority processInstancePriority,
                                    String workerGroup);

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @param scheduleExpression scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param processInstancePriority process instance priority
     * @param scheduleStatus schedule status
     * @return update result code
     */
    Result<Void> updateSchedule(User loginUser,
                                       String projectName,
                                       Integer id,
                                       String scheduleExpression,
                                       WarningType warningType,
                                       int warningGroupId,
                                       FailureStrategy failureStrategy,
                                       ReleaseState scheduleStatus,
                                       Priority processInstancePriority,
                                       String workerGroup);


    /**
     * set schedule online or offline
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @param scheduleStatus schedule status
     * @return publish result code
     */
    Result<Void> setScheduleState(User loginUser,
                                         String projectName,
                                         Integer id,
                                         ReleaseState scheduleStatus);

    /**
     * query schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefineId process definition id
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return schedule list page
     */
    Result<PageListVO<Schedule>> querySchedule(User loginUser, String projectName, Integer processDefineId, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query schedule list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return schedule list
     */
    Result<List<Schedule>> queryScheduleList(User loginUser, String projectName);

    /**
     * delete schedule
     *
     * @param projectId project id
     * @param scheduleId schedule id
     * @throws RuntimeException runtime exception
     */
    void deleteSchedule(int projectId, int scheduleId);

    /**
     * delete schedule by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param scheduleId scheule id
     * @return delete result code
     */
    Result<Void> deleteScheduleById(User loginUser, String projectName, Integer scheduleId);

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param schedule schedule expression
     * @return the next five fire time
     */
    Result<List<String>> previewSchedule(User loginUser, String projectName, String schedule);
}