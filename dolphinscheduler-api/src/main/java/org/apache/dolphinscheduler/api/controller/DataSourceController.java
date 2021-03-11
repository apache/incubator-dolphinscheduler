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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_DATA_SOURCE;
import static org.apache.dolphinscheduler.api.enums.Status.CONNECTION_TEST_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.CONNECT_DATASOURCE_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_DATA_SOURCE_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.KERBEROS_STARTUP_STATE;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_DATASOURCE;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_DATASOURCE_NAME_FAILURE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
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
 * data source controller
 */
@Api(tags = "DATA_SOURCE_TAG")
@RestController
@RequestMapping("datasources")
public class DataSourceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * create data source
     *
     * @param loginUser login user
     * @param name      data source name
     * @param note      data source description
     * @param type      data source type
     * @param host      host
     * @param port      port
     * @param database  data base
     * @param principal principal
     * @param userName  user name
     * @param password  password
     * @param other     other arguments
     * @return create result code
     */
    @ApiOperation(value = "createDataSource", notes = "CREATE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "note", value = "DATA_SOURCE_NOTE", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType"),
            @ApiImplicitParam(name = "host", value = "DATA_SOURCE_HOST", required = true, dataType = "String"),
            @ApiImplicitParam(name = "port", value = "DATA_SOURCE_PORT", required = true, dataType = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "principal", value = "DATA_SOURCE_PRINCIPAL", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "PASSWORD", dataType = "String"),
            @ApiImplicitParam(name = "connectType", value = "CONNECT_TYPE", dataType = "DbConnectType"),
            @ApiImplicitParam(name = "other", value = "DATA_SOURCE_OTHER", dataType = "String"),
            @ApiImplicitParam(name = "javaSecurityKrb5Conf", value = "DATA_SOURCE_KERBEROS_KRB5_CONF", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabUsername", value = "DATA_SOURCE_KERBEROS_KEYTAB_USERNAME", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabPath", value = "DATA_SOURCE_KERBEROS_KEYTAB_PATH", dataType = "String")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_DATASOURCE_ERROR)
    public Result createDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("name") String name,
                                   @RequestParam(value = "note", required = false) String note,
                                   @RequestParam(value = "type") DbType type,
                                   @RequestParam(value = "host") String host,
                                   @RequestParam(value = "port") String port,
                                   @RequestParam(value = "database") String database,
                                   @RequestParam(value = "principal") String principal,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "connectType") DbConnectType connectType,
                                   @RequestParam(value = "other") String other,
                                   @RequestParam(value = "javaSecurityKrb5Conf", required = false) String javaSecurityKrb5Conf,
                                   @RequestParam(value = "loginUserKeytabUsername", required = false) String loginUserKeytabUsername,
                                   @RequestParam(value = "loginUserKeytabPath", required = false) String loginUserKeytabPath) {
        logger.info("login user {} create datasource name: {}, note: {}, type: {}, host: {}, port: {}, database : {}, principal: {}, userName : {}, connectType: {}, other: {}",
                loginUser.getUserName(), name, note, type, host, port, database, principal, userName, connectType, other);
        String parameter = dataSourceService.buildParameter(type, host, port, database, principal, userName, password, connectType, other,
                javaSecurityKrb5Conf, loginUserKeytabUsername, loginUserKeytabPath);
        return dataSourceService.createDataSource(loginUser, name, note, type, parameter);
    }


    /**
     * updateProcessInstance data source
     *
     * @param loginUser login user
     * @param name      data source name
     * @param note      description
     * @param type      data source type
     * @param other     other arguments
     * @param id        data source di
     * @param host      host
     * @param port      port
     * @param database  database
     * @param principal principal
     * @param userName  user name
     * @param password  password
     * @return update result code
     */
    @ApiOperation(value = "updateDataSource", notes = "UPDATE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "note", value = "DATA_SOURCE_NOTE", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType"),
            @ApiImplicitParam(name = "host", value = "DATA_SOURCE_HOST", required = true, dataType = "String"),
            @ApiImplicitParam(name = "port", value = "DATA_SOURCE_PORT", required = true, dataType = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "principal", value = "DATA_SOURCE_PRINCIPAL", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "PASSWORD", dataType = "String"),
            @ApiImplicitParam(name = "connectType", value = "CONNECT_TYPE", dataType = "DbConnectType"),
            @ApiImplicitParam(name = "other", value = "DATA_SOURCE_OTHER", dataType = "String"),
            @ApiImplicitParam(name = "javaSecurityKrb5Conf", value = "DATA_SOURCE_KERBEROS_KRB5_CONF", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabUsername", value = "DATA_SOURCE_KERBEROS_KEYTAB_USERNAME", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabPath", value = "DATA_SOURCE_KERBEROS_KEYTAB_PATH", dataType = "String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_DATASOURCE_ERROR)
    public Result updateDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("id") int id,
                                   @RequestParam("name") String name,
                                   @RequestParam(value = "note", required = false) String note,
                                   @RequestParam(value = "type") DbType type,
                                   @RequestParam(value = "host") String host,
                                   @RequestParam(value = "port") String port,
                                   @RequestParam(value = "database") String database,
                                   @RequestParam(value = "principal") String principal,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "connectType") DbConnectType connectType,
                                   @RequestParam(value = "other") String other,
                                   @RequestParam(value = "javaSecurityKrb5Conf", required = false) String javaSecurityKrb5Conf,
                                   @RequestParam(value = "loginUserKeytabUsername", required = false) String loginUserKeytabUsername,
                                   @RequestParam(value = "loginUserKeytabPath", required = false) String loginUserKeytabPath) {
        logger.info("login user {} updateProcessInstance datasource name: {}, note: {}, type: {}, connectType: {}, other: {}",
                loginUser.getUserName(), name, note, type, connectType, other);
        String parameter = dataSourceService.buildParameter(type, host, port, database, principal, userName, password, connectType, other,
                javaSecurityKrb5Conf, loginUserKeytabUsername, loginUserKeytabPath);
        return dataSourceService.updateDataSource(id, loginUser, name, note, type, parameter);
    }

    /**
     * query data source detail
     *
     * @param loginUser login user
     * @param id        datasource id
     * @return data source detail
     */
    @ApiOperation(value = "queryDataSource", notes = "QUERY_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @PostMapping(value = "/update-ui")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    public Result queryDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("id") int id) {
        logger.info("login user {}, query datasource: {}",
                loginUser.getUserName(), id);
        Map<String, Object> result = dataSourceService.queryDataSource(id);
        return returnDataList(result);
    }

    /**
     * query datasource by type
     *
     * @param loginUser login user
     * @param type      data source type
     * @return data source list page
     */
    @ApiOperation(value = "queryDataSourceList", notes = "QUERY_DATA_SOURCE_LIST_BY_TYPE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType")
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    public Result queryDataSourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam("type") DbType type) {
        Map<String, Object> result = dataSourceService.queryDataSourceList(loginUser, type.ordinal());
        return returnDataList(result);
    }

    /**
     * query datasource with paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return data source list page
     */
    @ApiOperation(value = "queryDataSourceListPaging", notes = "QUERY_DATA_SOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    public Result queryDataSourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "searchVal", required = false) String searchVal,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * connect datasource
     *
     * @param loginUser login user
     * @param name      data source name
     * @param note      data source description
     * @param type      data source type
     * @param other     other parameters
     * @param host      host
     * @param port      port
     * @param database  data base
     * @param principal principal
     * @param userName  user name
     * @param password  password
     * @return connect result code
     */
    @ApiOperation(value = "connectDataSource", notes = "CONNECT_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "note", value = "DATA_SOURCE_NOTE", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType"),
            @ApiImplicitParam(name = "host", value = "DATA_SOURCE_HOST", required = true, dataType = "String"),
            @ApiImplicitParam(name = "port", value = "DATA_SOURCE_PORT", required = true, dataType = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "principal", value = "DATA_SOURCE_PRINCIPAL", dataType = "String"),
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "PASSWORD", dataType = "String"),
            @ApiImplicitParam(name = "connectType", value = "CONNECT_TYPE", dataType = "DbConnectType"),
            @ApiImplicitParam(name = "other", value = "DATA_SOURCE_OTHER", dataType = "String"),
            @ApiImplicitParam(name = "javaSecurityKrb5Conf", value = "DATA_SOURCE_KERBEROS_KRB5_CONF", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabUsername", value = "DATA_SOURCE_KERBEROS_KEYTAB_USERNAME", dataType = "String"),
            @ApiImplicitParam(name = "loginUserKeytabPath", value = "DATA_SOURCE_KERBEROS_KEYTAB_PATH", dataType = "String")
    })
    @PostMapping(value = "/connect")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECT_DATASOURCE_FAILURE)
    public Result connectDataSource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("name") String name,
                                    @RequestParam(value = "note", required = false) String note,
                                    @RequestParam(value = "type") DbType type,
                                    @RequestParam(value = "host") String host,
                                    @RequestParam(value = "port") String port,
                                    @RequestParam(value = "database") String database,
                                    @RequestParam(value = "principal") String principal,
                                    @RequestParam(value = "userName") String userName,
                                    @RequestParam(value = "password") String password,
                                    @RequestParam(value = "connectType") DbConnectType connectType,
                                    @RequestParam(value = "other") String other,
                                    @RequestParam(value = "javaSecurityKrb5Conf", required = false) String javaSecurityKrb5Conf,
                                    @RequestParam(value = "loginUserKeytabUsername", required = false) String loginUserKeytabUsername,
                                    @RequestParam(value = "loginUserKeytabPath", required = false) String loginUserKeytabPath) {
        logger.info("login user {}, connect datasource: {}, note: {}, type: {}, connectType: {}, other: {}",
                loginUser.getUserName(), name, note, type, connectType, other);
        String parameter = dataSourceService.buildParameter(type, host, port, database, principal, userName, password, connectType, other,
                javaSecurityKrb5Conf, loginUserKeytabUsername, loginUserKeytabPath);
        return dataSourceService.checkConnection(type, parameter);
    }

    /**
     * connection test
     *
     * @param loginUser login user
     * @param id        data source id
     * @return connect result code
     */
    @ApiOperation(value = "connectionTest", notes = "CONNECT_DATA_SOURCE_TEST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/connect-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECTION_TEST_FAILURE)
    public Result connectionTest(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("id") int id) {
        logger.info("connection test, login user:{}, id:{}", loginUser.getUserName(), id);
        return dataSourceService.connectionTest(id);
    }

    /**
     * delete datasource by id
     *
     * @param loginUser login user
     * @param id        datasource id
     * @return delete result
     */
    @ApiOperation(value = "delete", notes = "DELETE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_DATA_SOURCE_FAILURE)
    public Result delete(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                         @RequestParam("id") int id) {
        logger.info("delete datasource,login user:{}, id:{}", loginUser.getUserName(), id);
        return dataSourceService.delete(loginUser, id);
    }

    /**
     * verify datasource name
     *
     * @param loginUser login user
     * @param name      data source name
     * @return true if data source name not exists.otherwise return false
     */
    @ApiOperation(value = "verifyDataSourceName", notes = "VERIFY_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_DATASOURCE_NAME_FAILURE)
    public Result verifyDataSourceName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "name") String name
    ) {
        logger.info("login user {}, verify datasource name: {}",
                loginUser.getUserName(), name);

        return dataSourceService.verifyDataSourceName(name);
    }


    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthed data source result code
     */
    @ApiOperation(value = "unauthDatasource", notes = "UNAUTHORIZED_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/unauth-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_DATASOURCE)
    public Result unauthDatasource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("userId") Integer userId) {
        logger.info("unauthorized datasource, login user:{}, unauthorized userId:{}",
                loginUser.getUserName(), userId);
        Map<String, Object> result = dataSourceService.unauthDatasource(loginUser, userId);
        return returnDataList(result);
    }


    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    @ApiOperation(value = "authedDatasource", notes = "AUTHORIZED_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_DATA_SOURCE)
    public Result authedDatasource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("userId") Integer userId) {
        logger.info("authorized data source, login user:{}, authorized useId:{}",
                loginUser.getUserName(), userId);
        Map<String, Object> result = dataSourceService.authedDatasource(loginUser, userId);
        return returnDataList(result);
    }

    /**
     * get user info
     *
     * @param loginUser login user
     * @return user info data
     */
    @ApiOperation(value = "getKerberosStartupState", notes = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/kerberos-startup-state")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(KERBEROS_STARTUP_STATE)
    public Result getKerberosStartupState(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {}", loginUser.getUserName());
        // if upload resource is HDFS and kerberos startup is true , else false
        return success(Status.SUCCESS.getMsg(), CommonUtils.getKerberosStartupState());
    }
}
