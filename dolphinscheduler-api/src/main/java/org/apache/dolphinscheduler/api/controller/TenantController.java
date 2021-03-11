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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
import springfox.documentation.annotations.ApiIgnore;

/**
 * tenant controller
 */
@Api(tags = "TENANT_TAG")
@RestController
@RequestMapping("/tenant")
public class TenantController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    @Autowired
    private TenantService tenantService;

    /**
     * create tenant
     *
     * @param loginUser login user
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
    public Result createTenant(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "tenantCode") String tenantCode,
                               @RequestParam(value = "queueId") int queueId,
                               @RequestParam(value = "description", required = false) String description) throws Exception {
        logger.info("login user {}, create tenant, tenantCode: {}, queueId: {}, desc: {}",
                RegexUtils.escapeNRT(loginUser.getUserName()), RegexUtils.escapeNRT(tenantCode),
                queueId, RegexUtils.escapeNRT(description));
        Map<String, Object> result = tenantService.createTenant(loginUser, tenantCode, queueId, description);
        return returnDataList(result);
    }

    /**
     * query tenant list paging
     *
     * @param loginUser login user
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
    public Result queryTenantlistPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "searchVal", required = false) String searchVal,
                                        @RequestParam("pageNo") Integer pageNo,
                                        @RequestParam("pageSize") Integer pageSize) {
        logger.info("login user {}, list paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(), pageNo, searchVal, pageSize);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = tenantService.queryTenantList(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }


    /**
     * tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    @ApiOperation(value = "queryTenantlist", notes = "QUERY_TENANT_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TENANT_LIST_ERROR)
    public Result queryTenantlist(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {}, query tenant list", loginUser.getUserName());
        Map<String, Object> result = tenantService.queryTenantList(loginUser);
        return returnDataList(result);
    }


    /**
     * update tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @param tenantCode tenant code
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
    public Result updateTenant(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "id") int id,
                               @RequestParam(value = "tenantCode") String tenantCode,
                               @RequestParam(value = "queueId") int queueId,
                               @RequestParam(value = "description", required = false) String description) throws Exception {
        String userReplace = RegexUtils.escapeNRT(loginUser.getUserName());
        String tenantCodeReplace = RegexUtils.escapeNRT(tenantCode);
        String descReplace = RegexUtils.escapeNRT(description);
        logger.info("login user {}, create tenant, tenantCode: {}, queueId: {}, desc: {}", userReplace, tenantCodeReplace, queueId, descReplace);
        Map<String, Object> result = tenantService.updateTenant(loginUser, id, tenantCode, queueId, description);
        return returnDataList(result);
    }

    /**
     * delete tenant by id
     *
     * @param loginUser login user
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
    public Result deleteTenantById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id) throws Exception {
        logger.info("login user {}, delete tenant, tenantId: {},", loginUser.getUserName(), id);
        Map<String, Object> result = tenantService.deleteTenantById(loginUser, id);
        return returnDataList(result);
    }

    /**
     * verify tenant code
     *
     * @param loginUser login user
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
    public Result verifyTenantCode(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "tenantCode") String tenantCode) {
        logger.info("login user {}, verify tenant code: {}",
                loginUser.getUserName(), tenantCode);
        return tenantService.verifyTenantCode(tenantCode);
    }

}
