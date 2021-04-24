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

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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
     * @param loginUser    login user
     * @param userName     user name
     * @param userPassword user password
     * @param email        email
     * @param tenantId     tenant id
     * @param phone        phone
     * @param queue        queue
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
    public Result<Void> createUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "userName") String userName,
                             @RequestParam(value = "userPassword") String userPassword,
                             @RequestParam(value = "tenantId") int tenantId,
                             @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "phone", required = false) String phone,
                             @RequestParam(value = "state", required = false) int state) throws Exception {
        logger.info("login user {}, create user, userName: {}, email: {}, tenantId: {}, userPassword: {}, phone: {}, user queue: {}, state: {}",
                loginUser.getUserName(), userName, email, tenantId, Constants.PASSWORD_DEFAULT, phone, queue, state);
        return usersService.createUser(loginUser, userName, userPassword, email, tenantId, phone, queue, state);
    }

    /**
     * query user list paging
     *
     * @param loginUser login user
     * @param pageNo    page number
     * @param searchVal search avlue
     * @param pageSize  page size
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
    public Result<PageListVO<User>> queryUserList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @RequestParam("pageNo") Integer pageNo,
                                                  @RequestParam("pageSize") Integer pageSize,
                                                  @RequestParam(value = "searchVal", required = false) String searchVal) {
        logger.info("login user {}, list user paging, pageNo: {}, searchVal: {}, pageSize: {}",
                loginUser.getUserName(), pageNo, searchVal, pageSize);
        CheckParamResult result = checkPageParams(pageNo, pageSize);
        if (result.getStatus() != Status.SUCCESS) {
            return error(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        return usersService.queryUserList(loginUser, searchVal, pageNo, pageSize);
    }


    /**
     * update user
     *
     * @param loginUser    login user
     * @param id           user id
     * @param userName     user name
     * @param userPassword user password
     * @param email        email
     * @param tenantId     tennat id
     * @param phone        phone
     * @param queue        queue
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
    public Result<Void> updateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam(value = "id") int id,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "userPassword") String userPassword,
                                   @RequestParam(value = "queue", required = false, defaultValue = "") String queue,
                                   @RequestParam(value = "email") String email,
                                   @RequestParam(value = "tenantId") int tenantId,
                                   @RequestParam(value = "phone", required = false) String phone,
                                   @RequestParam(value = "state", required = false) int state) throws Exception {
        logger.info("login user {}, updateProcessInstance user, userName: {}, email: {}, tenantId: {}, userPassword: {}, phone: {}, user queue: {}, state: {}",
                loginUser.getUserName(), userName, email, tenantId, Constants.PASSWORD_DEFAULT, phone, queue, state);
        return usersService.updateUser(loginUser, id, userName, userPassword, email, tenantId, phone, queue, state);
    }

    /**
     * delete user by id
     *
     * @param loginUser login user
     * @param id        user id
     * @return delete result code
     */
    @ApiOperation(value = "delUserById", notes = "DELETE_USER_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "USER_ID", dataType = "Int", example = "100")
    })
    @PostMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_USER_BY_ID_ERROR)
    public Result<Void> delUserById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "id") int id) throws Exception {
        logger.info("login user {}, delete user, userId: {},", loginUser.getUserName(), id);
        return usersService.deleteUserById(loginUser, id);
    }

    /**
     * grant project
     *
     * @param loginUser  login user
     * @param userId     user id
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
    public Result<Void> grantProject(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "userId") int userId,
                                     @RequestParam(value = "projectIds") String projectIds) {
        logger.info("login user {}, grant project, userId: {},projectIds : {}", loginUser.getUserName(), userId, projectIds);
        return usersService.grantProject(loginUser, userId, projectIds);
    }

    /**
     * grant resource
     *
     * @param loginUser   login user
     * @param userId      user id
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
    public Result<Void> grantResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam(value = "userId") int userId,
                                      @RequestParam(value = "resourceIds") String resourceIds) {
        logger.info("login user {}, grant project, userId: {},resourceIds : {}", loginUser.getUserName(), userId, resourceIds);
        return usersService.grantResources(loginUser, userId, resourceIds);
    }


    /**
     * grant udf function
     *
     * @param loginUser login user
     * @param userId    user id
     * @param udfIds    udf id array
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
    public Result<Void> grantUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "userId") int userId,
                               @RequestParam(value = "udfIds") String udfIds) {
        logger.info("login user {}, grant project, userId: {},resourceIds : {}", loginUser.getUserName(), userId, udfIds);
        return usersService.grantUDFFunction(loginUser, userId, udfIds);
    }


    /**
     * grant datasource
     *
     * @param loginUser     login user
     * @param userId        user id
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
    public Result<Void> grantDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "userId") int userId,
                                  @RequestParam(value = "datasourceIds") String datasourceIds) {
        logger.info("login user {}, grant project, userId: {},projectIds : {}", loginUser.getUserName(), userId, datasourceIds);
        return usersService.grantDataSource(loginUser, userId, datasourceIds);
    }


    /**
     * get user info
     *
     * @param loginUser login user
     * @return user info
     */
    @ApiOperation(value = "getUserInfo", notes = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/get-user-info")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_USER_INFO_ERROR)
    public Result<User> getUserInfo(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {},get user info", loginUser.getUserName());
        return usersService.getUserInfo(loginUser);
    }

    /**
     * user list no paging
     *
     * @param loginUser login user
     * @return user list
     */
    @ApiOperation(value = "listUser", notes = "LIST_USER_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    public Result<List<User>> listUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {}, user list", loginUser.getUserName());
        return usersService.queryAllGeneralUsers(loginUser);
    }


    /**
     * user list no paging
     *
     * @param loginUser login user
     * @return user list
     */
    @GetMapping(value = "/list-all")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(USER_LIST_ERROR)
    public Result<List<User>> listAll(@RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {}, user list", loginUser.getUserName());
        return usersService.queryUserList(loginUser);
    }


    /**
     * verify username
     *
     * @param loginUser login user
     * @param userName  user name
     * @return true if user name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyUserName", notes = "VERIFY_USER_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String")
    })
    @GetMapping(value = "/verify-user-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_USERNAME_ERROR)
    public Result verifyUserName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "userName") String userName
    ) {
        logger.info("login user {}, verfiy user name: {}",
                loginUser.getUserName(), userName);
        return usersService.verifyUserName(userName);
    }


    /**
     * unauthorized user
     *
     * @param loginUser    login user
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
    public Result<List<User>> unauthorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("alertgroupId") Integer alertgroupId) {
        logger.info("unauthorized user, login user:{}, alert group id:{}",
                loginUser.getUserName(), alertgroupId);
        return usersService.unauthorizedUser(loginUser, alertgroupId);
    }


    /**
     * authorized user
     *
     * @param loginUser    login user
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
    public Result<List<User>> authorizedUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("alertgroupId") Integer alertgroupId) {
        try {
            logger.info("authorized user , login user:{}, alert group id:{}",
                    loginUser.getUserName(), alertgroupId);
            return usersService.authorizedUser(loginUser, alertgroupId);
        } catch (Exception e) {
            logger.error(Status.AUTHORIZED_USER_ERROR.getMsg(), e);
            return Result.error(Status.AUTHORIZED_USER_ERROR);
        }
    }

    /**
     * user register
     *
     * @param userName       user name
     * @param userPassword   user password
     * @param repeatPassword repeat password
     * @param email          user email
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
    public Result<User> registerUser(@RequestParam(value = "userName") String userName,
                                     @RequestParam(value = "userPassword") String userPassword,
                                     @RequestParam(value = "repeatPassword") String repeatPassword,
                                     @RequestParam(value = "email") String email) throws Exception {
        userName = ParameterUtils.handleEscapes(userName);
        userPassword = ParameterUtils.handleEscapes(userPassword);
        repeatPassword = ParameterUtils.handleEscapes(repeatPassword);
        email = ParameterUtils.handleEscapes(email);
        logger.info("user self-register, userName: {}, userPassword {}, repeatPassword {}, eamil {}",
                userName, Constants.PASSWORD_DEFAULT, Constants.PASSWORD_DEFAULT, email);
        return usersService.registerUser(userName, userPassword, repeatPassword, email);
    }

    /**
     * user activate
     *
     * @param userName       user name
     */
    @ApiOperation(value = "activateUser", notes = "ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", type = "String"),
    })
    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    public Result<User> activateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "userName") String userName) {
        userName = ParameterUtils.handleEscapes(userName);
        logger.info("login user {}, activate user, userName: {}",
                loginUser.getUserName(), userName);
        return usersService.activateUser(loginUser, userName);
    }

    /**
     * user batch activate
     *
     * @param  userNames       user names
     */
    @ApiOperation(value = "batchActivateUser", notes = "BATCH_ACTIVATE_USER_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userNames", value = "USER_NAMES", type = "String"),
    })
    @PostMapping("/batch/activate")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_USER_ERROR)
    public Result<Map<String, Object>> batchActivateUser(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                         @RequestBody List<String> userNames) {
        List<String> formatUserNames = userNames.stream().map(ParameterUtils::handleEscapes).collect(Collectors.toList());
        logger.info(" activate userNames: {}", formatUserNames);
        return usersService.batchActivateUser(loginUser, formatUserNames);
    }
}
