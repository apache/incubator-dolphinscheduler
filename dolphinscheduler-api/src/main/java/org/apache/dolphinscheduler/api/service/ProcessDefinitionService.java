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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * process definition service
 */
public interface ProcessDefinitionService {

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     * @throws JsonProcessingException JsonProcessingException
     */
    Map<String, Object> createProcessDefinition(User loginUser,
                                                String projectName,
                                                String name,
                                                String processDefinitionJson,
                                                String desc,
                                                String locations,
                                                String connects) throws JsonProcessingException;

    /**
     * query process definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return definition list
     */
    Map<String, Object> queryProcessDefinitionList(User loginUser,
                                                   String projectName);

    /**
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    Map<String, Object> queryProcessDefinitionListPaging(User loginUser,
                                                         String projectName,
                                                         String searchVal,
                                                         Integer pageNo,
                                                         Integer pageSize,
                                                         Integer userId);

    /**
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */

    Map<String, Object> queryProcessDefinitionById(User loginUser,
                                                   String projectName,
                                                   Integer processId);

    /**
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionName process definition name
     * @return process definition detail
     */

    Map<String, Object> queryProcessDefinitionByName(User loginUser,
                                                   String projectName,
                                                   String processDefinitionName);

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectName projectName
     * @param processDefinitionIds processDefinitionIds
     * @param targetProjectId targetProjectId
     */
    Map<String, Object> batchCopyProcessDefinition(User loginUser,
                                                   String projectName,
                                                   String processDefinitionIds,
                                                   int targetProjectId);

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectName projectName
     * @param processDefinitionIds processDefinitionIds
     * @param targetProjectId targetProjectId
     */
    Map<String, Object> batchMoveProcessDefinition(User loginUser,
                                                   String projectName,
                                                   String processDefinitionIds,
                                                   int targetProjectId);

    /**
     * update  process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */
    Map<String, Object> updateProcessDefinition(User loginUser,
                                                String projectName,
                                                int id,
                                                String name,
                                                String processDefinitionJson, String desc,
                                                String locations, String connects);

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    Map<String, Object> verifyProcessDefinitionName(User loginUser,
                                                    String projectName,
                                                    String name);

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    Map<String, Object> deleteProcessDefinitionById(User loginUser,
                                                    String projectName,
                                                    Integer processDefinitionId);

    /**
     * release process definition: online / offline
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id process definition id
     * @param releaseState release state
     * @return release result code
     */
    Map<String, Object> releaseProcessDefinition(User loginUser,
                                                 String projectName,
                                                 int id,
                                                 ReleaseState releaseState);

    /**
     * batch export process definition by ids
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionIds process definition ids
     * @param response http servlet response
     */
    void batchExportProcessDefinitionByIds(User loginUser,
                                           String projectName,
                                           String processDefinitionIds,
                                           HttpServletResponse response);

    /**
     * import process definition
     *
     * @param loginUser login user
     * @param file process metadata json file
     * @param currentProjectName current project name
     * @return import process
     */
    Map<String, Object> importProcessDefinition(User loginUser,
                                                MultipartFile file,
                                                String currentProjectName);

    /**
     * check the process definition node meets the specifications
     *
     * @param processData process data
     * @param processDefinitionJson process definition json
     * @return check result code
     */
    Map<String, Object> checkProcessNodeList(ProcessData processData,
                                             String processDefinitionJson);

    /**
     * get task node details based on process definition
     *
     * @param defineId define id
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionId(Integer defineId);

    /**
     * get task node details based on process definition
     *
     * @param defineIdList define id list
     * @return task node list
     */
    Map<String, Object> getTaskNodeListByDefinitionIdList(String defineIdList);

    /**
     * query process definition all by project id
     *
     * @param projectId project id
     * @return process definitions in the project
     */
    Map<String, Object> queryProcessDefinitionAllByProjectId(Integer projectId);

    /**
     * Encapsulates the TreeView structure
     *
     * @param processId process definition id
     * @param limit limit
     * @return tree view json data
     * @throws Exception exception
     */
    Map<String, Object> viewTree(Integer processId,
                                 Integer limit) throws Exception;

    /**
     * switch the defined process definition verison
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    Map<String, Object> switchProcessDefinitionVersion(User loginUser, String projectName
            , int processDefinitionId, long version);

    /**
     * query upstream task dependencies by task name
     *
     * @param processId process id
     * @param taskName task name
     * @return task dependencies list
     */
    Map<String, Object> queryUpstreamTaskDependencies(Integer processId, String taskName);

    /**
     * query downstream task dependencies by task name
     *
     * @param processId process id
     * @param taskName task name
     * @return task dependencies list
     */
    Map<String, Object> queryDownstreamTaskDependencies(Integer processId, String taskName);
  
    /**
     * check has associated process definition
     *
     * @param processDefinitionId process definition id
     * @param version version
     * @return The query result has a specific process definition return true
     */
    boolean checkHasAssociatedProcessDefinition(int processDefinitionId, long version);
}

