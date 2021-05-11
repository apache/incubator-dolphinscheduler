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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_QUEUE_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_QUEUE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_QUEUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * queue controller
 */
@Api(tags = "QUEUE_TAG")
@RestController
@RequestMapping("/queue")
public class QueueController extends BaseController {

    @Autowired
    private QueueService queueService;


    /**
     * query queue list
     *
     * @return queue list
     */
    @ApiOperation(value = "queryList", notes = "QUERY_QUEUE_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    @AccessLogAnnotation()
    public Result queryList() {
        Map<String, Object> result = queueService.queryList(AuthUtils.getLoginUser());
        return returnDataList(result);
    }

    /**
     * query queue list paging
     *
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return queue list
     */
    @ApiOperation(value = "queryQueueListPaging", notes = "QUERY_QUEUE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_QUEUE_LIST_ERROR)
    @AccessLogAnnotation()
    public Result queryQueueListPaging(@RequestParam("pageNo") Integer pageNo,
                                       @RequestParam(value = "searchVal", required = false) String searchVal,
                                       @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = queueService.queryList(AuthUtils.getLoginUser(), searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * create queue
     *
     * @param queue queue
     * @param queueName queue name
     * @return create result
     */
    @ApiOperation(value = "createQueue", notes = "CREATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataType = "String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_QUEUE_ERROR)
    @AccessLogAnnotation()
    public Result createQueue(@RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        Map<String, Object> result = queueService.createQueue(AuthUtils.getLoginUser(), queue, queueName);
        return returnDataList(result);
    }

    /**
     * update queue
     *
     * @param queue queue
     * @param id queue id
     * @param queueName queue name
     * @return update result code
     */
    @ApiOperation(value = "updateQueue", notes = "UPDATE_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "QUEUE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataType = "String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UPDATE_QUEUE_ERROR)
    @AccessLogAnnotation()
    public Result updateQueue(@RequestParam(value = "id") int id,
                              @RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName) {
        Map<String, Object> result = queueService.updateQueue(AuthUtils.getLoginUser(), id, queue, queueName);
        return returnDataList(result);
    }

    /**
     * verify queue and queue name
     *
     * @param queue queue
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyQueue", notes = "VERIFY_QUEUE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "QUEUE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "YARN_QUEUE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "queueName", value = "QUEUE_NAME", required = true, dataType = "String")
    })
    @PostMapping(value = "/verify-queue")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_QUEUE_ERROR)
    @AccessLogAnnotation()
    public Result verifyQueue(@RequestParam(value = "queue") String queue,
                              @RequestParam(value = "queueName") String queueName
    ) {

        return queueService.verifyQueue(queue, queueName);
    }

}
