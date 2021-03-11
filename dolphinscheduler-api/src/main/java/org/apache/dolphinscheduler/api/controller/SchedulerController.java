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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_SCHEDULE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.OFFLINE_SCHEDULE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.PREVIEW_SCHEDULE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.PUBLISH_SCHEDULE_ONLINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_SCHEDULE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_SCHEDULE_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_SCHEDULE_ERROR;
import static org.apache.dolphinscheduler.common.Constants.SESSION_USER;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * scheduler controller
 */
@Api(tags = "SCHEDULER_TAG")
@RestController
@RequestMapping("/projects/{projectName}/schedule")
public class SchedulerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    public static final String DEFAULT_WARNING_TYPE = "NONE";
    public static final String DEFAULT_NOTIFY_GROUP_ID = "1";
    public static final String DEFAULT_FAILURE_POLICY = "CONTINUE";
    public static final String DEFAULT_PROCESS_INSTANCE_PRIORITY = "MEDIUM";

    @Autowired
    private SchedulerService schedulerService;


    /**
     * create schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @return create result code
     */
    @ApiOperation(value = "createSchedule", notes = "CREATE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "schedule", value = "SCHEDULE", dataType = "String", example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", type = "WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", type = "FailureStrategy"),
            @ApiImplicitParam(name = "workerGroupId", value = "WORKER_GROUP_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", type = "Priority"),
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_SCHEDULE_ERROR)
    public Result createSchedule(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                 @RequestParam(value = "processDefinitionId") Integer processDefinitionId,
                                 @RequestParam(value = "schedule") String schedule,
                                 @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                 @RequestParam(value = "warningGroupId", required = false, defaultValue = DEFAULT_NOTIFY_GROUP_ID) int warningGroupId,
                                 @RequestParam(value = "failureStrategy", required = false, defaultValue = DEFAULT_FAILURE_POLICY) FailureStrategy failureStrategy,
                                 @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                 @RequestParam(value = "processInstancePriority", required = false, defaultValue = DEFAULT_PROCESS_INSTANCE_PRIORITY) Priority processInstancePriority) {
        logger.info("login user {},project name: {}, process name: {}, create schedule: {}, warning type: {}, warning group id: {},"
                        + "failure policy: {},processInstancePriority : {}, workGroupId:{}",
                RegexUtils.escapeNRT(loginUser.getUserName()), RegexUtils.escapeNRT(projectName), processDefinitionId, schedule, warningType, warningGroupId,
                failureStrategy, processInstancePriority, workerGroup);
        Map<String, Object> result = schedulerService.insertSchedule(loginUser, projectName, processDefinitionId, schedule,
                warningType, warningGroupId, failureStrategy, processInstancePriority, workerGroup);

        return returnDataList(result);
    }

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param processInstancePriority process instance priority
     * @return update result code
     */
    @ApiOperation(value = "updateSchedule", notes = "UPDATE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "SCHEDULE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "schedule", value = "SCHEDULE", dataType = "String", example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", type = "WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", type = "FailureStrategy"),
            @ApiImplicitParam(name = "workerGroupId", value = "WORKER_GROUP_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", type = "Priority"),
    })
    @PostMapping("/update")
    @ApiException(UPDATE_SCHEDULE_ERROR)
    public Result updateSchedule(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                 @RequestParam(value = "id") Integer id,
                                 @RequestParam(value = "schedule") String schedule,
                                 @RequestParam(value = "warningType", required = false, defaultValue = DEFAULT_WARNING_TYPE) WarningType warningType,
                                 @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                 @RequestParam(value = "failureStrategy", required = false, defaultValue = "END") FailureStrategy failureStrategy,
                                 @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                 @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority) {
        logger.info("login user {},project name: {},id: {}, updateProcessInstance schedule: {}, notify type: {}, notify mails: {}, "
                        + "failure policy: {},processInstancePriority : {},workerGroupId:{}",
                RegexUtils.escapeNRT(loginUser.getUserName()), RegexUtils.escapeNRT(projectName), id, schedule, warningType, warningGroupId, failureStrategy,
                processInstancePriority, workerGroup);

        Map<String, Object> result = schedulerService.updateSchedule(loginUser, projectName, id, schedule,
                warningType, warningGroupId, failureStrategy, null, processInstancePriority, workerGroup);
        return returnDataList(result);
    }

    /**
     * publish schedule setScheduleState
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @return publish result code
     */
    @ApiOperation(value = "online", notes = "ONLINE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "SCHEDULE_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping("/online")
    @ApiException(PUBLISH_SCHEDULE_ONLINE_ERROR)
    public Result online(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                         @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                         @RequestParam("id") Integer id) {
        logger.info("login user {}, schedule setScheduleState, project name: {}, id: {}",
                loginUser.getUserName(), projectName, id);
        Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.ONLINE);
        return returnDataList(result);
    }

    /**
     * offline schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id schedule id
     * @return operation result code
     */
    @ApiOperation(value = "offline", notes = "OFFLINE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "SCHEDULE_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping("/offline")
    @ApiException(OFFLINE_SCHEDULE_ERROR)
    public Result offline(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                          @RequestParam("id") Integer id) {
        logger.info("login user {}, schedule offline, project name: {}, process definition id: {}",
                loginUser.getUserName(), projectName, id);

        Map<String, Object> result = schedulerService.setScheduleState(loginUser, projectName, id, ReleaseState.OFFLINE);
        return returnDataList(result);
    }

    /**
     * query schedule list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return schedule list page
     */
    @ApiOperation(value = "queryScheduleListPaging", notes = "QUERY_SCHEDULE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "100")

    })
    @GetMapping("/list-paging")
    @ApiException(QUERY_SCHEDULE_LIST_PAGING_ERROR)
    public Result queryScheduleListPaging(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam Integer processDefinitionId,
                                          @RequestParam(value = "searchVal", required = false) String searchVal,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam("pageSize") Integer pageSize) {
        logger.info("login user {}, query schedule, project name: {}, process definition id: {}",
                loginUser.getUserName(), projectName, processDefinitionId);
        searchVal = ParameterUtils.handleEscapes(searchVal);
        Map<String, Object> result = schedulerService.querySchedule(loginUser, projectName, processDefinitionId, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * delete schedule by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param scheduleId schedule id
     * @return delete result code
     */
    @ApiOperation(value = "deleteScheduleById", notes = "OFFLINE_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scheduleId", value = "SCHEDULE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_SCHEDULE_CRON_BY_ID_ERROR)
    public Result deleteScheduleById(@RequestAttribute(value = SESSION_USER) User loginUser,
                                     @PathVariable String projectName,
                                     @RequestParam("scheduleId") Integer scheduleId
    ) {
        logger.info("delete schedule by id, login user:{}, project name:{}, schedule id:{}",
                loginUser.getUserName(), projectName, scheduleId);
        Map<String, Object> result = schedulerService.deleteScheduleById(loginUser, projectName, scheduleId);
        return returnDataList(result);
    }

    /**
     * query schedule list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return schedule list
     */
    @ApiOperation(value = "queryScheduleList", notes = "QUERY_SCHEDULE_LIST_NOTES")
    @PostMapping("/list")
    @ApiException(QUERY_SCHEDULE_LIST_ERROR)
    public Result queryScheduleList(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                    @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName) {
        logger.info("login user {}, query schedule list, project name: {}",
                loginUser.getUserName(), projectName);
        Map<String, Object> result = schedulerService.queryScheduleList(loginUser, projectName);
        return returnDataList(result);
    }

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param schedule schedule expression
     * @return the next five fire time
     */
    @ApiOperation(value = "previewSchedule", notes = "PREVIEW_SCHEDULE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "schedule", value = "SCHEDULE", dataType = "String", example = "{'startTime':'2019-06-10 00:00:00','endTime':'2019-06-13 00:00:00','crontab':'0 0 3/6 * * ? *'}"),
    })
    @PostMapping("/preview")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(PREVIEW_SCHEDULE_ERROR)
    public Result previewSchedule(@ApiIgnore @RequestAttribute(value = SESSION_USER) User loginUser,
                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                  @RequestParam(value = "schedule") String schedule
    ) {
        logger.info("login user {}, project name: {}, preview schedule: {}",
                loginUser.getUserName(), projectName, schedule);
        Map<String, Object> result = schedulerService.previewSchedule(loginUser, projectName, schedule);
        return returnDataList(result);
    }
}
