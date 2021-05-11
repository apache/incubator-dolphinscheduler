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

import static org.apache.dolphinscheduler.api.enums.Status.COMMAND_STATE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_DEFINITION_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.COUNT_PROCESS_INSTANCE_STATE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUEUE_COUNT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.TASK_INSTANCE_STATE_COUNT_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * data analysis controller
 */
@Api(tags = "DATA_ANALYSIS_TAG")
@RestController
@RequestMapping("projects/analysis")
public class DataAnalysisController extends BaseController {

    @Autowired
    DataAnalysisService dataAnalysisService;

    /**
     * statistical task instance status data
     *
     * @param startDate count start date
     * @param endDate count end date
     * @param projectId project id
     * @return task instance count data
     */
    @ApiOperation(value = "countTaskState", notes = "COUNT_TASK_STATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/task-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(TASK_INSTANCE_STATE_COUNT_ERROR)
    @AccessLogAnnotation()
    public Result countTaskState(@RequestParam(value = "startDate", required = false) String startDate,
                                 @RequestParam(value = "endDate", required = false) String endDate,
                                 @RequestParam(value = "projectId", required = false, defaultValue = "0") int projectId) {

        Map<String, Object> result = dataAnalysisService.countTaskStateByProject(AuthUtils.getLoginUser(), projectId, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * statistical process instance status data
     *
     * @param startDate start date
     * @param endDate end date
     * @param projectId project id
     * @return process instance data
     */
    @ApiOperation(value = "countProcessInstanceState", notes = "COUNT_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/process-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_INSTANCE_STATE_ERROR)
    @AccessLogAnnotation()
    public Result countProcessInstanceState(@RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate,
                                            @RequestParam(value = "projectId", required = false, defaultValue = "0") int projectId) {

        Map<String, Object> result = dataAnalysisService.countProcessInstanceStateByProject(AuthUtils.getLoginUser(), projectId, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * statistics the process definition quantities of certain person
     *
     * @param projectId project id
     * @return definition count in project id
     */
    @ApiOperation(value = "countDefinitionByUser", notes = "COUNT_PROCESS_DEFINITION_BY_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/define-user-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COUNT_PROCESS_DEFINITION_USER_ERROR)
    @AccessLogAnnotation()
    public Result countDefinitionByUser(@RequestParam(value = "projectId", required = false, defaultValue = "0") int projectId) {

        Map<String, Object> result = dataAnalysisService.countDefinitionByUser(AuthUtils.getLoginUser(), projectId);
        return returnDataList(result);
    }

    /**
     * statistical command status data
     *
     * @param startDate start date
     * @param endDate end date
     * @param projectId project id
     * @return command state in project id
     */
    @ApiOperation(value = "countCommandState", notes = "COUNT_COMMAND_STATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "START_DATE", dataType = "String"),
            @ApiImplicitParam(name = "endDate", value = "END_DATE", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/command-state-count")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COMMAND_STATE_COUNT_ERROR)
    @AccessLogAnnotation()
    public Result countCommandState(@RequestParam(value = "startDate", required = false) String startDate,
                                    @RequestParam(value = "endDate", required = false) String endDate,
                                    @RequestParam(value = "projectId", required = false, defaultValue = "0") int projectId) {

        Map<String, Object> result = dataAnalysisService.countCommandState(AuthUtils.getLoginUser(), projectId, startDate, endDate);
        return returnDataList(result);
    }

    /**
     * queue count
     *
     * @param projectId project id
     * @return queue state count
     */
    @ApiOperation(value = "countQueueState", notes = "COUNT_QUEUE_STATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectId", value = "PROJECT_ID", dataType = "Int", example = "100")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/queue-count")
    @ApiException(QUEUE_COUNT_ERROR)
    @AccessLogAnnotation()
    public Result countQueueState(@RequestParam(value = "projectId", required = false, defaultValue = "0") int projectId) {
        Map<String, Object> result = dataAnalysisService.countQueueState(AuthUtils.getLoginUser(), projectId);
        return returnDataList(result);
    }

}
