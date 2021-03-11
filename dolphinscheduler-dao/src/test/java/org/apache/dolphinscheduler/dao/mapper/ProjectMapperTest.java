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
package org.apache.dolphinscheduler.dao.mapper;


import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProjectMapperTest {

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    UserMapper userMapper;


    /**
     * insert
     * @return Project
     */
    private Project insertOne(){
        //insertOne
        Project project = new Project();
        project.setName("ut project");
        project.setUserId(111);
        projectMapper.insert(project);
        return project;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Project project = insertOne();
        project.setCreateTime(new Date());
        //update
        int update = projectMapper.updateById(project);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Project projectMap = insertOne();
        int delete = projectMapper.deleteById(projectMap.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Project project = insertOne();
        //query
        List<Project> projects = projectMapper.selectList(null);
        Assert.assertNotEquals(projects.size(), 0);
    }

    /**
     * test query detail by id
     */
    @Test
    public void testQueryDetailById() {

        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = insertOne();
        project.setUserId(user.getId());
        projectMapper.updateById(project);
        Project project1 = projectMapper.queryDetailById(project.getId());

        Assert.assertNotEquals(project1, null);
        Assert.assertEquals(project1.getUserName(), user.getUserName());
    }

    /**
     * test query project by name
     */
    @Test
    public void testQueryProjectByName() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = insertOne();
        project.setUserId(user.getId());
        projectMapper.updateById(project);
        Project project1 = projectMapper.queryByName(project.getName());

        Assert.assertNotEquals(project1, null);
    }

    /**
     * test page
     */
    @Test
    public void testQueryProjectListPaging() {
        Project project = insertOne();
        Project project1 = insertOne();

        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);
        project.setUserId(user.getId());
        projectMapper.updateById(project);

        Page<Project> page = new Page(1,3);
        IPage<Project> projectIPage = projectMapper.queryProjectListPaging(
                page,
                project.getUserId(),
                null
        );
        IPage<Project> projectIPage1 = projectMapper.queryProjectListPaging(
                page,
                project.getUserId(),
                project.getName()
        );
        Assert.assertNotEquals(projectIPage.getTotal(), 0);
        Assert.assertNotEquals(projectIPage1.getTotal(), 0);
    }

    /**
     * test query project create user
     */
    @Test
    public void testQueryProjectCreatedByUser() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assert.assertNotEquals(projects.size(), 0);

    }

    /**
     * test query authed project list by userId
     */
    @Test
    public void testQueryAuthedProjectListByUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectCreatedByUser(project.getUserId());

        Assert.assertNotEquals(projects.size(), 0);
    }

    /**
     * test query project expect userId
     */
    @Test
    public void testQueryProjectExceptUserId() {
        Project project = insertOne();

        List<Project> projects = projectMapper.queryProjectExceptUserId(
                100000
        );

        Assert.assertNotEquals(projects.size(), 0);
    }
}