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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.FORCE_TASK_SUCCESS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * task instance controller
 */
@Api(tags = "TASK_INSTANCE_TAG")
@RestController
@RequestMapping("/projects/{projectName}/task-instance")
public class TaskInstanceController extends BaseController {


    @Autowired
    TaskInstanceService taskInstanceService;

    /**
     * query task list paging
     *
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startTime start time
     * @param endTime end time
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    @ApiOperation(value = "queryTaskListPaging", notes = "QUERY_TASK_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processInstanceName", value = "PROCESS_INSTANCE_NAME", required = false, type = "String"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
            @ApiImplicitParam(name = "taskName", value = "TASK_NAME", type = "String"),
            @ApiImplicitParam(name = "executorName", value = "EXECUTOR_NAME", type = "String"),
            @ApiImplicitParam(name = "stateType", value = "EXECUTION_STATUS", type = "ExecutionStatus"),
            @ApiImplicitParam(name = "host", value = "HOST", type = "String"),
            @ApiImplicitParam(name = "startDate", value = "START_DATE", type = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", type = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping("/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_LIST_PAGING_ERROR)
    @AccessLogAnnotation()
    public Result queryTaskListPaging(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                      @RequestParam(value = "processInstanceId", required = false, defaultValue = "0") Integer processInstanceId,
                                      @RequestParam(value = "processInstanceName", required = false) String processInstanceName,
                                      @RequestParam(value = "searchVal", required = false) String searchVal,
                                      @RequestParam(value = "taskName", required = false) String taskName,
                                      @RequestParam(value = "executorName", required = false) String executorName,
                                      @RequestParam(value = "stateType", required = false) ExecutionStatus stateType,
                                      @RequestParam(value = "host", required = false) String host,
                                      @RequestParam(value = "startDate", required = false) String startTime,
                                      @RequestParam(value = "endDate", required = false) String endTime,
                                      @RequestParam("pageNo") Integer pageNo,
                                      @RequestParam("pageSize") Integer pageSize) {

        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = taskInstanceService.queryTaskListPaging(
                AuthUtils.getLoginUser(), projectName, processInstanceId, processInstanceName, taskName, executorName, startTime, endTime, searchVal, stateType, host, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * change one task instance's state from FAILURE to FORCED_SUCCESS
     *
     * @param projectName project name
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    @ApiOperation(value = "force-success", notes = "FORCE_TASK_SUCCESS")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_INSTANCE_ID", required = true, dataType = "Int", example = "12")
    })
    @PostMapping(value = "/force-success")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(FORCE_TASK_SUCCESS_ERROR)
    @AccessLogAnnotation()
    public Result<Object> forceTaskSuccess(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam(value = "taskInstanceId") Integer taskInstanceId) {
        Map<String, Object> result = taskInstanceService.forceTaskSuccess(AuthUtils.getLoginUser(), projectName, taskInstanceId);
        return returnDataList(result);
    }

}
