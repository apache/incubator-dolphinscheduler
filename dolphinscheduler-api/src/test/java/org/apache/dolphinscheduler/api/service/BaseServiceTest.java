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
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base service test
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"sun.security.*", "javax.net.*"})
@PrepareForTest({HadoopUtils.class})
public class BaseServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseServiceTest.class);

    private BaseServiceImpl baseService;

    @Mock
    private HadoopUtils hadoopUtils;

    @Before
    public void setUp() {
        baseService = new BaseServiceImpl();
    }

    @Test
    public void testIsAdmin(){

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        //ADMIN_USER
         boolean isAdmin = baseService.isAdmin(user);
        Assert.assertTrue(isAdmin);
        //GENERAL_USER
        user.setUserType(UserType.GENERAL_USER);
        isAdmin = baseService.isAdmin(user);
        Assert.assertFalse(isAdmin);

    }


    @Test
    public void testPutMsg() {

        CheckParamResult result = new CheckParamResult();
        baseService.putMsg(result, Status.SUCCESS);
        Assert.assertEquals(Status.SUCCESS, result.getStatus());
        //has params
        baseService.putMsg(result, Status.PROJECT_NOT_FOUNT, "test");

    }

    @Test
    public void testPutMsgTwo() {

        Result result = new Result();
        baseService.putMsg(result, Status.SUCCESS);
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        //has params
        baseService.putMsg(result, Status.PROJECT_NOT_FOUNT, "test");
    }

    @Test
    public void testCreateTenantDirIfNotExists() {

        PowerMockito.mockStatic(HadoopUtils.class);
        PowerMockito.when(HadoopUtils.getInstance()).thenReturn(hadoopUtils);

        try {
            baseService.createTenantDirIfNotExists("test");
        } catch (Exception e) {
            Assert.assertTrue(false);
            logger.error("CreateTenantDirIfNotExists error ", e);
            e.printStackTrace();
        }

    }
    @Test
    public void testHasPerm(){

        User user = new User();
        user.setId(1);
        //create user
        boolean hasPerm = baseService.hasPerm(user,1);
        Assert.assertTrue(hasPerm);

        //admin
        user.setId(2);
        user.setUserType(UserType.ADMIN_USER);
        hasPerm = baseService.hasPerm(user,1);
        Assert.assertTrue(hasPerm);

    }

}
