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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_FILE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZE_RESOURCE_TREE;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DOWNLOAD_RESOURCE_FILE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EDIT_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_BY_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_RESOURCES_LIST_PAGING;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_UDF_FUNCTION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_FILE_IS_EMPTY;
import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_NOT_EXIST;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_RESOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_UDF_FUNCTION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_UDF_FUNCTION_NAME_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_RESOURCE_FILE_ON_LINE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VIEW_UDF_FUNCTION_ERROR;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

/**
 * resources controller
 */
@Api(tags = "RESOURCES_TAG")
@RestController
@RequestMapping("resources")
public class ResourcesController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesController.class);

    @Autowired
    private ResourcesService resourceService;
    @Autowired
    private UdfFuncService udfFuncService;

    /**
     *
     * @param loginUser   login user
     * @param type        type
     * @param alias       alias
     * @param description description
     * @param pid         parent id
     * @param currentDir  current directory
     * @return create result code
     */
    @ApiOperation(value = "createDirectory", notes = "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String"),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENTDIR", required = true, dataType = "String")
    })
    @PostMapping(value = "/directory/create")
    @ApiException(CREATE_RESOURCE_ERROR)
    public Result createDirectory(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "type") ResourceType type,
                                  @RequestParam(value = "name") String alias,
                                  @RequestParam(value = "description", required = false) String description,
                                  @RequestParam(value = "pid") int pid,
                                  @RequestParam(value = "currentDir") String currentDir) {
        logger.info("login user {}, create resource, type: {}, resource alias: {}, desc: {}, file: {},{}",
                loginUser.getUserName(), type, alias, description, pid, currentDir);
        return resourceService.createDirectory(loginUser, alias, description, type, pid, currentDir);
    }

    /**
     * create resource
     * @param loginUser
     * @param type
     * @param alias
     * @param description
     * @param file
     * @param pid
     * @param currentDir
     * @return create result code
     */
    @ApiOperation(value = "createResource", notes = "CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENTDIR", required = true, dataType = "String")
    })
    @PostMapping(value = "/create")
    @ApiException(CREATE_RESOURCE_ERROR)
    public Result createResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "type") ResourceType type,
                                 @RequestParam(value = "name") String alias,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "pid") int pid,
                                 @RequestParam(value = "currentDir") String currentDir) {
        logger.info("login user {}, create resource, type: {}, resource alias: {}, desc: {}, file: {},{}",
                loginUser.getUserName(), type, alias, description, file.getName(), file.getOriginalFilename());
        return resourceService.createResource(loginUser, alias, description, type, file, pid, currentDir);
    }

    /**
     * update resource
     *
     * @param loginUser login user
     * @param alias alias
     * @param resourceId resource id
     * @param type resource type
     * @param description description
     * @param file        resource file
     * @return update result code
     */
    @ApiOperation(value = "updateResource", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "name", value = "RESOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String"),
            @ApiImplicitParam(name = "file", value = "RESOURCE_FILE", required = true, dataType = "MultipartFile")
    })
    @PostMapping(value = "/update")
    @ApiException(UPDATE_RESOURCE_ERROR)
    public Result updateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "id") int resourceId,
                                 @RequestParam(value = "type") ResourceType type,
                                 @RequestParam(value = "name") String alias,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "file" ,required = false) MultipartFile file) {
        logger.info("login user {}, update resource, type: {}, resource alias: {}, desc: {}, file: {}",
                loginUser.getUserName(), type, alias, description, file);
        return resourceService.updateResource(loginUser, resourceId, alias, description, type, file);
    }

    /**
     * query resources list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceList", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType")
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    public Result queryResourceList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "type") ResourceType type
    ) {
        logger.info("query resource list, login user:{}, resource type:{}", loginUser.getUserName(), type);
        Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
        return returnDataList(result);
    }

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type resource type
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return resource list page
     */
    @ApiOperation(value = "queryResourceListPaging", notes = "QUERY_RESOURCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "int", example = "10"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_PAGING)
    public Result queryResourceListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @RequestParam(value = "type") ResourceType type,
                                          @RequestParam(value = "id") int id,
                                          @RequestParam("pageNo") Integer pageNo,
                                          @RequestParam(value = "searchVal", required = false) String searchVal,
                                          @RequestParam("pageSize") Integer pageSize
    ) {
        logger.info("query resource list, login user:{}, resource type:{}, search value:{}",
                loginUser.getUserName(), type, searchVal);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }

        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = resourceService.queryResourceListPaging(loginUser, id, type, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }


    /**
     * delete resource
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return delete result code
     */
    @ApiOperation(value = "deleteResource", notes = "DELETE_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_RESOURCE_ERROR)
    public Result deleteResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam(value = "id") int resourceId
    ) throws Exception {
        logger.info("login user {}, delete resource id: {}",
                loginUser.getUserName(), resourceId);
        return resourceService.delete(loginUser, resourceId);
    }


    /**
     * verify resource by alias and type
     *
     * @param loginUser login user
     * @param fullName resource full name
     * @param type resource type
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "verifyResourceName", notes = "VERIFY_RESOURCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR)
    public Result verifyResourceName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                     @RequestParam(value = "fullName") String fullName,
                                     @RequestParam(value = "type") ResourceType type
    ) {
        logger.info("login user {}, verify resource alias: {},resource type: {}",
                loginUser.getUserName(), fullName, type);

        return resourceService.verifyResourceName(fullName, type, loginUser);
    }

    /**
     * query resources jar list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @ApiOperation(value = "queryResourceByProgramType", notes = "QUERY_RESOURCE_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType")
    })
    @GetMapping(value = "/list/jar")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_RESOURCES_LIST_ERROR)
    public Result queryResourceJarList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "type") ResourceType type,
                                       @RequestParam(value = "programType",required = false) ProgramType programType
    ) {
        String programTypeName = programType == null ? "" : programType.name();
        logger.info("query resource list, resource type:{}, program type:{}", type, programTypeName);
        Map<String, Object> result = resourceService.queryResourceByProgramType(loginUser, type,programType);
        return returnDataList(result);
    }

    /**
     * query resource by full name and type
     *
     * @param loginUser login user
     * @param fullName resource full name
     * @param type resource type
     * @param id resource id
     * @return true if the resource name not exists, otherwise return false
     */
    @ApiOperation(value = "queryResource", notes = "QUERY_BY_RESOURCE_NAME")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "fullName", value = "RESOURCE_FULL_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = false, dataType = "Int", example = "10")
    })
    @GetMapping(value = "/queryResource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RESOURCE_NOT_EXIST)
    public Result queryResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "fullName", required = false) String fullName,
                                @RequestParam(value = "id", required = false) Integer id,
                                @RequestParam(value = "type") ResourceType type
    ) {
        logger.info("login user {}, query resource by full name: {} or id: {},resource type: {}",
                loginUser.getUserName(), fullName, id, type);

        return resourceService.queryResource(fullName, id, type);
    }

    /**
     * view resource file online
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    @ApiOperation(value = "viewResource", notes = "VIEW_RESOURCE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "skipLineNum", value = "SKIP_LINE_NUM", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view")
    @ApiException(VIEW_RESOURCE_FILE_ON_LINE_ERROR)
    public Result viewResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @RequestParam(value = "id") int resourceId,
                               @RequestParam(value = "skipLineNum") int skipLineNum,
                               @RequestParam(value = "limit") int limit
    ) {
        logger.info("login user {}, view resource : {}, skipLineNum {} , limit {}",
                loginUser.getUserName(), resourceId, skipLineNum, limit);

        return resourceService.readResource(resourceId, skipLineNum, limit);
    }

    /**
     * create resource file online
     * @param loginUser
     * @param type
     * @param fileName
     * @param fileSuffix
     * @param description
     * @param content
     * @param pid
     * @param currentDir
     * @return create result code
     */
    @ApiOperation(value = "onlineCreateResource", notes = "ONLINE_CREATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "RESOURCE_TYPE", required = true, dataType = "ResourceType"),
            @ApiImplicitParam(name = "fileName", value = "RESOURCE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "suffix", value = "SUFFIX", required = true, dataType = "String"),
            @ApiImplicitParam(name = "description", value = "RESOURCE_DESC", dataType = "String"),
            @ApiImplicitParam(name = "content", value = "CONTENT", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pid", value = "RESOURCE_PID", required = true, dataType = "Int", example = "10"),
            @ApiImplicitParam(name = "currentDir", value = "RESOURCE_CURRENTDIR", required = true, dataType = "String")
    })
    @PostMapping(value = "/online-create")
    @ApiException(CREATE_RESOURCE_FILE_ON_LINE_ERROR)
    public Result onlineCreateResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "type") ResourceType type,
                                       @RequestParam(value = "fileName") String fileName,
                                       @RequestParam(value = "suffix") String fileSuffix,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam(value = "content") String content,
                                       @RequestParam(value = "pid") int pid,
                                       @RequestParam(value = "currentDir") String currentDir
    ) {
        logger.info("login user {}, online create resource! fileName : {}, type : {}, suffix : {},desc : {},content : {}",
                loginUser.getUserName(), fileName, type, fileSuffix, description, content, pid, currentDir);
        if (StringUtils.isEmpty(content)) {
            logger.error("resource file contents are not allowed to be empty");
            return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.onlineCreateResource(loginUser, type, fileName, fileSuffix, description, content, pid, currentDir);
    }

    /**
     * edit resource file online
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @param content content
     * @return update result code
     */
    @ApiOperation(value = "updateResourceContent", notes = "UPDATE_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "content", value = "CONTENT", required = true, dataType = "String")
    })
    @PostMapping(value = "/update-content")
    @ApiException(EDIT_RESOURCE_FILE_ON_LINE_ERROR)
    public Result updateResourceContent(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam(value = "id") int resourceId,
                                        @RequestParam(value = "content") String content
    ) {
        logger.info("login user {}, updateProcessInstance resource : {}",
                loginUser.getUserName(), resourceId);
        if (StringUtils.isEmpty(content)) {
            logger.error("The resource file contents are not allowed to be empty");
            return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
        }
        return resourceService.updateResourceContent(resourceId, content);
    }

    /**
     * download resource file
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return resource content
     */
    @ApiOperation(value = "downloadResource", notes = "DOWNLOAD_RESOURCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/download")
    @ResponseBody
    @ApiException(DOWNLOAD_RESOURCE_FILE_ERROR)
    public ResponseEntity downloadResource(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam(value = "id") int resourceId) throws Exception {
        logger.info("login user {}, download resource : {}",
                loginUser.getUserName(), resourceId);
        Resource file = resourceService.downloadResource(resourceId);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Status.RESOURCE_NOT_EXIST.getMsg());
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }


    /**
     * create udf function
     *
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param description description
     * @param className class name
     * @param resourceId resource id
     * @return create result code
     */
    @ApiOperation(value = "createUdfFunc", notes = "CREATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType = "UdfType"),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "suffix", value = "CLASS_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES", dataType = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", dataType = "String"),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataType = "String"),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @PostMapping(value = "/udf-func/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_UDF_FUNCTION_ERROR)
    public Result createUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "resourceId") int resourceId) {
        logger.info("login user {}, create udf function, type: {},  funcName: {},argTypes: {} ,database: {},desc: {},resourceId: {}",
                loginUser.getUserName(), type, funcName, argTypes, database, description, resourceId);
        return udfFuncService.createUdfFunction(loginUser, funcName, className, argTypes, database, description, type, resourceId);
    }

    /**
     * view udf function
     *
     * @param loginUser login user
     * @param id resource id
     * @return udf function detail
     */
    @ApiOperation(value = "viewUIUdfFunction", notes = "VIEW_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @GetMapping(value = "/udf-func/update-ui")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VIEW_UDF_FUNCTION_ERROR)
    public Result viewUIUdfFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("id") int id) {
        logger.info("login user {}, query udf{}",
                loginUser.getUserName(), id);
        Map<String, Object> map = udfFuncService.queryUdfFuncDetail(id);
        return returnDataList(map);
    }

    /**
     * update udf function
     *
     * @param loginUser login user
     * @param type resource type
     * @param funcName function name
     * @param argTypes argument types
     * @param database data base
     * @param description description
     * @param resourceId resource id
     * @param className class name
     * @param udfFuncId udf function id
     * @return update result code
     */
    @ApiOperation(value = "updateUdfFunc", notes = "UPDATE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "UDF_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType = "UdfType"),
            @ApiImplicitParam(name = "funcName", value = "FUNC_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "className", value = "CLASS_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "argTypes", value = "ARG_TYPES", dataType = "String"),
            @ApiImplicitParam(name = "database", value = "DATABASE_NAME", dataType = "String"),
            @ApiImplicitParam(name = "description", value = "UDF_DESC", dataType = "String"),
            @ApiImplicitParam(name = "resourceId", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")

    })
    @PostMapping(value = "/udf-func/update")
    @ApiException(UPDATE_UDF_FUNCTION_ERROR)
    public Result updateUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "id") int udfFuncId,
                                @RequestParam(value = "type") UdfType type,
                                @RequestParam(value = "funcName") String funcName,
                                @RequestParam(value = "className") String className,
                                @RequestParam(value = "argTypes", required = false) String argTypes,
                                @RequestParam(value = "database", required = false) String database,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam(value = "resourceId") int resourceId) {
        logger.info("login user {}, updateProcessInstance udf function id: {},type: {},  funcName: {},argTypes: {} ,database: {},desc: {},resourceId: {}",
                loginUser.getUserName(), udfFuncId, type, funcName, argTypes, database, description, resourceId);
        Map<String, Object> result = udfFuncService.updateUdfFunc(udfFuncId, funcName, className, argTypes, database, description, type, resourceId);
        return returnDataList(result);
    }

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return udf function list page
     */
    @ApiOperation(value = "queryUdfFuncListPaging", notes = "QUERY_UDF_FUNCTION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/udf-func/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_UDF_FUNCTION_LIST_PAGING_ERROR)
    public Result<Object> queryUdfFuncListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("pageNo") Integer pageNo,
                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                   @RequestParam("pageSize") Integer pageSize
    ) {
        logger.info("query udf functions list, login user:{},search value:{}",
                loginUser.getUserName(), searchVal);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }

        result = udfFuncService.queryUdfFuncListPaging(loginUser, searchVal, pageNo, pageSize);
        return returnDataListPaging(result);
    }

    /**
     * query udf func list by type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    @ApiOperation(value = "queryUdfFuncList", notes = "QUERY_UDF_FUNC_LIST_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "UDF_TYPE", required = true, dataType = "UdfType")
    })
    @GetMapping(value = "/udf-func/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_BY_TYPE_ERROR)
    public Result<Object> queryUdfFuncList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("type") UdfType type) {
        logger.info("query udf func list, type:{}", type);
        Map<String, Object> result = udfFuncService.queryUdfFuncList(loginUser, type.ordinal());
        return returnDataList(result);
    }

    /**
     * verify udf function name can use or not
     *
     * @param loginUser login user
     * @param name name
     * @return true if the name can user, otherwise return false
     */
    @ApiOperation(value = "verifyUdfFuncName", notes = "VERIFY_UDF_FUNCTION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "FUNC_NAME", required = true, dataType = "String")

    })
    @GetMapping(value = "/udf-func/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_UDF_FUNCTION_NAME_ERROR)
    public Result verifyUdfFuncName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam(value = "name") String name
    ) {
        logger.info("login user {}, verify udf function name: {}",
                loginUser.getUserName(), name);

        return udfFuncService.verifyUdfFuncByName(name);
    }

    /**
     * delete udf function
     *
     * @param loginUser login user
     * @param udfFuncId udf function id
     * @return delete result code
     */
    @ApiOperation(value = "deleteUdfFunc", notes = "DELETE_UDF_FUNCTION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "RESOURCE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/udf-func/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_UDF_FUNCTION_ERROR)
    public Result deleteUdfFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "id") int udfFuncId
    ) {
        logger.info("login user {}, delete udf function id: {}", loginUser.getUserName(), udfFuncId);
        return udfFuncService.delete(udfFuncId);
    }

    /**
     * authorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    @ApiOperation(value = "authorizedFile", notes = "AUTHORIZED_FILE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-file")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_FILE_RESOURCE_ERROR)
    public Result authorizedFile(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("userId") Integer userId) {
        logger.info("authorized file resource, user: {}, user id:{}", loginUser.getUserName(), userId);
        Map<String, Object> result = resourceService.authorizedFile(loginUser, userId);
        return returnDataList(result);
    }


    /**
     * unauthorized file resource list
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "authorizeResourceTree", notes = "AUTHORIZE_RESOURCE_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authorize-resource-tree")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZE_RESOURCE_TREE)
    public Result authorizeResourceTree(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        logger.info("all resource file, user:{}, user id:{}", loginUser.getUserName(), userId);
        Map<String, Object> result = resourceService.authorizeResourceTree(loginUser, userId);
        return returnDataList(result);
    }


    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    @ApiOperation(value = "unauthUDFFunc", notes = "UNAUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/unauth-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(UNAUTHORIZED_UDF_FUNCTION_ERROR)
    public Result unauthUDFFunc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam("userId") Integer userId) {
        logger.info("unauthorized udf function, login user:{}, unauthorized user id:{}", loginUser.getUserName(), userId);

        Map<String, Object> result = resourceService.unauthorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }


    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @ApiOperation(value = "authUDFFunc", notes = "AUTHORIZED_UDF_FUNC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/authed-udf-func")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(AUTHORIZED_UDF_FUNCTION_ERROR)
    public Result authorizedUDFFunction(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @RequestParam("userId") Integer userId) {
        logger.info("auth udf function, login user:{}, auth user id:{}", loginUser.getUserName(), userId);
        Map<String, Object> result = resourceService.authorizedUDFFunction(loginUser, userId);
        return returnDataList(result);
    }
}