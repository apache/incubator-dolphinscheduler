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

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionVersionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionVersionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

    @Autowired
    private ProcessDefinitionService processDefinitionService;

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
    public Result<PageListVO<ProcessDefinitionVersion>> queryProcessDefinitionVersions(User loginUser, String projectName, int pageNo, int pageSize, int processDefinitionId) {

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        PageInfo<ProcessDefinitionVersion> pageInfo = new PageInfo<>(pageNo, pageSize);
        Page<ProcessDefinitionVersion> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinitionVersion> processDefinitionVersionsPaging = processDefinitionVersionMapper.queryProcessDefinitionVersionsPaging(page, processDefinitionId);
        List<ProcessDefinitionVersion> processDefinitionVersions = processDefinitionVersionsPaging.getRecords();
        pageInfo.setLists(processDefinitionVersions);
        pageInfo.setTotalCount((int) processDefinitionVersionsPaging.getTotal());
        return Result.success(new PageListVO<>(pageInfo));
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
     * @return delele result code
     */
    @Override
    public Result<Void> deleteByProcessDefinitionIdAndVersion(User loginUser, String projectName, int processDefinitionId, long version) {
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check has associated process definition
        boolean hasAssociatedProcessDefinition = processDefinitionService.checkHasAssociatedProcessDefinition(processDefinitionId, version);
        if (hasAssociatedProcessDefinition) {
            return Result.error(Status.PROCESS_DEFINITION_VERSION_IS_USED);
        }

        processDefinitionVersionMapper.deleteByProcessDefinitionIdAndVersion(processDefinitionId, version);
        return Result.success(null);
    }



}
