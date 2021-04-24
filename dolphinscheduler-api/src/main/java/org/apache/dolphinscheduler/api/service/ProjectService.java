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

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * project service
 **/
public interface ProjectService {

    /**
     * create project
     *
     * @param loginUser login user
     * @param name project name
     * @param desc description
     * @return returns an error if it exists
     */
    Result<Integer> createProject(User loginUser, String name, String desc);

    /**
     * query project details by id
     *
     * @param projectId project id
     * @return project detail information
     */
    Result<Project> queryById(Integer projectId);

    /**
     * check project and authorization
     *
     * @param loginUser login user
     * @param project project
     * @param projectName project name
     * @return true if the login user have permission to see the project
     */
    CheckParamResult checkProjectAndAuth(User loginUser, Project project, String projectName);

    CheckParamResult hasProjectAndPerm(User loginUser, Project project);

    /**
     * admin can view all projects
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which the login user have permission to see
     */
    Result<PageListVO<Project>> queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal);

    /**
     * delete project by id
     *
     * @param loginUser login user
     * @param projectId project id
     * @return delete result code
     */
    Result<Void> deleteProject(User loginUser, Integer projectId);

    /**
     * updateProcessInstance project
     *
     * @param loginUser login user
     * @param projectId project id
     * @param projectName project name
     * @param desc description
     * @return update result code
     */
    Result<Void> update(User loginUser, Integer projectId, String projectName, String desc);

    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return the projects which user have not permission to see
     */
    Result<List<Project>> queryUnauthorizedProject(User loginUser, Integer userId);

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    Result<List<Project>> queryAuthorizedProject(User loginUser, Integer userId);

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @return projects which the user have permission to see, Except for items created by this user
     */
    Result<List<Project>> queryProjectCreatedByUser(User loginUser);

    /**
     * query all project list that have one or more process definitions.
     *
     * @return project list
     */
    Result<List<Project>> queryAllProjectList();

    /**
     * query authorized and user create project list by user id
     * @param loginUser
     * @return
     */
    Result<List<Project>> queryProjectCreatedAndAuthorizedByUser(User loginUser);

}