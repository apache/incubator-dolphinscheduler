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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_RECORD_LIST_PAGING_ERROR;

import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskRecordService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskRecord;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

/**
 * task record controller
 */
@ApiIgnore
@RestController
@RequestMapping("/projects/task-record")
public class TaskRecordController extends BaseController {


    private static final Logger logger = LoggerFactory.getLogger(TaskRecordController.class);


    @Autowired
    TaskRecordService taskRecordService;

    /**
     * query task record list page
     *
     * @param loginUser   login user
     * @param taskName    task name
     * @param state       state
     * @param sourceTable source table
     * @param destTable   destination table
     * @param taskDate    task date
     * @param startTime   start time
     * @param endTime     end time
     * @param pageNo      page number
     * @param pageSize    page size
     * @return task record list
     */
    @GetMapping("/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_RECORD_LIST_PAGING_ERROR)
    public Result<PageListVO<TaskRecord>> queryTaskRecordListPaging(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "sourceTable", required = false) String sourceTable,
            @RequestParam(value = "destTable", required = false) String destTable,
            @RequestParam(value = "taskDate", required = false) String taskDate,
            @RequestParam(value = "startDate", required = false) String startTime,
            @RequestParam(value = "endDate", required = false) String endTime,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize
    ) {

        logger.info("query task record list, task name:{}, state :{}, taskDate: {}, start:{}, end:{}",
                taskName, state, taskDate, startTime, endTime);
        return taskRecordService.queryTaskRecordListPaging(false, taskName, startTime, taskDate, sourceTable,
                destTable, endTime, state, pageNo, pageSize);
    }

    /**
     * query history task record list paging
     *
     * @param loginUser   login user
     * @param taskName    task name
     * @param state       state
     * @param sourceTable source table
     * @param destTable   destination table
     * @param taskDate    task date
     * @param startTime   start time
     * @param endTime     end time
     * @param pageNo      page number
     * @param pageSize    page size
     * @return history task record list
     */
    @GetMapping("/history-list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_RECORD_LIST_PAGING_ERROR)
    public Result<PageListVO<TaskRecord>> queryHistoryTaskRecordListPaging(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "sourceTable", required = false) String sourceTable,
            @RequestParam(value = "destTable", required = false) String destTable,
            @RequestParam(value = "taskDate", required = false) String taskDate,
            @RequestParam(value = "startDate", required = false) String startTime,
            @RequestParam(value = "endDate", required = false) String endTime,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize
    ) {

        logger.info("query hisotry task record list, task name:{}, state :{}, taskDate: {}, start:{}, end:{}",
                taskName, state, taskDate, startTime, endTime);
        return taskRecordService.queryTaskRecordListPaging(true, taskName, startTime, taskDate, sourceTable, destTable, endTime, state, pageNo, pageSize);
    }

}
