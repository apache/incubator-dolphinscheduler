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
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

/**
 *  datasource mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class DataSourceMapperTest {

    /**
     * datasource mapper
     */
    @Autowired
    DataSourceMapper dataSourceMapper;

    /**
     * datasource user relation mapper
     */
    @Autowired
    DataSourceUserMapper dataSourceUserMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * test insert
     */
    @Test
    public void testInsert(){
        DataSource dataSource = createDataSource();
        assertThat(dataSource.getId(), greaterThan(0));
    }

    /**
     * test query
     */
    @Test
    public void testSelectById() {
        DataSource expectedDataSource = createDataSource();
        DataSource actualDataSource = dataSourceMapper.selectById(expectedDataSource.getId());
        assertEquals(expectedDataSource, actualDataSource);
    }


    /**
     * test query
     */
    @Test
    public void testUpdate() {
        DataSource expectedDataSource = createDataSource();

        expectedDataSource.setName("modify " + expectedDataSource.getName());
        expectedDataSource.setNote("modify " + expectedDataSource.getNote());
        expectedDataSource.setUserId(2);
        expectedDataSource.setType(DbType.HIVE);
        expectedDataSource.setConnectionParams("modify " + expectedDataSource.getConnectionParams());
        expectedDataSource.setUpdateTime(DateUtils.getCurrentDate());

        dataSourceMapper.updateById(expectedDataSource);

        DataSource actualDataSource = dataSourceMapper.selectById(expectedDataSource.getId());

        assertEquals(expectedDataSource, actualDataSource);
    }


    /**
     * test delete
     */
    @Test
    public void testDelete(){
        DataSource expectedDataSource = createDataSource();

        dataSourceMapper.deleteById(expectedDataSource.getId());

        DataSource actualDataSource = dataSourceMapper.selectById(expectedDataSource.getId());

        assertNull(actualDataSource);
    }



    /**
     * test query datasource by type
     */
    @Test
    public void testQueryDataSourceByType() {
        Integer userId = 1;

        Map<Integer, DataSource> datasourceMap = createDataSourceMap(userId, "test");

        List<DataSource> actualDataSources = dataSourceMapper.queryDataSourceByType(
                0, DbType.MYSQL.ordinal());

        assertThat(actualDataSources.size(), greaterThanOrEqualTo(2));

        for (DataSource actualDataSource : actualDataSources){
            DataSource expectedDataSource = datasourceMap.get(actualDataSource.getId());
            if (expectedDataSource != null){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }

    }

    /**
     * test page
     */
    @Test
    public void testSelectPaging() {
        String name = "test";
        Integer userId = 1;

        Map<Integer, DataSource> expectedDataSourceMap = createDataSourceMap(userId, name);

        Page page = new Page(0, 4);

        IPage<DataSource> dataSourceIPage = dataSourceMapper.selectPaging(page, userId, name);
        List<DataSource> actualDataSources = dataSourceIPage.getRecords();

        for (DataSource actualDataSource : actualDataSources){
            DataSource expectedDataSource = expectedDataSourceMap.get(actualDataSource.getId());
            if (expectedDataSource != null){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }

    }

    /**
     * test query datasource by name
     */
    @Test
    public void testQueryDataSourceByName() {
        String name = "test";
        DataSource expectedDataSource = createDataSource(name);

        List<DataSource> actualDataSources = dataSourceMapper.queryDataSourceByName(name);

        for (DataSource actualDataSource : actualDataSources){
            if (expectedDataSource.getId() == actualDataSource.getId()){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }

    }

    /**
     * test query authed datasource
     */
    @Test
    public void testQueryAuthedDatasource() {
        String name = "test";
        Integer userId = 1;

        Map<Integer, DataSource> expectedDataSourceMap = createDataSourceMap(userId, name);

        List<DataSource> actualDataSources = dataSourceMapper.queryAuthedDatasource(userId);

        for (DataSource actualDataSource : actualDataSources){
            DataSource expectedDataSource = expectedDataSourceMap.get(actualDataSource.getId());
            if (expectedDataSource != null){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }

    }

    /**
     * test query datasource except userId
     */
    @Test
    public void testQueryDatasourceExceptUserId() {
        String name = "test";
        Integer userId = 1;

        Map<Integer, DataSource> expectedDataSourceMap = createDataSourceMap(userId, name);

        List<DataSource> actualDataSources = dataSourceMapper.queryDatasourceExceptUserId(userId);

        for (DataSource actualDataSource : actualDataSources){
            DataSource expectedDataSource = expectedDataSourceMap.get(actualDataSource.getId());
            if (expectedDataSource != null){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }
    }

    /**
     * test list all datasource by type
     */
    @Test
    public void testListAllDataSourceByType() {
        Integer count = 10;

        Map<Integer, DataSource> expectedDataSourceMap = createDataSourceMap(count);

        List<DataSource> actualDataSources = dataSourceMapper.listAllDataSourceByType(DbType.MYSQL.ordinal());

        assertThat(actualDataSources.size(), greaterThanOrEqualTo(count));

        for (DataSource actualDataSource : actualDataSources){
            DataSource expectedDataSource = expectedDataSourceMap.get(actualDataSource.getId());
            if (expectedDataSource != null){
                assertEquals(expectedDataSource,actualDataSource);
            }
        }
    }

    @Test
    public void testListAuthorizedDataSource(){
        //create general user
        User generalUser1 = createGeneralUser("user1");
        User generalUser2 = createGeneralUser("user2");

        //create data source
        DataSource dataSource = createDataSource(generalUser1.getId(), "ds-1");
        DataSource unauthorizedDataSource = createDataSource(generalUser2.getId(), "ds-2");


        //data source ids
        Integer[] dataSourceIds = new Integer[]{dataSource.getId(),unauthorizedDataSource.getId()};

        List<DataSource> authorizedDataSource = dataSourceMapper.listAuthorizedDataSource(generalUser1.getId(), dataSourceIds);

        Assert.assertEquals(generalUser1.getId(),dataSource.getUserId());
        Assert.assertNotEquals(generalUser1.getId(),unauthorizedDataSource.getUserId());
        Assert.assertFalse(authorizedDataSource.stream().map(t -> t.getId()).collect(toList()).containsAll(Arrays.asList(dataSourceIds)));

        //authorize object unauthorizedDataSource to generalUser1
        createUserDataSource(generalUser1, unauthorizedDataSource);
        authorizedDataSource = dataSourceMapper.listAuthorizedDataSource(generalUser1.getId(), dataSourceIds);

        Assert.assertTrue(authorizedDataSource.stream().map(t -> t.getId()).collect(toList()).containsAll(Arrays.asList(dataSourceIds)));
    }

    /**
     * create datasource relation
     * @param userId
     */
    private Map<Integer,DataSource> createDataSourceMap(Integer userId,String name){

        Map<Integer,DataSource> dataSourceMap = new HashMap<>();

        DataSource dataSource = createDataSource(userId, name);

        dataSourceMap.put(dataSource.getId(),dataSource);

        DataSource otherDataSource = createDataSource(userId + 1,name);

        DatasourceUser datasourceUser = new DatasourceUser();

        datasourceUser.setDatasourceId(otherDataSource.getId());
        datasourceUser.setUserId(userId);
        datasourceUser.setPerm(7);
        datasourceUser.setCreateTime(DateUtils.getCurrentDate());
        datasourceUser.setUpdateTime(DateUtils.getCurrentDate());

        dataSourceUserMapper.insert(datasourceUser);

        dataSourceMap.put(otherDataSource.getId(), otherDataSource);

        return dataSourceMap;
    }

    /**
     * create datasource map
     * @param count datasource count
     * @return datasource map
     */
    private Map<Integer,DataSource> createDataSourceMap(Integer count){
        Map<Integer,DataSource> dataSourceMap = new HashMap<>();

        for (int i = 0 ; i < count ;i++){
            DataSource dataSource = createDataSource("test");
            dataSourceMap.put(dataSource.getId(),dataSource);
        }

        return dataSourceMap;
    }

    /**
     * create datasource
     * @return datasource
     */
    private DataSource createDataSource(){
        return createDataSource(1,"test");
    }


    /**
     * create datasource
     * @param name name
     * @return datasource
     */
    private DataSource createDataSource(String name){
        return createDataSource(1,name);
    }

    /**
     * create datasource
     * @param userId userId
     * @param name name
     * @return datasource
     */
    private DataSource createDataSource(Integer userId,String name){
        Random random = new Random();
        DataSource dataSource = new DataSource();
        dataSource.setUserId(userId);
        dataSource.setName(name);
        dataSource.setType(DbType.MYSQL);
        dataSource.setNote("mysql test");
        dataSource.setConnectionParams("hello mysql");
        dataSource.setUpdateTime(DateUtils.getCurrentDate());
        dataSource.setCreateTime(DateUtils.getCurrentDate());

        dataSourceMapper.insert(dataSource);

        return dataSource;
    }

    /**
     * create general user
     * @return User
     */
    private User createGeneralUser(String userName){
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
     * create the relation of user and data source
     * @param user          user
     * @param dataSource    data source
     * @return DatasourceUser
     */
    private DatasourceUser createUserDataSource(User user,DataSource dataSource){
        DatasourceUser datasourceUser = new DatasourceUser();

        datasourceUser.setDatasourceId(dataSource.getId());
        datasourceUser.setUserId(user.getId());
        datasourceUser.setPerm(7);
        datasourceUser.setCreateTime(DateUtils.getCurrentDate());
        datasourceUser.setUpdateTime(DateUtils.getCurrentDate());

        dataSourceUserMapper.insert(datasourceUser);
        return datasourceUser;
    }


}