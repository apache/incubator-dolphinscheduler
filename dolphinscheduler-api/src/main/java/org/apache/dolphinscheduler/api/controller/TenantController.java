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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TENANT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TENANT_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TENANT_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TENANT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TENANT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_OS_TENANT_CODE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TenantService;
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
 * tenant controller
 */
@Api(tags = "TENANT_TAG")
@RestController
@RequestMapping("/tenant")
public class TenantController extends BaseController {

    @Autowired
    private TenantService tenantService;

    /**
     * create tenant
     *
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param description description
     * @return create result code
     */
    @ApiOperation(value = "createTenant", notes = "CREATE_TENANT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantCode", value = "TENANT_CODE", required = true, dataType = "String"),
            @ApiImplicitParam(name = "queueId", value = "QUEUE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "description", value = "TENANT_DESC", dataType = "String")

    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TENANT_ERROR)
    @AccessLogAnnotation()
    public Result createTenant(@RequestParam(value = "tenantCode") String tenantCode,
                               @RequestParam(value = "queueId") int queueId,
                               @RequestParam(value = "description", required = false) String description) throws Exception {

        Map<String, Object> result = tenantService.createTenant(AuthUtils.getLoginUser(), tenantCode, queueId, description);
        return returnDataList(result);
    }

    /**
     * query tenant list paging
     *
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return tenant list page
     */
    @ApiOperation(value = "queryTenantlistPaging", notes = "QUERY_TENANT_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TENANT_LIST_PAGING_ERROR)
    @AccessLogAnnotation()
    public Result queryTenantlistPaging(@RequestParam(value = "searchVal", required = false) String searchVal,
                                        @RequestParam("pageNo") Integer pageNo,
                                        @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = tenantService.queryTenantList(AuthUtils.getLoginUser(), searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }


    /**
     * tenant list
     *
     * @return tenant list
     */
    @ApiOperation(value = "queryTenantlist", notes = "QUERY_TENANT_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TENANT_LIST_ERROR)
    @AccessLogAnnotation()
    public Result queryTenantlist() {
        Map<String, Object> result = tenantService.queryTenantList(AuthUtils.getLoginUser());
        return returnDataList(result);
    }


    /**
     * udpate tenant
     *
     * @param id tennat id
     * @param tenantCode tennat code
     * @param queueId queue id
     * @param description description
     * @return update result code
     */
    @ApiOperation(value = "updateTenant", notes = "UPDATE_TENANT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "TENANT_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "tenantCode", value = "TENANT_CODE", required = true, dataType = "String"),
            @ApiImplicitParam(name = "queueId", value = "QUEUE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "description", value = "TENANT_DESC", type = "String")

    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TENANT_ERROR)
    @AccessLogAnnotation()
    public Result updateTenant(@RequestParam(value = "id") int id,
                               @RequestParam(value = "tenantCode") String tenantCode,
                               @RequestParam(value = "queueId") int queueId,
                               @RequestParam(value = "description", required = false) String description) throws Exception {

        Map<String, Object> result = tenantService.updateTenant(AuthUtils.getLoginUser(), id, tenantCode, queueId, description);
        return returnDataList(result);
    }

    /**
     * delete tenant by id
     *
     * @param id tenant id
     * @return delete result code
     */
    @ApiOperation(value = "deleteTenantById", notes = "DELETE_TENANT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ID", value = "TENANT_ID", required = true, dataType = "Int", example = "100")

    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TENANT_BY_ID_ERROR)
    @AccessLogAnnotation()
    public Result deleteTenantById(@RequestParam(value = "id") int id) throws Exception {
        Map<String, Object> result = tenantService.deleteTenantById(AuthUtils.getLoginUser(), id);
        return returnDataList(result);
    }

    /**
     * verify tenant code
     *
     * @param tenantCode tenant code
     * @return true if tenant code can user, otherwise return false
     */
    @ApiOperation(value = "verifyTenantCode", notes = "VERIFY_TENANT_CODE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantCode", value = "TENANT_CODE", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-tenant-code")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_OS_TENANT_CODE_ERROR)
    @AccessLogAnnotation()
    public Result verifyTenantCode(@RequestParam(value = "tenantCode") String tenantCode) {
        return tenantService.verifyTenantCode(tenantCode);
    }

}
