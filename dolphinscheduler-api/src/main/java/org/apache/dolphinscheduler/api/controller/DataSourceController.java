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

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;

import java.util.Map;

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
import io.swagger.annotations.ApiParam;

/**
 * data source controller
 */
@Api(tags = "DATA_SOURCE_TAG")
@RestController
@RequestMapping("datasources")
public class DataSourceController extends BaseController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * create data source
     *
     * @param dataSourceParam datasource param
     * @return create result code
     */
    @ApiOperation(value = "createDataSource", notes = "CREATE_DATA_SOURCE_NOTES")
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_DATASOURCE_ERROR)
    @AccessLogAnnotation()
    public Result createDataSource(@ApiParam(name = "DATA_SOURCE_PARAM", required = true)
                                   @RequestBody BaseDataSourceParamDTO dataSourceParam) {
        return dataSourceService.createDataSource(AuthUtils.getLoginUser(), dataSourceParam);
    }


    /**
     * updateProcessInstance data source
     *
     * @param dataSourceParam datasource param
     * @return update result code
     */
    @ApiOperation(value = "updateDataSource", notes = "UPDATE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_DATASOURCE_ERROR)
    @AccessLogAnnotation()
    public Result updateDataSource(@RequestBody BaseDataSourceParamDTO dataSourceParam) {
        return dataSourceService.updateDataSource(dataSourceParam.getId(), AuthUtils.getLoginUser(), dataSourceParam);
    }

    /**
     * query data source detail
     *
     * @param id datasource id
     * @return data source detail
     */
    @ApiOperation(value = "queryDataSource", notes = "QUERY_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @PostMapping(value = "/update-ui")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation()
    public Result queryDataSource(@RequestParam("id") int id) {

        Map<String, Object> result = dataSourceService.queryDataSource(id);
        return returnDataList(result);
    }

    /**
     * query datasouce by type
     *
     * @param type data source type
     * @return data source list page
     */
    @ApiOperation(value = "queryDataSourceList", notes = "QUERY_DATA_SOURCE_LIST_BY_TYPE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType")
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation()
    public Result queryDataSourceList(@RequestParam("type") DbType type) {
        Map<String, Object> result = dataSourceService.queryDataSourceList(AuthUtils.getLoginUser(), type.ordinal());
        return returnDataList(result);
    }

    /**
     * query datasource with paging
     *
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
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
    @AccessLogAnnotation()
    public Result queryDataSourceListPaging(@RequestParam(value = "searchVal", required = false) String searchVal,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = dataSourceService.queryDataSourceListPaging(AuthUtils.getLoginUser(), searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * connect datasource
     *
     * @param dataSourceParam datasource param
     * @return connect result code
     */
    @ApiOperation(value = "connectDataSource", notes = "CONNECT_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String"),
    })
    @PostMapping(value = "/connect")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECT_DATASOURCE_FAILURE)
    @AccessLogAnnotation()
    public Result connectDataSource(@RequestBody BaseDataSourceParamDTO dataSourceParam) {
        DatasourceUtil.checkDatasourceParam(dataSourceParam);
        ConnectionParam connectionParams = DatasourceUtil.buildConnectionParams(dataSourceParam);
        return dataSourceService.checkConnection(dataSourceParam.getType(), connectionParams);
    }

    /**
     * connection test
     *
     * @param id data source id
     * @return connect result code
     */
    @ApiOperation(value = "connectionTest", notes = "CONNECT_DATA_SOURCE_TEST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/connect-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECTION_TEST_FAILURE)
    @AccessLogAnnotation()
    public Result connectionTest(@RequestParam("id") int id) {
        return dataSourceService.connectionTest(id);
    }

    /**
     * delete datasource by id
     *
     * @param id datasource id
     * @return delete result
     */
    @ApiOperation(value = "delete", notes = "DELETE_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DATA_SOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_DATA_SOURCE_FAILURE)
    @AccessLogAnnotation()
    public Result delete(@RequestParam("id") int id) {
        return dataSourceService.delete(AuthUtils.getLoginUser(), id);
    }

    /**
     * verify datasource name
     *
     * @param name data source name
     * @return true if data source name not exists.otherwise return false
     */
    @ApiOperation(value = "verifyDataSourceName", notes = "VERIFY_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "DATA_SOURCE_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_DATASOURCE_NAME_FAILURE)
    @AccessLogAnnotation()
    public Result verifyDataSourceName(@RequestParam(value = "name") String name
    ) {
        return dataSourceService.verifyDataSourceName(name);
    }


    /**
     * unauthorized datasource
     *
     * @param userId user id
     * @return unauthed data source result code
     */
    @ApiOperation(value = "unauthDatasource", notes = "UNAUTHORIZED_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/unauth-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_DATASOURCE)
    @AccessLogAnnotation()
    public Result unauthDatasource(@RequestParam("userId") Integer userId) {

        Map<String, Object> result = dataSourceService.unauthDatasource(AuthUtils.getLoginUser(), userId);
        return returnDataList(result);
    }


    /**
     * authorized datasource
     *
     * @param userId user id
     * @return authorized result code
     */
    @ApiOperation(value = "authedDatasource", notes = "AUTHORIZED_DATA_SOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_DATA_SOURCE)
    @AccessLogAnnotation()
    public Result authedDatasource(@RequestParam("userId") Integer userId) {

        Map<String, Object> result = dataSourceService.authedDatasource(AuthUtils.getLoginUser(), userId);
        return returnDataList(result);
    }

    /**
     * get user info
     *
     * @return user info data
     */
    @ApiOperation(value = "getKerberosStartupState", notes = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/kerberos-startup-state")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(KERBEROS_STARTUP_STATE)
    @AccessLogAnnotation()
    public Result getKerberosStartupState() {
        // if upload resource is HDFS and kerberos startup is true , else false
        return success(Status.SUCCESS.getMsg(), CommonUtils.getKerberosStartupState());
    }
}
