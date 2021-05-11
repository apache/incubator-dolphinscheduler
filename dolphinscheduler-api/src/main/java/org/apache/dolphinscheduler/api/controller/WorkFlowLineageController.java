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

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_LINEAGE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

/**
 * work flow lineage controller
 */
@Api(tags = "WORK_FLOW_LINEAGE_TAG")
@RestController
@RequestMapping("lineages/{projectId}")
public class WorkFlowLineageController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageController.class);

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @GetMapping(value = "/list-name")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation()
    public Result<List<WorkFlowLineage>> queryWorkFlowLineageByName(@ApiParam(name = "projectId", value = "PROJECT_ID", required = true, example = "1") @PathVariable int projectId,
                                                                    @ApiIgnore @RequestParam(value = "searchVal", required = false) String searchVal) {
        try {
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal, projectId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @GetMapping(value = "/list-ids")
    @ResponseStatus(HttpStatus.OK)
    @AccessLogAnnotation()
    public Result<Map<String, Object>> queryWorkFlowLineageByIds(@ApiParam(name = "projectId", value = "PROJECT_ID", required = true, example = "1") @PathVariable int projectId,
                                                                 @ApiIgnore @RequestParam(value = "ids", required = false) String ids) {
        try {
            ids = ParameterUtils.handleEscapes(ids);
            Set<Integer> idsSet = new HashSet<>();
            if (ids != null) {
                String[] idsStr = ids.split(",");
                for (String id : idsStr) {
                    idsSet.add(Integer.parseInt(id));
                }
            }

            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByIds(idsSet, projectId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }
}
