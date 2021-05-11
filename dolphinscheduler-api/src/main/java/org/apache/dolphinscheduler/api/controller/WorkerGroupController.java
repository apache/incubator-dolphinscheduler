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

import static org.apache.dolphinscheduler.api.enums.Status.DELETE_WORKER_GROUP_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKER_ADDRESS_LIST_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKER_GROUP_FAIL;
import static org.apache.dolphinscheduler.api.enums.Status.SAVE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
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
 * worker group controller
 */
@Api(tags = "WORKER_GROUP_TAG")
@RestController
@RequestMapping("/worker-group")
public class WorkerGroupController extends BaseController {

    @Autowired
    WorkerGroupService workerGroupService;

    /**
     * create or update a worker group
     *
     * @param id worker group id
     * @param name worker group name
     * @param addrList addr list
     * @return create or update result code
     */
    @ApiOperation(value = "saveWorkerGroup", notes = "CREATE_WORKER_GROUP_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", dataType = "Int", example = "10", defaultValue = "0"),
            @ApiImplicitParam(name = "name", value = "WORKER_GROUP_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "addrList", value = "WORKER_ADDR_LIST", required = true, dataType = "String")
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SAVE_ERROR)
    @AccessLogAnnotation()
    public Result saveWorkerGroup(@RequestParam(value = "id", required = false, defaultValue = "0") int id,
                                  @RequestParam(value = "name") String name,
                                  @RequestParam(value = "addrList") String addrList
    ) {
        Map<String, Object> result = workerGroupService.saveWorkerGroup(AuthUtils.getLoginUser(), id, name, addrList);
        return returnDataList(result);
    }

    /**
     * query worker groups paging
     *
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    @ApiOperation(value = "queryAllWorkerGroupsPaging", notes = "QUERY_WORKER_GROUP_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation()
    public Result queryAllWorkerGroupsPaging(@RequestParam("pageNo") Integer pageNo,
                                             @RequestParam("pageSize") Integer pageSize,
                                             @RequestParam(value = "searchVal", required = false) String searchVal
    ) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = workerGroupService.queryAllGroupPaging(AuthUtils.getLoginUser(), pageNo, pageSize, searchVal);
        return returnDataListPaging(result);
    }

    /**
     * query all worker groups
     *
     * @return all worker group list
     */
    @ApiOperation(value = "queryAllWorkerGroups", notes = "QUERY_WORKER_GROUP_LIST_NOTES")
    @GetMapping(value = "/all-groups")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_GROUP_FAIL)
    @AccessLogAnnotation()
    public Result queryAllWorkerGroups() {
        Map<String, Object> result = workerGroupService.queryAllGroup();
        return returnDataList(result);
    }

    /**
     * delete worker group by id
     *
     * @param id group id
     * @return delete result code
     */
    @ApiOperation(value = "deleteById", notes = "DELETE_WORKER_GROUP_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "WORKER_GROUP_ID", required = true, dataType = "Int", example = "10"),
    })
    @PostMapping(value = "/delete-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_WORKER_GROUP_FAIL)
    @AccessLogAnnotation()
    public Result deleteById(@RequestParam("id") Integer id
    ) {
        Map<String, Object> result = workerGroupService.deleteWorkerGroupById(AuthUtils.getLoginUser(), id);
        return returnDataList(result);
    }

    /**
     * query worker address list
     *
     * @return all worker address list
     */
    @ApiOperation(value = "queryWorkerAddressList", notes = "QUERY_WORKER_ADDRESS_LIST_NOTES")
    @GetMapping(value = "/worker-address-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_WORKER_ADDRESS_LIST_FAIL)
    @AccessLogAnnotation()
    public Result queryWorkerAddressList() {
        Map<String, Object> result = workerGroupService.getWorkerAddressList();
        return returnDataList(result);
    }

}
