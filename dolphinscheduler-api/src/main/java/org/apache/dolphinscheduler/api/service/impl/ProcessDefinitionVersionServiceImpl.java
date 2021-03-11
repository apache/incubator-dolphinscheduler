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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionVersionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionVersionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.ImmutableMap;

/**
 * process definition version service impl
 */
@Service
public class ProcessDefinitionVersionServiceImpl extends BaseServiceImpl implements ProcessDefinitionVersionService {

    @Autowired
    private ProcessDefinitionVersionMapper processDefinitionVersionMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    /**
     * add the newest version of one process definition
     *
     * @param processDefinition the process definition that need to record version
     * @return the newest version number of this process definition
     */
    @Override
    public long addProcessDefinitionVersion(ProcessDefinition processDefinition) {

        long version = this.queryMaxVersionByProcessDefinitionId(processDefinition.getId()) + 1;

        ProcessDefinitionVersion processDefinitionVersion = ProcessDefinitionVersion
            .newBuilder()
            .processDefinitionId(processDefinition.getId())
            .version(version)
            .processDefinitionJson(processDefinition.getProcessDefinitionJson())
            .description(processDefinition.getDescription())
            .locations(processDefinition.getLocations())
            .connects(processDefinition.getConnects())
            .timeout(processDefinition.getTimeout())
            .globalParams(processDefinition.getGlobalParams())
            .createTime(processDefinition.getUpdateTime())
            .warningGroupId(processDefinition.getWarningGroupId())
                .resourceIds(processDefinition.getResourceIds())
                .build();

        processDefinitionVersionMapper.insert(processDefinitionVersion);

        return version;
    }

    /**
     * query the max version number by the process definition id
     *
     * @param processDefinitionId process definition id
     * @return the max version number of this id
     */
    private long queryMaxVersionByProcessDefinitionId(int processDefinitionId) {
        Long maxVersion = processDefinitionVersionMapper.queryMaxVersionByProcessDefinitionId(processDefinitionId);
        if (Objects.isNull(maxVersion)) {
            return 0L;
        } else {
            return maxVersion;
        }
    }

    /**
     * query the pagination versions info by one certain process definition id
     *
     * @param loginUser login user info to check auth
     * @param projectName process definition project name
     * @param pageNo page number
     * @param pageSize page size
     * @param processDefinitionId process definition id
     * @return the pagination process definition versions info of the certain process definition
     */
    @Override
    public Map<String, Object> queryProcessDefinitionVersions(User loginUser, String projectName, int pageNo, int pageSize, int processDefinitionId) {

        Map<String, Object> result = new HashMap<>();

        // check the if pageNo or pageSize less than 1
        if (pageNo <= 0 || pageSize <= 0) {
            putMsg(result
                    , Status.QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR
                    , pageNo
                    , pageSize);
            return result;
        }

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        PageInfo<ProcessDefinitionVersion> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProcessDefinitionVersion> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinitionVersion> processDefinitionVersionsPaging = processDefinitionVersionMapper.queryProcessDefinitionVersionsPaging(page, processDefinitionId);
        List<ProcessDefinitionVersion> processDefinitionVersions = processDefinitionVersionsPaging.getRecords();
        pageInfo.setLists(processDefinitionVersions);
        pageInfo.setTotalCount((int) processDefinitionVersionsPaging.getTotal());
        return ImmutableMap.of(
                Constants.MSG, Status.SUCCESS.getMsg()
                , Constants.STATUS, Status.SUCCESS
                , Constants.DATA_LIST, pageInfo);
    }

    /**
     * query one certain process definition version by version number and process definition id
     *
     * @param processDefinitionId process definition id
     * @param version version number
     * @return the process definition version info
     */
    @Override
    public ProcessDefinitionVersion queryByProcessDefinitionIdAndVersion(int processDefinitionId, long version) {
        return processDefinitionVersionMapper.queryByProcessDefinitionIdAndVersion(processDefinitionId, version);
    }

    /**
     * delete one certain process definition by version number and process definition id
     *
     * @param loginUser login user info to check auth
     * @param projectName process definition project name
     * @param processDefinitionId process definition id
     * @param version version number
     * @return delete result code
     */
    @Override
    public Map<String, Object> deleteByProcessDefinitionIdAndVersion(User loginUser, String projectName, int processDefinitionId, long version) {
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        processDefinitionVersionMapper.deleteByProcessDefinitionIdAndVersion(processDefinitionId, version);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
