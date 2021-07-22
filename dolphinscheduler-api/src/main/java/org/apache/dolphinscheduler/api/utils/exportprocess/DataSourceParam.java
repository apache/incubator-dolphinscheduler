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

package org.apache.dolphinscheduler.api.utils.exportprocess;

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * task node add datasource param strategy
 */
@Service
public class DataSourceParam implements ProcessAddTaskParam, InitializingBean {

    private static final String PARAMS = "params";
    @Autowired
    private DataSourceMapper dataSourceMapper;

    /**
     * add datasource params
     *
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JsonNode addExportSpecialParam(JsonNode taskNode) {
        ObjectNode sqlParameters = JSONUtils.parseObject(taskNode.path(PARAMS).toString());
        DataSource dataSource = dataSourceMapper.selectById(sqlParameters.get("datasource").asInt());
        if (null != dataSource) {
            sqlParameters.put("datasourceName", dataSource.getName());
        }
        ((ObjectNode) taskNode).set(PARAMS, sqlParameters);
        return taskNode;
    }

    /**
     * import process add datasource params
     *
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JsonNode addImportSpecialParam(JsonNode taskNode) {
        ObjectNode sqlParameters = (ObjectNode) taskNode.path(PARAMS);
        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByName(sqlParameters.path("datasourceName").asText());
        if (!dataSources.isEmpty()) {
            DataSource dataSource = dataSources.get(0);
            sqlParameters.put("datasource", dataSource.getId());
        }
        ((ObjectNode) taskNode).set(PARAMS, sqlParameters);
        return taskNode;
    }


    /**
     * put datasource strategy
     */
    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.SQL.getDesc(), this);
        TaskNodeParamFactory.register(TaskType.PROCEDURE.getDesc(), this);
    }
}
