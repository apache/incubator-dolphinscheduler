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


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class UdfFuncMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    UdfFuncMapper udfFuncMapper;

    @Autowired
    UDFUserMapper udfUserMapper;

    /**
     * insert one udf
     *
     * @return UdfFunc
     */
    private UdfFunc insertOne() {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setUserId(1);
        udfFunc.setFuncName("dolphin_udf_func");
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mr");
        udfFunc.setType(UdfType.HIVE);
        udfFunc.setResourceId(1);
        udfFunc.setResourceName("dolphin_resource");
        udfFunc.setCreateTime(new Date());
        udfFunc.setUpdateTime(new Date());
        udfFuncMapper.insert(udfFunc);
        return udfFunc;
    }

    /**
     * insert one udf
     *
     * @return
     */
    private UdfFunc insertOne(User user) {
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setUserId(user.getId());
        udfFunc.setFuncName("dolphin_udf_func");
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mr");
        udfFunc.setType(UdfType.HIVE);
        udfFunc.setResourceId(1);
        udfFunc.setResourceName("dolphin_resource");
        udfFunc.setCreateTime(new Date());
        udfFunc.setUpdateTime(new Date());
        udfFuncMapper.insert(udfFunc);
        return udfFunc;
    }

    /**
     * insert one user
     *
     * @return User
     */
    private User insertOneUser() {
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert one user
     *
     * @return User
     */
    private User insertOneUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * insert UDFUser
     *
     * @param user    user
     * @param udfFunc udf func
     * @return UDFUser
     */
    private UDFUser insertOneUDFUser(User user, UdfFunc udfFunc) {
        UDFUser udfUser = new UDFUser();
        udfUser.setUdfId(udfFunc.getId());
        udfUser.setUserId(user.getId());
        udfUser.setCreateTime(new Date());
        udfUser.setUpdateTime(new Date());
        udfUserMapper.insert(udfUser);
        return udfUser;
    }

    /**
     * create general user
     *
     * @return User
     */
    private User createGeneralUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        //insertOne
        UdfFunc udfFunc = insertOne();
        udfFunc.setResourceName("dolphin_resource_update");
        udfFunc.setResourceId(2);
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mrUpdate");
        udfFunc.setUpdateTime(new Date());
        //update
        int update = udfFuncMapper.updateById(udfFunc);
        Assert.assertEquals(update, 1);

    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        //insertOne
        UdfFunc udfFunc = insertOne();
        //delete
        int delete = udfFuncMapper.deleteById(udfFunc.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        //insertOne
        UdfFunc udfFunc = insertOne();
        //query
        List<UdfFunc> udfFuncList = udfFuncMapper.selectList(null);
        Assert.assertNotEquals(udfFuncList.size(), 0);
    }

    /**
     * test query udf by ids
     */
    @Test
    public void testQueryUdfByIdStr() {
        //insertOne
        UdfFunc udfFunc = insertOne();
        //insertOne
        UdfFunc udfFunc1 = insertOne();
        int[] idArray = new int[]{udfFunc.getId(), udfFunc1.getId()};
        //queryUdfByIdStr
        List<UdfFunc> udfFuncList = udfFuncMapper.queryUdfByIdStr(idArray, "");
        Assert.assertNotEquals(udfFuncList.size(), 0);
    }

    /**
     * test page
     */
    @Test
    public void testQueryUdfFuncPaging() {
        //insertOneUser
        User user = insertOneUser();
        //insertOne
        UdfFunc udfFunc = insertOne(user);
        //queryUdfFuncPaging
        Page<UdfFunc> page = new Page(1, 3);
        IPage<UdfFunc> udfFuncIPage = udfFuncMapper.queryUdfFuncPaging(page, user.getId(), "");
        Assert.assertNotEquals(udfFuncIPage.getTotal(), 0);

    }

    /**
     * test get udffunc by type
     */
    @Test
    public void testGetUdfFuncByType() {
        //insertOneUser
        User user = insertOneUser();
        //insertOne
        UdfFunc udfFunc = insertOne(user);
        //getUdfFuncByType
        List<UdfFunc> udfFuncList = udfFuncMapper.getUdfFuncByType(user.getId(), udfFunc.getType().ordinal());
        Assert.assertNotEquals(udfFuncList.size(), 0);

    }

    /**
     * test query udffunc expect userId
     */
    @Test
    public void testQueryUdfFuncExceptUserId() {
        //insertOneUser
        User user1 = insertOneUser();
        User user2 = insertOneUser("user2");
        //insertOne
        UdfFunc udfFunc1 = insertOne(user1);
        UdfFunc udfFunc2 = insertOne(user2);
        List<UdfFunc> udfFuncList = udfFuncMapper.queryUdfFuncExceptUserId(user1.getId());
        Assert.assertNotEquals(udfFuncList.size(), 0);

    }

    /**
     * test query authed udffunc
     */
    @Test
    public void testQueryAuthedUdfFunc() {
        //insertOneUser
        User user = insertOneUser();

        //insertOne
        UdfFunc udfFunc = insertOne(user);

        //insertOneUDFUser
        UDFUser udfUser = insertOneUDFUser(user, udfFunc);
        //queryAuthedUdfFunc
        List<UdfFunc> udfFuncList = udfFuncMapper.queryAuthedUdfFunc(user.getId());
        Assert.assertNotEquals(udfFuncList.size(), 0);
    }

    @Test
    public void testListAuthorizedUdfFunc() {
        //create general user
        User generalUser1 = createGeneralUser("user1");
        User generalUser2 = createGeneralUser("user2");

        //create udf function
        UdfFunc udfFunc = insertOne(generalUser1);
        UdfFunc unauthorizedUdfFunc = insertOne(generalUser2);

        //udf function ids
        Integer[] udfFuncIds = new Integer[]{udfFunc.getId(), unauthorizedUdfFunc.getId()};

        List<UdfFunc> authorizedUdfFunc = udfFuncMapper.listAuthorizedUdfFunc(generalUser1.getId(), udfFuncIds);

        Assert.assertEquals(generalUser1.getId(), udfFunc.getUserId());
        Assert.assertNotEquals(generalUser1.getId(), unauthorizedUdfFunc.getUserId());
        Assert.assertFalse(authorizedUdfFunc.stream().map(t -> t.getId()).collect(toList()).containsAll(Arrays.asList(udfFuncIds)));


        //authorize object unauthorizedUdfFunc to generalUser1
        insertOneUDFUser(generalUser1, unauthorizedUdfFunc);
        authorizedUdfFunc = udfFuncMapper.listAuthorizedUdfFunc(generalUser1.getId(), udfFuncIds);
        Assert.assertTrue(authorizedUdfFunc.stream().map(t -> t.getId()).collect(toList()).containsAll(Arrays.asList(udfFuncIds)));
    }

    @Test
    public void batchUpdateUdfFuncTest() {
        //create general user
        User generalUser1 = createGeneralUser("user1");
        UdfFunc udfFunc = insertOne(generalUser1);
        udfFunc.setResourceName("/updateTest");
        List<UdfFunc> udfFuncList = new ArrayList<>();
        udfFuncList.add(udfFunc);
        Assert.assertTrue(udfFuncMapper.batchUpdateUdfFunc(udfFuncList) > 0);

    }
}