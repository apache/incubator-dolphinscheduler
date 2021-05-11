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

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_COPY_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_MOVE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_PROCESS_DEFINITION;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_PROCESS_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionVersionService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * process definition controller
 */
@Api(tags = "PROCESS_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectName}/process")
public class ProcessDefinitionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessDefinitionVersionService processDefinitionVersionService;

    /**
     * create process definition
     *
     * @param projectName project name
     * @param name process definition name
     * @param json process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     */
    @ApiOperation(value = "save", notes = "CREATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String"),
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_DEFINITION)
    @AccessLogAnnotation()
    public Result createProcessDefinition(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "processDefinitionJson", required = true) String json,
                                          @RequestParam(value = "locations", required = true) String locations,
                                          @RequestParam(value = "connects", required = true) String connects,
                                          @RequestParam(value = "description", required = false) String description) throws JsonProcessingException {

        Map<String, Object> result = processDefinitionService.createProcessDefinition(AuthUtils.getLoginUser(), projectName, name, json,
                description, locations, connects);
        return returnDataList(result);
    }

    /**
     * copy  process definition
     *
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param targetProjectId target project id
     * @return copy result code
     */
    @ApiOperation(value = "copyProcessDefinition", notes = "COPY_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", required = true, dataType = "String", example = "3,4"),
            @ApiImplicitParam(name = "targetProjectId", value = "TARGET_PROJECT_ID", required = true, dataType = "Int", example = "10")
    })
    @PostMapping(value = "/copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_COPY_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result copyProcessDefinition(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "processDefinitionIds", required = true) String processDefinitionIds,
                                        @RequestParam(value = "targetProjectId", required = true) int targetProjectId) {

        return returnDataList(
                processDefinitionService.batchCopyProcessDefinition(AuthUtils.getLoginUser(), projectName, processDefinitionIds, targetProjectId));
    }

    /**
     * move process definition
     *
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param targetProjectId target project id
     * @return move result code
     */
    @ApiOperation(value = "moveProcessDefinition", notes = "MOVE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", required = true, dataType = "String", example = "3,4"),
            @ApiImplicitParam(name = "targetProjectId", value = "TARGET_PROJECT_ID", required = true, dataType = "Int", example = "10")
    })
    @PostMapping(value = "/move")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_MOVE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result moveProcessDefinition(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "processDefinitionIds", required = true) String processDefinitionIds,
                                        @RequestParam(value = "targetProjectId", required = true) int targetProjectId) {
        return returnDataList(
                processDefinitionService.batchMoveProcessDefinition(AuthUtils.getLoginUser(), projectName, processDefinitionIds, targetProjectId));
    }

    /**
     * verify process definition name unique
     *
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCESS_DEFINITION_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR)
    @AccessLogAnnotation()
    public Result verifyProcessDefinitionName(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                              @RequestParam(value = "name", required = true) String name) {

        Map<String, Object> result = processDefinitionService.verifyProcessDefinitionName(AuthUtils.getLoginUser(), projectName, name);
        return returnDataList(result);
    }

    /**
     * update process definition
     *
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param description description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */

    @ApiOperation(value = "updateProcessDefinition", notes = "UPDATE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "id", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionJson", value = "PROCESS_DEFINITION_JSON", required = true, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_DEFINITION_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_DEFINITION_CONNECTS", required = true, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_DEFINITION_DESC", required = false, type = "String"),
            @ApiImplicitParam(name = "releaseState", value = "RELEASE_PROCESS_DEFINITION_NOTES", required = false, dataType = "ReleaseState")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result updateProcessDefinition(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "name", required = true) String name,
                                          @RequestParam(value = "id", required = true) int id,
                                          @RequestParam(value = "processDefinitionJson", required = true) String processDefinitionJson,
                                          @RequestParam(value = "locations", required = false) String locations,
                                          @RequestParam(value = "connects", required = false) String connects,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "releaseState", required = false, defaultValue = "OFFLINE") ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.updateProcessDefinition(AuthUtils.getLoginUser(), projectName, id, name,
                processDefinitionJson, description, locations, connects);
        //  If the update fails, the result will be returned directly
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataList(result);
        }

        //  Judge whether to go online after editing,0 means offline, 1 means online
        if (releaseState == ReleaseState.ONLINE) {
            result = processDefinitionService.releaseProcessDefinition(AuthUtils.getLoginUser(), projectName, id, releaseState);
        }
        return returnDataList(result);
    }

    /**
     * query process definition version paging list info
     *
     * @param projectName the process definition project name
     * @param pageNo the process definition version list current page number
     * @param pageSize the process definition version list page size
     * @param processDefinitionId the process definition id
     * @return the process definition version list
     */
    @ApiOperation(value = "queryProcessDefinitionVersions", notes = "QUERY_PROCESS_DEFINITION_VERSIONS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_VERSIONS_ERROR)
    @AccessLogAnnotation()
    public Result queryProcessDefinitionVersions(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "pageNo") int pageNo,
                                                 @RequestParam(value = "pageSize") int pageSize,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId) {

        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        result = processDefinitionVersionService.queryProcessDefinitionVersions(AuthUtils.getLoginUser()
                , projectName, pageNo, pageSize, processDefinitionId);
        return returnDataList(result);
    }

    /**
     * switch certain process definition version
     *
     * @param projectName the process definition project name
     * @param processDefinitionId the process definition id
     * @param version the version user want to switch
     * @return switch version result code
     */
    @ApiOperation(value = "switchProcessDefinitionVersion", notes = "SWITCH_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/switch")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation()
    public Result switchProcessDefinitionVersion(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") long version) {
        Map<String, Object> result = processDefinitionService.switchProcessDefinitionVersion(AuthUtils.getLoginUser(), projectName
                , processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * delete the certain process definition version by version and process definition id
     *
     * @param projectName the process definition project name
     * @param processDefinitionId process definition id
     * @param version the process definition version user want to delete
     * @return delete version result code
     */
    @ApiOperation(value = "deleteProcessDefinitionVersion", notes = "DELETE_PROCESS_DEFINITION_VERSION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "version", value = "VERSION", required = true, dataType = "Long", example = "100")
    })
    @GetMapping(value = "/version/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINITION_VERSION_ERROR)
    @AccessLogAnnotation()
    public Result deleteProcessDefinitionVersion(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                                 @RequestParam(value = "version") long version) {
        Map<String, Object> result = processDefinitionVersionService.deleteByProcessDefinitionIdAndVersion(AuthUtils.getLoginUser(), projectName, processDefinitionId, version);
        return returnDataList(result);
    }

    /**
     * release process definition
     *
     * @param projectName project name
     * @param processId process definition id
     * @param releaseState release state
     * @return release result code
     */
    @ApiOperation(value = "releaseProcessDefinition", notes = "RELEASE_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_DEFINITION_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_DEFINITION_CONNECTS", required = true, dataType = "ReleaseState"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result releaseProcessDefinition(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam(value = "processId", required = true) int processId,
                                           @RequestParam(value = "releaseState", required = true) ReleaseState releaseState) {

        Map<String, Object> result = processDefinitionService.releaseProcessDefinition(AuthUtils.getLoginUser(), projectName, processId, releaseState);
        return returnDataList(result);
    }

    /**
     * query detail of process definition by id
     *
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionById", notes = "QUERY_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result queryProcessDefinitionById(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                             @RequestParam("processId") Integer processId
    ) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionById(AuthUtils.getLoginUser(), projectName, processId);
        return returnDataList(result);
    }

    /**
     * query detail of process definition by name
     *
     * @param projectName project name
     * @param processDefinitionName process definition name
     * @return process definition detail
     */
    @ApiOperation(value = "queryProcessDefinitionByName", notes = "QUERY_PROCESS_DEFINITION_BY_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionName", value = "PROCESS_DEFINITION_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/select-by-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation()
    public Result<ProcessDefinition> queryProcessDefinitionByName(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                  @RequestParam("processDefinitionName") String processDefinitionName) {

        Map<String, Object> result = processDefinitionService.queryProcessDefinitionByName(AuthUtils.getLoginUser(), projectName, processDefinitionName);
        return returnDataList(result);
    }

    /**
     * query Process definition list
     *
     * @param projectName project name
     * @return process definition list
     */
    @ApiOperation(value = "queryProcessDefinitionList", notes = "QUERY_PROCESS_DEFINITION_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation()
    public Result queryProcessDefinitionList(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName
    ) {
        Map<String, Object> result = processDefinitionService.queryProcessDefinitionList(AuthUtils.getLoginUser(), projectName);
        return returnDataList(result);
    }

    /**
     * query process definition list paging
     *
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    @ApiOperation(value = "queryProcessDefinitionListPaging", notes = "QUERY_PROCESS_DEFINITION_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR)
    @AccessLogAnnotation()
    public Result queryProcessDefinitionListPaging(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                   @RequestParam("pageNo") Integer pageNo,
                                                   @RequestParam(value = "searchVal", required = false) String searchVal,
                                                   @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                   @RequestParam("pageSize") Integer pageSize) {

        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = processDefinitionService.queryProcessDefinitionListPaging(AuthUtils.getLoginUser(), projectName, searchVal, pageNo, pageSize, userId);
        return returnDataListPaging(result);
    }

    /**
     * encapsulation tree view structure
     *
     * @param projectName project name
     * @param id process definition id
     * @param limit limit
     * @return tree view json data
     */
    @ApiOperation(value = "viewTree", notes = "VIEW_TREE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "limit", value = "LIMIT", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/view-tree")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR)
    @AccessLogAnnotation()
    public Result viewTree(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                           @RequestParam("processId") Integer id,
                           @RequestParam("limit") Integer limit) throws Exception {

        Map<String, Object> result = processDefinitionService.viewTree(id, limit);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition id
     *
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return task list
     */
    @ApiOperation(value = "getNodeListByDefinitionId", notes = "GET_NODE_LIST_BY_DEFINITION_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "gen-task-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    @AccessLogAnnotation()
    public Result getNodeListByDefinitionId(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                            @RequestParam("processDefinitionId") Integer processDefinitionId) throws Exception {

        Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionId(processDefinitionId);
        return returnDataList(result);
    }

    /**
     * get tasks list by process definition id
     *
     * @param projectName project name
     * @param processDefinitionIdList process definition id list
     * @return node list data
     */
    @ApiOperation(value = "getNodeListByDefinitionIdList", notes = "GET_NODE_LIST_BY_DEFINITION_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIdList", value = "PROCESS_DEFINITION_ID_LIST", required = true, type = "String")
    })
    @GetMapping(value = "get-task-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR)
    @AccessLogAnnotation()
    public Result getNodeListByDefinitionIdList(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                @RequestParam("processDefinitionIdList") String processDefinitionIdList) {

        Map<String, Object> result = processDefinitionService.getTaskNodeListByDefinitionIdList(processDefinitionIdList);
        return returnDataList(result);
    }

    /**
     * delete process definition by id
     *
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessDefinitionById", notes = "DELETE_PROCESS_DEFINITION_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_ID_ERROR)
    @AccessLogAnnotation()
    public Result deleteProcessDefinitionById(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                              @RequestParam("processDefinitionId") Integer processDefinitionId
    ) {

        Map<String, Object> result = processDefinitionService.deleteProcessDefinitionById(AuthUtils.getLoginUser(), projectName, processDefinitionId);
        return returnDataList(result);
    }

    /**
     * batch delete process definition by ids
     *
     * @param projectName project name
     * @param processDefinitionIds process definition id list
     * @return delete result code
     */
    @ApiOperation(value = "batchDeleteProcessDefinitionByIds", notes = "BATCH_DELETE_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_IDS", type = "String")
    })
    @GetMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR)
    @AccessLogAnnotation()
    public Result batchDeleteProcessDefinitionByIds(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                    @RequestParam("processDefinitionIds") String processDefinitionIds
    ) {

        Map<String, Object> result = new HashMap<>();
        List<String> deleteFailedIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(processDefinitionIds)) {
            String[] processDefinitionIdArray = processDefinitionIds.split(",");

            for (String strProcessDefinitionId : processDefinitionIdArray) {
                int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
                try {
                    Map<String, Object> deleteResult = processDefinitionService.deleteProcessDefinitionById(AuthUtils.getLoginUser(), projectName, processDefinitionId);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedIdList.add(strProcessDefinitionId);
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedIdList.add(strProcessDefinitionId);
                }
            }
        }

        if (!deleteFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return returnDataList(result);
    }

    /**
     * batch export process definition by ids
     *
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param response response
     */

    @ApiOperation(value = "batchExportProcessDefinitionByIds", notes = "BATCH_EXPORT_PROCESS_DEFINITION_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionIds", value = "PROCESS_DEFINITION_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/export")
    @ResponseBody
    @AccessLogAnnotation(ignoreRequestArgs = {"response"})
    public void batchExportProcessDefinitionByIds(@ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                  @RequestParam("processDefinitionIds") String processDefinitionIds,
                                                  HttpServletResponse response) {
        try {

            processDefinitionService.batchExportProcessDefinitionByIds(AuthUtils.getLoginUser(), projectName, processDefinitionIds, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR.getMsg(), e);
        }
    }

    /**
     * query process definition all by project id
     *
     * @param projectId project id
     * @return process definition list
     */
    @ApiOperation(value = "queryProcessDefinitionAllByProjectId", notes = "QUERY_PROCESS_DEFINITION_All_BY_PROJECT_ID_NOTES")
    @GetMapping(value = "/queryProcessDefinitionAllByProjectId")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_DEFINITION_LIST)
    @AccessLogAnnotation()
    public Result queryProcessDefinitionAllByProjectId(@RequestParam("projectId") Integer projectId) {

        Map<String, Object> result = processDefinitionService.queryProcessDefinitionAllByProjectId(projectId);
        return returnDataList(result);
    }

}
