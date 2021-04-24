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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.entity.User;


/**
 * process definition version service
 */
public interface ProcessDefinitionVersionService {

    /**
     * add the newest version of one process definition
     *
     * @param processDefinition the process definition that need to record version
     * @return the newest version number of this process definition
     */
    long addProcessDefinitionVersion(ProcessDefinition processDefinition);

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
    Result<PageListVO<ProcessDefinitionVersion>> queryProcessDefinitionVersions(User loginUser, String projectName,
                                                                                int pageNo, int pageSize, int processDefinitionId);

    /**
     * query one certain process definition version by version number and process definition id
     *
     * @param processDefinitionId process definition id
     * @param version version number
     * @return the process definition version info
     */
    ProcessDefinitionVersion queryByProcessDefinitionIdAndVersion(int processDefinitionId,
                                                                  long version);

    /**
     * delete one certain process definition by version number and process definition id
     *
     * @param loginUser login user info to check auth
     * @param projectName process definition project name
     * @param processDefinitionId process definition id
     * @param version version number
     * @return delele result code
     */
    Result<Void> deleteByProcessDefinitionIdAndVersion(User loginUser, String projectName,
                                                       int processDefinitionId, long version);
}
