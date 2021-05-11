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

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_ALERT_PLUGIN_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * alert plugin instance controller
 */
@Api(tags = "ALERT_PLUGIN_INSTANCE_TAG")
@RestController
@RequestMapping("alert-plugin-instance")
public class AlertPluginInstanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AlertPluginInstanceController.class);

    @Autowired
    private AlertPluginInstanceService alertPluginInstanceService;


    /**
     * create alert plugin instance
     *
     * @param pluginDefineId alert plugin define id
     * @param instanceName instance name
     * @param pluginInstanceParams instance params
     * @return result
     */
    @ApiOperation(value = "createAlertPluginInstance", notes = "CREATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pluginDefineId", value = "ALERT_PLUGIN_DEFINE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "instanceName", value = "ALERT_PLUGIN_INSTANCE_NAME", required = true, dataType = "String", example = "DING TALK"),
            @ApiImplicitParam(name = "pluginInstanceParams", value = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, dataType = "String", example = "ALERT_PLUGIN_INSTANCE_PARAMS")
    })
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result createAlertPluginInstance(@RequestParam(value = "pluginDefineId") int pluginDefineId,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        Map<String, Object> result = alertPluginInstanceService.create(AuthUtils.getLoginUser(), pluginDefineId, instanceName, pluginInstanceParams);
        return returnDataList(result);
    }

    /**
     * updateAlertPluginInstance
     *
     * @param alertPluginInstanceId alert plugin instance id
     * @param instanceName instance name
     * @param pluginInstanceParams instance params
     * @return result
     */
    @ApiOperation(value = "update", notes = "UPDATE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "alertPluginInstanceId", value = "ALERT_PLUGIN_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "instanceName", value = "ALERT_PLUGIN_INSTANCE_NAME", required = true, dataType = "String", example = "DING TALK"),
            @ApiImplicitParam(name = "pluginInstanceParams", value = "ALERT_PLUGIN_INSTANCE_PARAMS", required = true, dataType = "String", example = "ALERT_PLUGIN_INSTANCE_PARAMS")
    })
    @GetMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result updateAlertPluginInstance(@RequestParam(value = "alertPluginInstanceId") int alertPluginInstanceId,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        Map<String, Object> result = alertPluginInstanceService.update(AuthUtils.getLoginUser(), alertPluginInstanceId, instanceName, pluginInstanceParams);
        return returnDataList(result);
    }

    /**
     * deleteAlertPluginInstance
     *
     * @param id id
     * @return result
     */
    @ApiOperation(value = "delete", notes = "DELETE_ALERT_PLUGIN_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ALERT_PLUGIN_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result deleteAlertPluginInstance(@RequestParam(value = "id") int id) {

        Map<String, Object> result = alertPluginInstanceService.delete(AuthUtils.getLoginUser(), id);
        return returnDataList(result);
    }

    /**
     * getAlertPluginInstance
     *
     * @param id alert plugin instance id
     * @return result
     */
    @ApiOperation(value = "get", notes = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(value = "/get")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result getAlertPluginInstance(@RequestParam(value = "id") int id) {
        Map<String, Object> result = alertPluginInstanceService.get(AuthUtils.getLoginUser(), id);
        return returnDataList(result);
    }

    /**
     * getAlertPluginInstance
     *
     * @return result
     */
    @ApiOperation(value = "/queryAll", notes = "QUERY_ALL_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(value = "/queryAll")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result getAlertPluginInstance() {
        Map<String, Object> result = alertPluginInstanceService.queryAll();
        return returnDataList(result);
    }

    /**
     * check alert group exist
     *
     * @param alertInstanceName alert instance name
     * @return check result code
     */
    @ApiOperation(value = "verifyAlertInstanceName", notes = "VERIFY_ALERT_INSTANCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
    })
    @GetMapping(value = "/verify-alert-instance-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation()
    public Result verifyGroupName(@RequestParam(value = "alertInstanceName") String alertInstanceName) {

        boolean exist = alertPluginInstanceService.checkExistPluginInstanceName(alertInstanceName);
        Result result = new Result();
        if (exist) {
            logger.error("alert plugin instance {} has exist, can't create again.", alertInstanceName);
            result.setCode(Status.PLUGIN_INSTANCE_ALREADY_EXIT.getCode());
            result.setMsg(Status.PLUGIN_INSTANCE_ALREADY_EXIT.getMsg());
        } else {
            result.setCode(Status.SUCCESS.getCode());
            result.setMsg(Status.SUCCESS.getMsg());
        }
        return result;
    }

    /**
     * paging query alert plugin instance group list
     *
     * @param pageNo page number
     * @param pageSize page size
     * @return alert plugin instance list page
     */
    @ApiOperation(value = "queryAlertPluginInstanceListPaging", notes = "QUERY_ALERT_PLUGIN_INSTANCE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR)
    @AccessLogAnnotation()
    public Result listPaging(@RequestParam("pageNo") Integer pageNo,
                             @RequestParam("pageSize") Integer pageSize) {
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }

        result = alertPluginInstanceService.queryPluginPage(pageNo, pageSize);
        return returnDataListPaging(result);
    }

}
