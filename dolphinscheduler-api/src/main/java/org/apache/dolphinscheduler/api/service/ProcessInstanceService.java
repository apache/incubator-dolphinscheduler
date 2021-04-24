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

import org.apache.dolphinscheduler.api.dto.gantt.GanttDto;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * process instance service
 */
public interface ProcessInstanceService {

    /**
     * return top n SUCCESS process instance order by running time which started between startTime and endTime
     */
    Result<List<ProcessInstance>> queryTopNLongestRunningProcessInstance(User loginUser, String projectName, int size, String startTime, String endTime);

    /**
     * query process instance by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process instance id
     * @return process instance detail
     */
    Result<ProcessInstance> queryProcessInstanceById(User loginUser, String projectName, Integer processId);

    /**
     * paging query process instance list, filtering according to project, process definition, time range, keyword, process status
     *
     * @param loginUser login user
     * @param projectName project name
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefineId process definition id
     * @param searchVal search value
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @return process instance list
     */
    Result<PageListVO<ProcessInstance>> queryProcessInstanceList(User loginUser, String projectName, Integer processDefineId,
                                                                 String startDate, String endDate,
                                                                 String searchVal, String executorName, ExecutionStatus stateType, String host,
                                                                 Integer pageNo, Integer pageSize);

    /**
     * query task list by process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process instance id
     * @return task list for the process instance
     * @throws IOException io exception
     */
    Result<Map<String, Object>> queryTaskListByProcessId(User loginUser, String projectName, Integer processId) throws IOException;

    Map<String, DependResult> parseLogForDependentResult(String log) throws IOException;

    /**
     * query sub process instance detail info by task id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskId task id
     * @return sub process instance detail
     */
    Result<Map<String, Object>> querySubProcessInstanceByTaskId(User loginUser, String projectName, Integer taskId);

    /**
     * update process instance
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceJson process instance json
     * @param processInstanceId process instance id
     * @param scheduleTime schedule time
     * @param syncDefine sync define
     * @param flag flag
     * @param locations locations
     * @param connects connects
     * @return update result code
     * @throws ParseException parse exception for json parse
     */
    Result<Void> updateProcessInstance(User loginUser, String projectName, Integer processInstanceId,
                                              String processInstanceJson, String scheduleTime, Boolean syncDefine,
                                              Flag flag, String locations, String connects) throws ParseException;

    /**
     * query parent process instance detail info by sub process instance id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param subId sub process id
     * @return parent instance detail
     */
    Result<Map<String, Object>> queryParentInstanceBySubId(User loginUser, String projectName, Integer subId);

    /**
     * delete process instance by id, at the same time，delete task instance and their mapping relation data
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @return delete result code
     */
    Result<Void> deleteProcessInstanceById(User loginUser, String projectName, Integer processInstanceId);

    /**
     * view process instance variables
     *
     * @param processInstanceId process instance id
     * @return variables data
     */
    Result<Map<String, Object>> viewVariables(Integer processInstanceId);

    /**
     * encapsulation gantt structure
     *
     * @param processInstanceId process instance id
     * @return gantt tree data
     * @throws Exception exception when json parse
     */
    Result<GanttDto> viewGantt(Integer processInstanceId) throws Exception;

    /**
     * query process instance by processDefinitionId and stateArray
     * @param processDefinitionId processDefinitionId
     * @param states states array
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineIdAndStatus(int processDefinitionId, int[] states);

    /**
     * query process instance by processDefinitionId
     * @param processDefinitionId processDefinitionId
     * @param size size
     * @return process instance list
     */
    List<ProcessInstance> queryByProcessDefineId(int processDefinitionId,int size);

}
