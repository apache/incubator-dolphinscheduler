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
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.ProjectServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * project service test
 **/
@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceTest.class);

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectUserMapper projectUserMapper;

    @Mock
    private ProcessDefinitionMapper processDefinitionMapper;

    private String projectName = "ProjectServiceTest";

    private String userName = "ProjectServiceTest";

    @Test
    public void testCreateProject() {

        User loginUser = getLoginUser();
        loginUser.setId(1);
        Result<Integer> result = projectService.createProject(loginUser, projectName, getDesc());
        logger.info(result.toString());
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

        //project name exist
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(getProject());
        result = projectService.createProject(loginUser, projectName, projectName);
        logger.info(result.toString());
        Assert.assertEquals(Status.PROJECT_ALREADY_EXISTS.getCode(), (int) result.getCode());

        //success
        Mockito.when(projectMapper.insert(Mockito.any(Project.class))).thenReturn(1);
        result = projectService.createProject(loginUser, "test", "test");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testQueryById() {

        //not exist
        Result<Project> result = projectService.queryById(Integer.MAX_VALUE);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) result.getCode());
        logger.info(result.toString());

        //success
        Mockito.when(projectMapper.selectById(1)).thenReturn(getProject());
        result = projectService.queryById(1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testCheckProjectAndAuth() {

        Mockito.when(projectUserMapper.queryProjectRelation(1, 1)).thenReturn(getProjectUser());
        User loginUser = getLoginUser();

        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, null, projectName);
        logger.info(checkParamResult.toString());
        Status status = checkParamResult.getStatus();
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, status);

        Project project = getProject();
        //USER_NO_OPERATION_PROJECT_PERM
        project.setUserId(2);
        checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        logger.info(checkParamResult.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM, checkParamResult.getStatus());

        //success
        project.setUserId(1);
        checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        logger.info(checkParamResult.toString());
        Assert.assertEquals(Status.SUCCESS, checkParamResult.getStatus());


        checkParamResult = projectService.checkProjectAndAuth(loginUser, null, projectName);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, checkParamResult.getStatus());

        Project project1 = getProject();
        // USER_NO_OPERATION_PROJECT_PERM
        project1.setUserId(2);
        checkParamResult = projectService.checkProjectAndAuth(loginUser, project1, projectName);
        Assert.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM, checkParamResult.getStatus());

        //success
        project1.setUserId(1);
        projectService.checkProjectAndAuth(loginUser, project1, projectName);

    }

    @Test
    public void testHasProjectAndPerm() {

        // Mockito.when(projectUserMapper.queryProjectRelation(1, 1)).thenReturn(getProjectUser());
        User loginUser = getLoginUser();
        Project project = getProject();
        // not exist user
        User tempUser = new User();
        tempUser.setId(Integer.MAX_VALUE);
        CheckParamResult result = projectService.hasProjectAndPerm(tempUser, project);
        logger.info(result.toString());
        Assert.assertNotEquals(Status.SUCCESS, result.getStatus());

        //success
        project.setUserId(1);
        result = projectService.hasProjectAndPerm(loginUser, project);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS, result.getStatus());
    }

    @Test
    public void testQueryProjectListPaging() {

        IPage<Project> page = new Page<>(1, 10);
        page.setRecords(getList());
        page.setTotal(1L);
        Mockito.when(projectMapper.queryProjectListPaging(Mockito.any(Page.class), Mockito.eq(1), Mockito.eq(projectName))).thenReturn(page);
        User loginUser = getLoginUser();

        // project owner
        Result<PageListVO<Project>> pageListVOResult = projectService.queryProjectListPaging(loginUser, 10, 1, projectName);
        logger.info(pageListVOResult.toString());
        PageListVO<Project> pageListVO = pageListVOResult.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageListVO.getTotalList()));

        //admin
        Mockito.when(projectMapper.queryProjectListPaging(Mockito.any(Page.class), Mockito.eq(0), Mockito.eq(projectName))).thenReturn(page);
        loginUser.setUserType(UserType.ADMIN_USER);
        pageListVOResult = projectService.queryProjectListPaging(loginUser, 10, 1, projectName);
        logger.info(pageListVOResult.toString());
        pageListVO = pageListVOResult.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageListVO.getTotalList()));
    }

    @Test
    public void testDeleteProject() {

        Mockito.when(projectMapper.selectById(1)).thenReturn(getProject());
        User loginUser = getLoginUser();
        //PROJECT_NOT_FOUNT
        Result<Void> result = projectService.deleteProject(loginUser, 12);
        logger.info(result.toString());
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) result.getCode());
        loginUser.setId(2);
        //USER_NO_OPERATION_PROJECT_PERM
        result = projectService.deleteProject(loginUser, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PROJECT_PERM.getCode(), (int) result.getCode());

        //DELETE_PROJECT_ERROR_DEFINES_NOT_NULL
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(1)).thenReturn(getProcessDefinitions());
        loginUser.setUserType(UserType.ADMIN_USER);
        result = projectService.deleteProject(loginUser, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.DELETE_PROJECT_ERROR_DEFINES_NOT_NULL.getCode(), (int) result.getCode());

        //success
        Mockito.when(projectMapper.deleteById(1)).thenReturn(1);
        Mockito.when(processDefinitionMapper.queryAllDefinitionList(1)).thenReturn(new ArrayList<>());
        result = projectService.deleteProject(loginUser, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testUpdate() {

        User loginUser = getLoginUser();
        Project project = getProject();
        project.setId(2);
        Mockito.when(projectMapper.queryByName(projectName)).thenReturn(project);
        Mockito.when(projectMapper.selectById(1)).thenReturn(getProject());
        // PROJECT_NOT_FOUNT
        Result<Void> result = projectService.update(loginUser, 12, projectName, "desc");
        logger.info(result.toString());
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT.getCode(), (int) result.getCode());

        //PROJECT_ALREADY_EXISTS
        result = projectService.update(loginUser, 1, projectName, "desc");
        logger.info(result.toString());
        Assert.assertEquals(Status.PROJECT_ALREADY_EXISTS.getCode(), (int) result.getCode());

        //success
        project.setUserId(1);
        Mockito.when(projectMapper.updateById(Mockito.any(Project.class))).thenReturn(1);
        result = projectService.update(loginUser, 1, "test", "desc");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testQueryAuthorizedProject() {

        User loginUser = getLoginUser();

        Mockito.when(projectMapper.queryAuthedProjectListByUserId(1)).thenReturn(getList());
        //USER_NO_OPERATION_PERM
        Result<List<Project>> result = projectService.queryAuthorizedProject(loginUser, 3);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        result = projectService.queryAuthorizedProject(loginUser, 1);
        logger.info(result.toString());
        List<Project> projects = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));

        loginUser.setUserType(UserType.GENERAL_USER);
        result = projectService.queryAuthorizedProject(loginUser, loginUser.getId());
        projects = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));
    }

    @Test
    public void testQueryCreatedProject() {

        User loginUser = getLoginUser();

        Mockito.when(projectMapper.queryProjectCreatedByUser(1)).thenReturn(getList());

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        Result<List<Project>> result = projectService.queryProjectCreatedByUser(loginUser);
        logger.info(result.toString());
        List<Project> projects = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryProjectCreatedAndAuthorizedByUser() {

        User loginUser = getLoginUser();

        // not admin user
        Mockito.when(projectMapper.queryProjectCreatedAndAuthorizedByUserId(1)).thenReturn(getList());
        Result<List<Project>> result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> notAdminUserResult = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(notAdminUserResult));

        //admin user
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(projectMapper.selectList(null)).thenReturn(getList());
        result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
        List<Project> projects = result.getData();

        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryAllProjectList() {
        Mockito.when(projectMapper.queryAllProject()).thenReturn(getList());

        Result<List<Project>> result = projectService.queryAllProjectList();
        logger.info(result.toString());
        List<Project> projects = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));

    }

    @Test
    public void testQueryUnauthorizedProject() {
        // Mockito.when(projectMapper.queryAuthedProjectListByUserId(1)).thenReturn(getList());
        Mockito.when(projectMapper.queryProjectExceptUserId(2)).thenReturn(getList());

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);

        Result<List<Project>> result = projectService.queryUnauthorizedProject(loginUser, 2);
        logger.info(result.toString());
        List<Project> projects = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(projects));
    }

    private Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setName(projectName);
        project.setUserId(1);
        return project;
    }

    private List<Project> getList() {
        List<Project> list = new ArrayList<>();
        list.add(getProject());
        return list;
    }

    /**
     * create admin user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName(userName);
        loginUser.setId(1);
        return loginUser;
    }

    /**
     * get project user
     */
    private ProjectUser getProjectUser() {
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(1);
        projectUser.setUserId(1);
        return projectUser;
    }

    private List<ProcessDefinition> getProcessDefinitions() {
        List<ProcessDefinition> list = new ArrayList<>();
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProjectId(1);
        list.add(processDefinition);
        return list;
    }

    private List<Integer> getProjectIds() {
        return Collections.singletonList(1);
    }


    private String getDesc() {
        return "projectUserMapper.deleteProjectRelation(projectId,userId)projectUserMappe"
                + ".deleteProjectRelation(projectId,userId)projectUserMappe"
                + "r.deleteProjectRelation(projectId,userId)projectUserMapper"
                + ".deleteProjectRelation(projectId,userId)projectUserMapper.deleteProjectRelation(projectId,userId)";
    }

}
