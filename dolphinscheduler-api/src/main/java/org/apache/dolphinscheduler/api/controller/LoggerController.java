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

import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_INSTANCE_LOG_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * logger controller
 */
@Api(tags = "LOGGER_TAG")
@RestController
@RequestMapping("/log")
public class LoggerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LoggerController.class);


    @Autowired
    private LoggerService loggerService;

    /**
     * query task log
     *
     * @param loginUser      login user
     * @param taskInstanceId task instance id
     * @param skipNum        skip number
     * @param limit          limit
     * @return task log content
     */
    @ApiOperation(value = "queryLog", notes = "QUERY_TASK_INSTANCE_LOG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/detail")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_INSTANCE_LOG_ERROR)
    public Result<String> queryLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                           @RequestParam(value = "taskInstanceId") int taskInstanceId,
                           @RequestParam(value = "skipLineNum") int skipNum,
                           @RequestParam(value = "limit") int limit) {
        logger.info(
                "login user {}, view {} task instance log ,skipLineNum {} , limit {}", loginUser.getUserName(), taskInstanceId, skipNum, limit);
        return loggerService.queryLog(taskInstanceId, skipNum, limit);
    }


    /**
     * download log file
     *
     * @param loginUser      login user
     * @param taskInstanceId task instance id
     * @return log file content
     */
    @ApiOperation(value = "downloadTaskLog", notes = "DOWNLOAD_TASK_INSTANCE_LOG_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/download-log")
    @ResponseBody
    @ApiException(DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR)
    public ResponseEntity<byte[]> downloadTaskLog(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        byte[] logBytes = loggerService.getLogBytes(taskInstanceId);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + System.currentTimeMillis() + ".log" + "\"")
                .body(logBytes);
    }

}
