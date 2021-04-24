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
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.UsersServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
 * users service test
 */
@RunWith(MockitoJUnitRunner.class)
public class UsersServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceTest.class);

    @InjectMocks
    private UsersServiceImpl usersService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    @Mock
    private ProjectUserMapper projectUserMapper;

    @Mock
    private ResourceUserMapper resourceUserMapper;

    @Mock
    private UDFUserMapper udfUserMapper;

    private String queueName = "UsersServiceTestQueue";

    @Before
    public void before() {
    }

    @After
    public void after() {

    }

    @Test
    public void testCreateUserForLdap() {
        String userName = "user1";
        String email = "user1@ldap.com";
        User user = usersService.createUser(UserType.ADMIN_USER, userName, email);
        Assert.assertNotNull(user);
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        String userName = "userTest0001~";
        String userPassword = "userTest";
        String email = "123@qq.com";
        int tenantId = Integer.MAX_VALUE;
        String phone = "13456432345";
        int state = 1;
        try {
            //userName error
            Result<Void> result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            userName = "userTest0001";
            userPassword = "userTest000111111111111111";
            //password error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            userPassword = "userTest0001";
            email = "1q.com";
            //email error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            email = "122222@qq.com";
            phone = "2233";
            //phone error
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            phone = "13456432345";
            //tenantId not exists
            result = usersService.createUser(user, userName, userPassword, email, tenantId, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(), (int) result.getCode());
            //success
            Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
            result = usersService.createUser(user, userName, userPassword, email, 1, phone, queueName, state);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

        } catch (Exception e) {
            logger.error(Status.CREATE_USER_ERROR.getMsg(), e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testQueryUser() {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        when(userMapper.queryUserByNamePassword(userName, EncryptionUtils.getMd5(userPassword))).thenReturn(getGeneralUser());
        User queryUser = usersService.queryUser(userName, userPassword);
        logger.info(queryUser.toString());
        Assert.assertTrue(queryUser != null);
    }

    @Test
    public void testSelectByIds() {
        List<Integer> ids = new ArrayList<>();
        List<User> users = usersService.queryUser(ids);
        Assert.assertTrue(users.isEmpty());
        ids.add(1);
        List<User> userList = new ArrayList<>();
        userList.add(new User());
        when(userMapper.selectByIds(ids)).thenReturn(userList);
        List<User> userList1 = usersService.queryUser(ids);
        Assert.assertFalse(userList1.isEmpty());
    }

    @Test
    public void testGetUserIdByName() {
        User user = new User();
        user.setId(1);
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("test_user");

        //user name null
        int userId = usersService.getUserIdByName("");
        Assert.assertEquals(0, userId);

        //user not exist
        when(usersService.queryUser(user.getUserName())).thenReturn(null);
        int userNotExistId = usersService.getUserIdByName(user.getUserName());
        Assert.assertEquals(-1, userNotExistId);

        //user exist
        when(usersService.queryUser(user.getUserName())).thenReturn(user);
        int userExistId = usersService.getUserIdByName(user.getUserName());
        Assert.assertEquals(user.getId(), userExistId);
    }


    @Test
    public void testQueryUserList() {
        User user = new User();

        //no operate
        Result<List<User>> result = usersService.queryUserList(user);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

        //success
        user.setUserType(UserType.ADMIN_USER);
        when(userMapper.selectList(null)).thenReturn(getUserList());
        result = usersService.queryUserList(user);
        List<User> userList = result.getData();
        Assert.assertTrue(userList.size() > 0);
    }

    @Test
    public void testQueryUserListPage() {
        User user = new User();
        IPage<User> page = new Page<>(1, 10);
        page.setRecords(getUserList());
        when(userMapper.queryUserPaging(any(Page.class), eq("userTest"))).thenReturn(page);

        //no operate
        Result<PageListVO<User>> pageListResult = usersService.queryUserList(user, "userTest", 1, 10);
        logger.info(pageListResult.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) pageListResult.getCode());

        //success
        user.setUserType(UserType.ADMIN_USER);
        pageListResult = usersService.queryUserList(user, "userTest", 1, 10);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) pageListResult.getCode());
        PageListVO<User> userPageListVO = pageListResult.getData();
        Assert.assertTrue(userPageListVO.getTotalList().size() > 0);
    }

    @Test
    public void testUpdateUser() {
        String userName = "userTest0001";
        String userPassword = "userTest0001";
        try {
            //user not exist
            Result<Void> result = usersService.updateUser(getLoginUser(), 0, userName, userPassword, "3443@qq.com", 1, "13457864543", "queue", 1);
            Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());
            logger.info(result.toString());

            //success
            when(userMapper.selectById(1)).thenReturn(getUser());
            result = usersService.updateUser(getLoginUser(), 1, userName, userPassword, "32222s@qq.com", 1, "13457864543", "queue", 1);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        } catch (Exception e) {
            logger.error("update user error", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testDeleteUserById() {
        User loginUser = new User();
        try {
            when(userMapper.queryTenantCodeByUserId(1)).thenReturn(getUser());
            when(userMapper.selectById(1)).thenReturn(getUser());

            //no operate
            Result<Void> result = usersService.deleteUserById(loginUser, 3);
            logger.info(result.toString());
            Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

            // user not exist
            loginUser.setUserType(UserType.ADMIN_USER);
            result = usersService.deleteUserById(loginUser, 3);
            logger.info(result.toString());
            Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());

            //success
            result = usersService.deleteUserById(loginUser, 1);
            logger.info(result.toString());
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        } catch (Exception e) {
            logger.error("delete user error", e);
            Assert.assertTrue(false);
        }


    }

    @Test
    public void testGrantProject() {
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        String projectIds = "100000,120000";
        Result<Void> result = usersService.grantProject(loginUser, 1, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantProject(loginUser, 2, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        when(projectUserMapper.deleteProjectRelation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(1);
        result = usersService.grantProject(loginUser, 1, projectIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testGrantResources() {
        String resourceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Result<Void> result = usersService.grantResources(loginUser, 1, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantResources(loginUser, 2, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        when(resourceMapper.selectById(Mockito.anyInt())).thenReturn(getResource());
        when(resourceUserMapper.deleteResourceUser(1, 0)).thenReturn(1);
        result = usersService.grantResources(loginUser, 1, resourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }


    @Test
    public void testGrantUDFFunction() {
        String udfIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Result<Void> result = usersService.grantUDFFunction(loginUser, 1, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantUDFFunction(loginUser, 2, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        when(udfUserMapper.deleteByUserId(1)).thenReturn(1);
        result = usersService.grantUDFFunction(loginUser, 1, udfIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testGrantDataSource() {
        String datasourceIds = "100000,120000";
        when(userMapper.selectById(1)).thenReturn(getUser());
        User loginUser = new User();
        Result<Void> result = usersService.grantDataSource(loginUser, 1, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //user not exist
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.grantDataSource(loginUser, 2, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());
        //success
        when(datasourceUserMapper.deleteByUserId(Mockito.anyInt())).thenReturn(1);
        result = usersService.grantDataSource(loginUser, 1, datasourceIds);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    private User getLoginUser(){
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    @Test
    public void getUserInfo() {
        User loginUser = new User();
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.ADMIN_USER);
        // get admin user
        Result<User> result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        User tempUser = result.getData();
        //check userName
        Assert.assertEquals("admin", tempUser.getUserName());

        //get general user
        loginUser.setUserType(null);
        loginUser.setId(1);
        when(userMapper.queryDetailsById(1)).thenReturn(getGeneralUser());
        when(alertGroupMapper.queryByUserId(1)).thenReturn(getAlertGroups());
        result = usersService.getUserInfo(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        tempUser = result.getData();
        //check userName
        Assert.assertEquals("userTest0001", tempUser.getUserName());
    }


    @Test
    public void testQueryAllGeneralUsers() {
        User loginUser = new User();
        //no operate
        Result<List<User>> result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        when(userMapper.queryAllGeneralUser()).thenReturn(getUserList());
        result = usersService.queryAllGeneralUsers(loginUser);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        List<User> userList = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    @Test
    public void testVerifyUserName() {
        //not exist user
        Result result = usersService.verifyUserName("admin89899");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        //exist user
        when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
        result = usersService.verifyUserName("userTest0001");
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NAME_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void testUnauthorizedUser() {
        User loginUser = new User();
        when(userMapper.selectList(null)).thenReturn(getUserList());
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        //no operate
        Result<List<User>> result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        loginUser.setUserType(UserType.ADMIN_USER);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //success
        result = usersService.unauthorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }


    @Test
    public void testAuthorizedUser() {
        User loginUser = new User();
        when(userMapper.queryUserListByAlertGroupId(2)).thenReturn(getUserList());
        //no operate
        Result<List<User>> result = usersService.authorizedUser(loginUser, 2);
        logger.info(result.toString());
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());
        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        result = usersService.authorizedUser(loginUser, 2);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        List<User> userList = result.getData();
        logger.info(result.toString());
        Assert.assertTrue(CollectionUtils.isNotEmpty(userList));
    }

    @Test
    public void testRegisterUser() {
        String userName = "userTest0002~";
        String userPassword = "userTest";
        String repeatPassword = "userTest";
        String email = "123@qq.com";
        try {
            //userName error
            Result<User> result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            userName = "userTest0002";
            userPassword = "userTest000111111111111111";
            //password error
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            userPassword = "userTest0002";
            email = "1q.com";
            //email error
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            //repeatPassword error
            email = "7400@qq.com";
            repeatPassword = "userPassword";
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            //success
            repeatPassword = "userTest0002";
            result = usersService.registerUser(userName, userPassword, repeatPassword, email);
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }


    @Test
    public void testActivateUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        String userName = "userTest0002~";
        try {
            //not admin
            Result<User> result = usersService.activateUser(user, userName);
            Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

            //userName error
            user.setUserType(UserType.ADMIN_USER);
            result = usersService.activateUser(user, userName);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) result.getCode());

            //user not exist
            userName = "userTest10013";
            result = usersService.activateUser(user, userName);
            Assert.assertEquals(Status.USER_NOT_EXIST.getCode(), (int) result.getCode());

            //user state error
            userName = "userTest0001";
            when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getUser());
            result = usersService.activateUser(user, userName);
            Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), (int) (result.getCode()));

            //success
            when(userMapper.queryByUserNameAccurately(userName)).thenReturn(getDisabledUser());
            result = usersService.activateUser(user, userName);
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testBatchActivateUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        List<String> userNames = new ArrayList<>();
        userNames.add("userTest0001");
        userNames.add("userTest0002");
        userNames.add("userTest0003~");
        userNames.add("userTest0004");

        try {
            //not admin
            Result<Map<String, Object>> result = usersService.batchActivateUser(user, userNames);
            Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

            //batch activate user names
            user.setUserType(UserType.ADMIN_USER);
            when(userMapper.queryByUserNameAccurately("userTest0001")).thenReturn(getUser());
            when(userMapper.queryByUserNameAccurately("userTest0002")).thenReturn(getDisabledUser());
            result = usersService.batchActivateUser(user, userNames);
            Map<String, Object> responseData = result.getData();
            Map<String, Object> successData = (Map<String, Object>) responseData.get("success");
            int totalSuccess = (Integer) successData.get("sum");

            Map<String, Object> failedData = (Map<String, Object>) responseData.get("failed");
            int totalFailed = (Integer) failedData.get("sum");

            Assert.assertEquals(1, totalSuccess);
            Assert.assertEquals(3, totalFailed);
            Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    /**
     * get disabled user
     */
    private User getDisabledUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        user.setState(0);
        return user;
    }


    /**
     * get user
     */
    private User getGeneralUser() {
        User user = new User();
        user.setUserType(UserType.GENERAL_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        return user;
    }

    private List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(getGeneralUser());
        return userList;
    }

    /**
     * get user
     */
    private User getUser() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setUserName("userTest0001");
        user.setUserPassword("userTest0001");
        user.setState(1);
        return user;
    }

    /**
     * get tenant
     *
     * @return tenant
     */
    private Tenant getTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1);
        return tenant;
    }

    /**
     * get resource
     *
     * @return resource
     */
    private Resource getResource() {
        Resource resource = new Resource();
        resource.setPid(-1);
        resource.setUserId(1);
        resource.setDescription("ResourcesServiceTest.jar");
        resource.setAlias("ResourcesServiceTest.jar");
        resource.setFullName("/ResourcesServiceTest.jar");
        resource.setType(ResourceType.FILE);
        return resource;
    }

    private List<AlertGroup> getAlertGroups() {
        List<AlertGroup> alertGroups = new ArrayList<>();
        AlertGroup alertGroup = new AlertGroup();
        alertGroups.add(alertGroup);
        return alertGroups;
    }

}