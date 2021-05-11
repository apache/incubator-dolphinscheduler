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

import static org.apache.dolphinscheduler.api.enums.Status.CHECK_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_PROCESS_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
 * executor controller
 */
@Api(tags = "EXECUTOR_TAG")
@RestController
@RequestMapping("projects/{projectName}/executors")
public class ExecutorController extends BaseController {

    @Autowired
    private ExecutorService execService;

    /**
     * execute process instance
     *
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param scheduleTime schedule time
     * @param failureStrategy failure strategy
     * @param startNodeList start nodes list
     * @param taskDependType task depend type
     * @param execType execute type
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param runMode run mode
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @param timeout timeout
     * @return start process result code
     */
    @ApiOperation(value = "startProcessInstance", notes = "RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataType = "FailureStrategy"),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataType = "String"),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataType = "TaskDependType"),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataType = "CommandType"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataType = "WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE", dataType = "RunMode"),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataType = "Priority"),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataType = "String", example = "default"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int", example = "100"),
    })
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result startProcessInstance(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                       @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                       @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
                                       @RequestParam(value = "failureStrategy", required = true) FailureStrategy failureStrategy,
                                       @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                       @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                       @RequestParam(value = "execType", required = false) CommandType execType,
                                       @RequestParam(value = "warningType", required = true) WarningType warningType,
                                       @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                       @RequestParam(value = "runMode", required = false) RunMode runMode,
                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                       @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                       @RequestParam(value = "timeout", required = false) Integer timeout,
                                       @RequestParam(value = "startParams", required = false) String startParams) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }
        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }
        Map<String, Object> result = execService.execProcessInstance(AuthUtils.getLoginUser(), projectName, processDefinitionId, scheduleTime, execType, failureStrategy,
                startNodeList, taskDependType, warningType,
                warningGroupId, runMode, processInstancePriority, workerGroup, timeout, startParamMap);
        return returnDataList(result);
    }


    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @ApiOperation(value = "execute", notes = "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataType = "ExecuteType")
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result execute(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                          @RequestParam("processInstanceId") Integer processInstanceId,
                          @RequestParam("executeType") ExecuteType executeType
    ) {
        Map<String, Object> result = execService.execute(AuthUtils.getLoginUser(), projectName, processInstanceId, executeType);
        return returnDataList(result);
    }

    /**
     * check process definition and all of the son process definitions is on line.
     *
     * @param processDefinitionId process definition id
     * @return check result code
     */
    @ApiOperation(value = "startCheckProcessDefinition", notes = "START_CHECK_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CHECK_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result startCheckProcessDefinition(@RequestParam(value = "processDefinitionId") int processDefinitionId) {
        Map<String, Object> result = execService.startCheckByProcessDefinedId(processDefinitionId);
        return returnDataList(result);
    }
}
