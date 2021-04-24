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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.AlertGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import java.util.ArrayList;
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
 * alert group service test
 */
@RunWith(MockitoJUnitRunner.class)
public class AlertGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupServiceTest.class);

    @InjectMocks
    private AlertGroupServiceImpl alertGroupService;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    private String groupName = "AlertGroupServiceTest";

    @Test
    public void testQueryAlertGroup() {

        Mockito.when(alertGroupMapper.queryAllGroupList()).thenReturn(getList());
        Result<List<AlertGroup>> result = alertGroupService.queryAlertgroup();
        logger.info(result.toString());
        List<AlertGroup> alertGroups = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(alertGroups));
    }

    @Test
    public void testListPaging() {
        IPage<AlertGroup> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getList());
        Mockito.when(alertGroupMapper.queryAlertGroupPage(any(Page.class), eq(groupName))).thenReturn(page);
        User user = new User();
        // no operate
        Result<PageListVO<AlertGroup>> pageListVOResult = alertGroupService.listPaging(user, groupName, 1, 10);
        logger.info(pageListVOResult.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) pageListVOResult.getCode());
        //success
        user.setUserType(UserType.ADMIN_USER);
        pageListVOResult = alertGroupService.listPaging(user, groupName, 1, 10);
        logger.info(pageListVOResult.toString());
        PageListVO<AlertGroup> alertGroupPageListVO = pageListVOResult.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(alertGroupPageListVO.getTotalList()));

    }

    @Test
    public void testCreateAlertgroup() {

        Mockito.when(alertGroupMapper.insert(any(AlertGroup.class))).thenReturn(2);
        User user = new User();
        //no operate
        Result<?> result = alertGroupService.createAlertgroup(user, groupName, groupName, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        user.setUserType(UserType.ADMIN_USER);
        //success
        result = alertGroupService.createAlertgroup(user, groupName, groupName, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testUpdateAlertgroup() {

        User user = new User();
        // no operate
        Result<Void> result = alertGroupService.updateAlertgroup(user, 1, groupName, groupName, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        user.setUserType(UserType.ADMIN_USER);
        // not exist
        result = alertGroupService.updateAlertgroup(user, 1, groupName, groupName, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.ALERT_GROUP_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.updateAlertgroup(user, 2, groupName, groupName, null);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testDelAlertgroupById() {

        User user = new User();
        // no operate
        Result<Void> result = alertGroupService.delAlertgroupById(user, 1);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        user.setUserType(UserType.ADMIN_USER);
        // not exist
        result = alertGroupService.delAlertgroupById(user, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.ALERT_GROUP_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.delAlertgroupById(user, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testVerifyGroupName() {
        //group name not exist
        boolean result = alertGroupService.existGroupName(groupName);
        Assert.assertFalse(result);
        Mockito.when(alertGroupMapper.existGroupName(groupName)).thenReturn(true);

        //group name exist
        result = alertGroupService.existGroupName(groupName);
        Assert.assertTrue(result);
    }

    /**
     * create admin user
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * get list
     */
    private List<AlertGroup> getList() {
        List<AlertGroup> alertGroups = new ArrayList<>();
        alertGroups.add(getEntity());
        return alertGroups;
    }

    /**
     * get entity
     */
    private AlertGroup getEntity() {
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setId(1);
        alertGroup.setGroupName(groupName);
        return alertGroup;
    }

}
