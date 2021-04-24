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

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

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
     * @param loginUser login user
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
    public Result<Void> createAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "pluginDefineId") int pluginDefineId,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        logger.info("login user {},create alert plugin instance, instanceName:{} ",
                RegexUtils.escapeNRT(loginUser.getUserName()),
                RegexUtils.escapeNRT(instanceName));
        return alertPluginInstanceService.create(loginUser, pluginDefineId, instanceName, pluginInstanceParams);
    }

    /**
     * updateAlertPluginInstance
     *
     * @param loginUser login user
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
    public Result<Void> updateAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "alertPluginInstanceId") int alertPluginInstanceId,
                                            @RequestParam(value = "instanceName") String instanceName,
                                            @RequestParam(value = "pluginInstanceParams") String pluginInstanceParams) {
        logger.info("login user {},update alert plugin instance id {}", RegexUtils.escapeNRT(loginUser.getUserName()), alertPluginInstanceId);
        return alertPluginInstanceService.update(loginUser, alertPluginInstanceId, instanceName, pluginInstanceParams);
    }

    /**
     * deleteAlertPluginInstance
     *
     * @param loginUser login user
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
    public Result<Void> deleteAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "id") int id) {
        logger.info("login user {},delete alert plugin instance id {}", RegexUtils.escapeNRT(loginUser.getUserName()), id);

        return alertPluginInstanceService.delete(loginUser, id);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @param id alert plugin instance id
     * @return result
     */
    @ApiOperation(value = "get", notes = "GET_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(value = "/get")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<AlertPluginInstance> getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                              @RequestParam(value = "id") int id) {
        logger.info("login user {},get alert plugin instance, id {}", RegexUtils.escapeNRT(loginUser.getUserName()), id);
        return alertPluginInstanceService.get(loginUser, id);
    }

    /**
     * getAlertPluginInstance
     *
     * @param loginUser login user
     * @return result
     */
    @ApiOperation(value = "/queryAll", notes = "QUERY_ALL_ALERT_PLUGIN_INSTANCE_NOTES")
    @PostMapping(value = "/queryAll")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR)
    public Result<List<AlertPluginInstanceVO>> getAlertPluginInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        logger.info("login user {}, query all alert plugin instance", RegexUtils.escapeNRT(loginUser.getUserName()));
        return alertPluginInstanceService.queryAll();
    }

    /**
     * check alert group exist
     *
     * @param loginUser login user
     * @param alertInstanceName alert instance name
     * @return check result code
     */
    @ApiOperation(value = "verifyAlertInstanceName", notes = "VERIFY_ALERT_INSTANCE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupName", value = "GROUP_NAME", required = true, dataType = "String"),
    })
    @GetMapping(value = "/verify-alert-instance-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyGroupName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam(value = "alertInstanceName") String alertInstanceName) {
        logger.info("login user {},verify alert instance name: {}", RegexUtils.escapeNRT(loginUser.getUserName()), RegexUtils.escapeNRT(alertInstanceName));

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
     * @param loginUser login user
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
    public Result<PageListVO<AlertPluginInstanceVO>> listPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                @RequestParam("pageNo") Integer pageNo,
                                                                @RequestParam("pageSize") Integer pageSize) {
        logger.info("login user {}, list paging, pageNo: {}, pageSize: {}", RegexUtils.escapeNRT(loginUser.getUserName()), pageNo, pageSize);
        CheckParamResult result = checkPageParams(pageNo, pageSize);
        if (result.getStatus() != Status.SUCCESS) {
            return error(result);
        }

        return alertPluginInstanceService.queryPluginPage(pageNo, pageSize);
    }

}
