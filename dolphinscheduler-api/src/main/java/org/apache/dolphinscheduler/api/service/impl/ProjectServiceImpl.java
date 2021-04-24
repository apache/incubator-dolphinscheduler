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

import static org.apache.dolphinscheduler.api.utils.CheckUtils.checkDesc;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * project service impl
 **/
@Service
public class ProjectServiceImpl extends BaseServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * create project
     *
     * @param loginUser login user
     * @param name project name
     * @param desc description
     * @return returns an error if it exists
     */
    @Override
    public Result<Integer> createProject(User loginUser, String name, String desc) {

        CheckParamResult descCheckResult = checkDesc(desc);
        if (!Status.SUCCESS.equals(descCheckResult.getStatus())) {
            return Result.error(descCheckResult);
        }

        Project project = projectMapper.queryByName(name);
        if (project != null) {
            putMsg(descCheckResult, Status.PROJECT_ALREADY_EXISTS, name);
            return Result.error(descCheckResult);
        }

        Date now = new Date();

        project = Project
                .newBuilder()
                .name(name)
                .description(desc)
                .userId(loginUser.getId())
                .userName(loginUser.getUserName())
                .createTime(now)
                .updateTime(now)
                .build();

        if (projectMapper.insert(project) > 0) {
            return Result.success(project.getId());
        } else {
            putMsg(descCheckResult, Status.CREATE_PROJECT_ERROR);
            return Result.error(descCheckResult);
        }
    }

    /**
     * query project details by id
     *
     * @param projectId project id
     * @return project detail information
     */
    @Override
    public Result<Project> queryById(Integer projectId) {

        Project project = projectMapper.selectById(projectId);

        if (project != null) {
            return Result.success(project);
        } else {
            return Result.errorWithArgs(Status.PROJECT_NOT_FOUNT, projectId);
        }
    }

    /**
     * check project and authorization
     *
     * @param loginUser login user
     * @param project project
     * @param projectName project name
     * @return true if the login user have permission to see the project
     */
    @Override
    public CheckParamResult checkProjectAndAuth(User loginUser, Project project, String projectName) {
        CheckParamResult checkParamResult = new CheckParamResult();
        if (project == null) {
            putMsg(checkParamResult, Status.PROJECT_NOT_FOUNT, projectName);
        } else if (!checkReadPermission(loginUser, project)) {
            // check read permission
            putMsg(checkParamResult, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), projectName);
        } else {
            putMsg(checkParamResult, Status.SUCCESS);
        }
        return checkParamResult;
    }

    @Override
    public CheckParamResult hasProjectAndPerm(User loginUser, Project project) {
        CheckParamResult checkResult = new CheckParamResult();
        if (project == null) {
            putMsg(checkResult, Status.PROJECT_NOT_FOUNT, "");
        } else if (!checkReadPermission(loginUser, project)) {
            putMsg(checkResult, Status.USER_NO_OPERATION_PROJECT_PERM, loginUser.getUserName(), project.getName());
        } else {
            checkResult = new CheckParamResult(Status.SUCCESS);
        }
        return checkResult;
    }

    /**
     * admin can view all projects
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return project list which the login user have permission to see
     */
    @Override
    public Result<PageListVO<Project>> queryProjectListPaging(User loginUser, Integer pageSize, Integer pageNo, String searchVal) {
        PageInfo<Project> pageInfo = new PageInfo<>(pageNo, pageSize);

        Page<Project> page = new Page<>(pageNo, pageSize);

        int userId = loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId();
        IPage<Project> projectIPage = projectMapper.queryProjectListPaging(page, userId, searchVal);

        List<Project> projectList = projectIPage.getRecords();
        if (userId != 0) {
            for (Project project : projectList) {
                project.setPerm(Constants.DEFAULT_ADMIN_PERMISSION);
            }
        }
        pageInfo.setTotalCount((int) projectIPage.getTotal());
        pageInfo.setLists(projectList);

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * delete project by id
     *
     * @param loginUser login user
     * @param projectId project id
     * @return delete result code
     */
    @Override
    public Result<Void> deleteProject(User loginUser, Integer projectId) {
        Project project = projectMapper.selectById(projectId);
        CheckParamResult checkResult = getCheckResult(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        if (!hasPerm(loginUser, project.getUserId())) {
            putMsg(checkResult, Status.USER_NO_OPERATION_PERM);
            return Result.error(checkResult);
        }

        List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryAllDefinitionList(projectId);

        if (!processDefinitionList.isEmpty()) {
            putMsg(checkResult, Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL);
            return Result.error(checkResult);
        }
        int delete = projectMapper.deleteById(projectId);
        if (delete > 0) {
            putMsg(checkResult, Status.SUCCESS);
            return Result.success(null);
        } else {
            putMsg(checkResult, Status.DELETE_PROJECT_ERROR);
            return Result.error(checkResult);
        }
    }

    /**
     * get check result
     *
     * @param loginUser login user
     * @param project project
     * @return check result
     */
    private CheckParamResult getCheckResult(User loginUser, Project project) {
        String projectName = project == null ? null : project.getName();
        return checkProjectAndAuth(loginUser, project, projectName);
    }

    /**
     * updateProcessInstance project
     *
     * @param loginUser login user
     * @param projectId project id
     * @param projectName project name
     * @param desc description
     * @return update result code
     */
    @Override
    public Result<Void> update(User loginUser, Integer projectId, String projectName, String desc) {

        CheckParamResult checkParamResult = checkDesc(desc);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        Project project = projectMapper.selectById(projectId);
        CheckParamResult hasProjectAndPerm = hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(hasProjectAndPerm.getStatus())) {
            return Result.error(hasProjectAndPerm);
        }
        Project tempProject = projectMapper.queryByName(projectName);
        if (tempProject != null && tempProject.getId() != projectId) {
            putMsg(hasProjectAndPerm, Status.PROJECT_ALREADY_EXISTS, projectName);
            return Result.error(hasProjectAndPerm);
        }
        project.setName(projectName);
        project.setDescription(desc);
        project.setUpdateTime(new Date());

        int update = projectMapper.updateById(project);
        if (update > 0) {
            return Result.success(null);
        } else {
            putMsg(hasProjectAndPerm, Status.UPDATE_PROJECT_ERROR);
            return Result.error(hasProjectAndPerm);
        }
    }


    /**
     * query unauthorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return the projects which user have not permission to see
     */
    @Override
    public Result<List<Project>> queryUnauthorizedProject(User loginUser, Integer userId) {
        if (loginUser.getId() != userId && isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        /**
         * query all project list except specified userId
         */
        List<Project> projectList = projectMapper.queryProjectExceptUserId(userId);
        List<Project> resultList = new ArrayList<>();
        Set<Project> projectSet = null;
        if (projectList != null && !projectList.isEmpty()) {
            projectSet = new HashSet<>(projectList);

            List<Project> authedProjectList = projectMapper.queryAuthedProjectListByUserId(userId);

            resultList = getUnauthorizedProjects(projectSet, authedProjectList);
        }
        return Result.success(resultList);
    }

    /**
     * get unauthorized project
     *
     * @param projectSet project set
     * @param authedProjectList authed project list
     * @return project list that authorization
     */
    private List<Project> getUnauthorizedProjects(Set<Project> projectSet, List<Project> authedProjectList) {
        List<Project> resultList;
        Set<Project> authedProjectSet = null;
        if (authedProjectList != null && !authedProjectList.isEmpty()) {
            authedProjectSet = new HashSet<>(authedProjectList);
            projectSet.removeAll(authedProjectSet);

        }
        resultList = new ArrayList<>(projectSet);
        return resultList;
    }


    /**
     * query authorized project
     *
     * @param loginUser login user
     * @param userId user id
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @Override
    public Result<List<Project>> queryAuthorizedProject(User loginUser, Integer userId) {

        if (loginUser.getId() != userId && isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<Project> projects = projectMapper.queryAuthedProjectListByUserId(userId);

        return Result.success(projects);
    }

    /**
     * query authorized project
     *
     * @param loginUser login user
     * @return projects which the user have permission to see, Except for items created by this user
     */
    @Override
    public Result<List<Project>> queryProjectCreatedByUser(User loginUser) {

        List<Project> projects = projectMapper.queryProjectCreatedByUser(loginUser.getId());

        return Result.success(projects);
    }

    /**
     * query authorized and user create project list by user
     *
     * @param loginUser login user
     * @return
     */
    @Override
    public Result<List<Project>> queryProjectCreatedAndAuthorizedByUser(User loginUser) {

        List<Project> projects = null;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            projects = projectMapper.selectList(null);
        } else {
            projects = projectMapper.queryProjectCreatedAndAuthorizedByUserId(loginUser.getId());
        }

        return Result.success(projects);
    }

    /**
     * check whether have read permission
     *
     * @param user user
     * @param project project
     * @return true if the user have permission to see the project, otherwise return false
     */
    private boolean checkReadPermission(User user, Project project) {
        int permissionId = queryPermission(user, project);
        return (permissionId & Constants.READ_PERMISSION) != 0;
    }

    /**
     * query permission id
     *
     * @param user user
     * @param project project
     * @return permission
     */
    private int queryPermission(User user, Project project) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return Constants.READ_PERMISSION;
        }

        if (project.getUserId() == user.getId()) {
            return Constants.ALL_PERMISSIONS;
        }

        ProjectUser projectUser = projectUserMapper.queryProjectRelation(project.getId(), user.getId());

        if (projectUser == null) {
            return 0;
        }

        return projectUser.getPerm();

    }

    /**
     * query all project list
     *
     * @return project list
     */
    @Override
    public Result<List<Project>> queryAllProjectList() {
        List<Project> projects = projectMapper.queryAllProject();

        return Result.success(projects);
    }

}
