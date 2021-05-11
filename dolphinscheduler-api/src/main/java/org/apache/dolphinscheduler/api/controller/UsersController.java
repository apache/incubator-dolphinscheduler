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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_USER_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_USER_INFO_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_PROJECT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GRANT_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_USER_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_USER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.USER_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_USERNAME_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * users controller
 */
@Api(tags = "USERS_TAG")
@RestController
@RequestMapping("/users")
public class UsersController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UsersService usersService;

    /**
     * create user
     *
     * @param userName user name
     * @param userPassword user password
     * @param email email
     * @param tenantId tenant id
     * @param phone phone
     * @param queue queue
     * @return create result code
     */
    @ApiOperation(value = "createUser", notes = "CREATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", type = "String"),
            @ApiImplicitParam(name = "tenantId", value = "TENANT_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "QUEUE", dataType = "String"),
            @ApiImplicitParam(name = "email", value = "EMAIL", dataType = "String"),
            @ApiImplicitParam(name = "phone", value = "PHONE", dataType = "String"),
            @ApiImplicitParam(name = "state", value = "STATE", dataType = "Int", example = "1")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"userPassword"})
    public Result createUser(@RequestParam(value = "userName") String userName,
                             @RequestParam(value = "userPassword") String userPassword,
                             @RequestParam(value = "tenantId") int tenantId,
                             @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "state", required = false) int state) throws Exception {
        Map<String, Object> result = usersService.createUser(AuthUtils.getLoginUser(), userName, userPassword, email, tenantId, phone, queue, state);
        return returnDataList(result);
    }

    /**
     * query user list paging
     *
     * @param pageNo page number
     * @param searchVal search avlue
     * @param pageSize page size
     * @return user list page
     */
    @ApiOperation(value = "queryUserList", notes = "QUERY_USER_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", type = "String")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_USER_LIST_PAGING_ERROR)
    @AccessLogAnnotation()
    public Result queryUserList(@RequestParam("pageNo") Integer pageNo,
                                @RequestParam("pageSize") Integer pageSize,
                                @RequestParam(value = "searchVal", required = false) String searchVal) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = usersService.queryUserList(AuthUtils.getLoginUser(), searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }


    /**
     * update user
     *
     * @param id user id
     * @param userName user name
     * @param userPassword user password
     * @param email email
     * @param tenantId tennat id
     * @param phone phone
     * @param queue queue
     * @return update result code
     */
    @ApiOperation(value = "updateUser", notes = "UPDATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "USER_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", type = "String"),
            @ApiImplicitParam(name = "tenantId", value = "TENANT_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "queue", value = "QUEUE", dataType = "String"),
            @ApiImplicitParam(name = "email", value = "EMAIL", dataType = "String"),
            @ApiImplicitParam(name = "phone", value = "PHONE", dataType = "String"),
            @ApiImplicitParam(name = "state", value = "STATE", dataType = "Int", example = "1")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"userPassword"})
    public Result updateUser(@RequestParam(value = "id") int id,
                             @RequestParam(value = "userName") String userName,
                             @RequestParam(value = "userPassword") String userPassword,
                             @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "tenantId") int tenantId,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "state", required = false) int state) throws Exception {
        Map<String, Object> result = usersService.updateUser(AuthUtils.getLoginUser(), id, userName, userPassword, email, tenantId, phone, queue, state);
        return returnDataList(result);
    }

    /**
     * delete user by id
     *
     * @param id user id
     * @return delete result code
     */
    @ApiOperation(value = "delUserById", notes = "DELETE_USER_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "USER_ID", dataType = "Int", example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_USER_BY_ID_ERROR)
    @AccessLogAnnotation
    public Result delUserById(@RequestParam(value = "id") int id) throws Exception {
        Map<String, Object> result = usersService.deleteUserById(AuthUtils.getLoginUser(), id);
        return returnDataList(result);
    }

    /**
     * grant project
     *
     * @param userId user id
     * @param projectIds project id array
     * @return grant result code
     */
    @ApiOperation(value = "grantProject", notes = "GRANT_PROJECT_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "projectIds", value = "PROJECT_IDS", type = "String")
    })
    @PostMapping(value = "/grant-project")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_PROJECT_ERROR)
    @AccessLogAnnotation
    public Result grantProject(@RequestParam(value = "userId") int userId,
                               @RequestParam(value = "projectIds") String projectIds) {
        Map<String, Object> result = usersService.grantProject(AuthUtils.getLoginUser(), userId, projectIds);
        return returnDataList(result);
    }

    /**
     * grant resource
     *
     * @param userId user id
     * @param resourceIds resource id array
     * @return grant result code
     */
    @ApiOperation(value = "grantResource", notes = "GRANT_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "resourceIds", value = "RESOURCE_IDS", type = "String")
    })
    @PostMapping(value = "/grant-file")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_RESOURCE_ERROR)
    @AccessLogAnnotation
    public Result grantResource(@RequestParam(value = "userId") int userId,
                                @RequestParam(value = "resourceIds") String resourceIds) {
        Map<String, Object> result = usersService.grantResources(AuthUtils.getLoginUser(), userId, resourceIds);
        return returnDataList(result);
    }


    /**
     * grant udf function
     *
     * @param userId user id
     * @param udfIds udf id array
     * @return grant result code
     */
    @ApiOperation(value = "grantUDFFunc", notes = "GRANT_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "udfIds", value = "UDF_IDS", type = "String")
    })
    @PostMapping(value = "/grant-udf-func")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_UDF_FUNCTION_ERROR)
    @AccessLogAnnotation
    public Result grantUDFFunc(@RequestParam(value = "userId") int userId,
                               @RequestParam(value = "udfIds") String udfIds) {
        Map<String, Object> result = usersService.grantUDFFunction(AuthUtils.getLoginUser(), userId, udfIds);
        return returnDataList(result);
    }


    /**
     * grant datasource
     *
     * @param userId user id
     * @param datasourceIds data source id array
     * @return grant result code
     */
    @ApiOperation(value = "grantDataSource", notes = "GRANT_DATASOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "datasourceIds", value = "DATASOURCE_IDS", type = "String")
    })
    @PostMapping(value = "/grant-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GRANT_DATASOURCE_ERROR)
    @AccessLogAnnotation
    public Result grantDataSource(@RequestParam(value = "userId") int userId,
                                  @RequestParam(value = "datasourceIds") String datasourceIds) {
        Map<String, Object> result = usersService.grantDataSource(AuthUtils.getLoginUser(), userId, datasourceIds);
        return returnDataList(result);
    }


    /**
     * get user info
     *
     * @return user info
     */
    @ApiOperation(value = "getUserInfo", notes = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/get-user-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_USER_INFO_ERROR)
    @AccessLogAnnotation
    public Result getUserInfo() {
        Map<String, Object> result = usersService.getUserInfo(AuthUtils.getLoginUser());
        return returnDataList(result);
    }

    /**
     * user list no paging
     *
     * @return user list
     */
    @ApiOperation(value = "listUser", notes = "LIST_USER_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    @AccessLogAnnotation
    public Result listUser() {
        Map<String, Object> result = usersService.queryAllGeneralUsers(AuthUtils.getLoginUser());
        return returnDataList(result);
    }


    /**
     * user list no paging
     *
     * @return user list
     */
    @GetMapping(value = "/list-all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    @AccessLogAnnotation
    public Result listAll() {
        Map<String, Object> result = usersService.queryUserList(AuthUtils.getLoginUser());
        return returnDataList(result);
    }


    /**
     * verify username
     *
     * @param userName user name
     * @return true if user name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyUserName", notes = "VERIFY_USER_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String")
    })
    @GetMapping(value = "/verify-user-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_USERNAME_ERROR)
    @AccessLogAnnotation
    public Result verifyUserName(@RequestParam(value = "userName") String userName
    ) {
        return usersService.verifyUserName(userName);
    }


    /**
     * unauthorized user
     *
     * @param alertgroupId alert group id
     * @return unauthorize result code
     */
    @ApiOperation(value = "unauthorizedUser", notes = "UNAUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertgroupId", value = "ALERT_GROUP_ID", type = "String")
    })
    @GetMapping(value = "/unauth-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_USER_ERROR)
    @AccessLogAnnotation
    public Result unauthorizedUser(@RequestParam("alertgroupId") Integer alertgroupId) {
        Map<String, Object> result = usersService.unauthorizedUser(AuthUtils.getLoginUser(), alertgroupId);
        return returnDataList(result);
    }


    /**
     * authorized user
     *
     * @param alertgroupId alert group id
     * @return authorized result code
     */
    @ApiOperation(value = "authorizedUser", notes = "AUTHORIZED_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertgroupId", value = "ALERT_GROUP_ID", type = "String")
    })
    @GetMapping(value = "/authed-user")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_USER_ERROR)
    @AccessLogAnnotation
    public Result authorizedUser(@RequestParam("alertgroupId") Integer alertgroupId) {
        try {
            Map<String, Object> result = usersService.authorizedUser(AuthUtils.getLoginUser(), alertgroupId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.AUTHORIZED_USER_ERROR.getMsg(), e);
            return error(Status.AUTHORIZED_USER_ERROR.getCode(), Status.AUTHORIZED_USER_ERROR.getMsg());
        }
    }

    /**
     * user register
     *
     * @param userName user name
     * @param userPassword user password
     * @param repeatPassword repeat password
     * @param email user email
     */
    @ApiOperation(value = "registerUser", notes = "REGISTER_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", type = "String"),
            @ApiImplicitParam(name = "repeatPassword", value = "REPEAT_PASSWORD", type = "String"),
            @ApiImplicitParam(name = "email", value = "EMAIL", type = "String"),
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CREATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> registerUser(@RequestParam(value = "userName") String userName,
                                       @RequestParam(value = "userPassword") String userPassword,
                                       @RequestParam(value = "repeatPassword") String repeatPassword,
                                       @RequestParam(value = "email") String email) throws Exception {
        userName = ParameterUtils.handleEscapes(userName);
        userPassword = ParameterUtils.handleEscapes(userPassword);
        repeatPassword = ParameterUtils.handleEscapes(repeatPassword);
        email = ParameterUtils.handleEscapes(email);
        Map<String, Object> result = usersService.registerUser(userName, userPassword, repeatPassword, email);
        return returnDataList(result);
    }

    /**
     * user activate
     *
     * @param userName user name
     */
    @ApiOperation(value = "activateUser", notes = "ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
    })
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> activateUser(@RequestParam(value = "userName") String userName) {
        userName = ParameterUtils.handleEscapes(userName);
        Map<String, Object> result = usersService.activateUser(AuthUtils.getLoginUser(), userName);
        return returnDataList(result);
    }

    /**
     * user batch activate
     *
     * @param userNames user names
     */
    @ApiOperation(value = "batchActivateUser", notes = "BATCH_ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "USER_NAMES", type = "String"),
    })
    @PostMapping("/batch/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    @AccessLogAnnotation
    public Result<Object> batchActivateUser(@RequestBody List<String> userNames) {
        List<String> formatUserNames = userNames.stream().map(ParameterUtils::handleEscapes).collect(Collectors.toList());
        Map<String, Object> result = usersService.batchActivateUser(AuthUtils.getLoginUser(), formatUserNames);
        return returnDataList(result);
    }
}
