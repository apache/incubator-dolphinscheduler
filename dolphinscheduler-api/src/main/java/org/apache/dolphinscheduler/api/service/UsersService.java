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
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * users service
 */
public interface UsersService {

    /**
     * create user, only system admin have permission
     *
     * @param loginUser login user
     * @param userName user name
     * @param userPassword user password
     * @param email email
     * @param tenantId tenant id
     * @param phone phone
     * @param queue queue
     * @return create result code
     * @throws Exception exception
     */
    Map<String, Object> createUser(User loginUser, String userName, String userPassword, String email,
                                   int tenantId, String phone, String queue, int state) throws IOException;

    User createUser(String userName, String userPassword, String email,
                    int tenantId, String phone, String queue, int state);

    /***
     * create User for ldap login
     */
    User createUser(UserType userType, String userId, String email);

    /**
     * get user by user name
     *
     * @param userName user name
     * @return exist user or null
     */
    User getUserByUserName(String userName);

    /**
     * query user by id
     *
     * @param id id
     * @return user info
     */
    User queryUser(int id);

    /**
     * query user
     *
     * @param name name
     * @return user info
     */
    User queryUser(String name);

    /**
     * query user
     *
     * @param name name
     * @param password password
     * @return user info
     */
    User queryUser(String name, String password);

    /**
     * get user id by user name
     *
     * @param name user name
     * @return if name empty 0, user not exists -1, user exist user id
     */
    int getUserIdByName(String name);

    /**
     * query user list
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return user list page
     */
    Map<String, Object> queryUserList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * updateProcessInstance user
     *
     *
     * @param loginUser
     * @param userId user id
     * @param userName user name
     * @param userPassword user password
     * @param email email
     * @param tenantId tenant id
     * @param phone phone
     * @param queue queue
     * @return update result code
     * @throws Exception exception
     */
    Map<String, Object> updateUser(User loginUser, int userId, String userName, String userPassword, String email,
                                   int tenantId, String phone, String queue, int state) throws IOException;

    /**
     * delete user
     *
     * @param loginUser login user
     * @param id user id
     * @return delete result code
     * @throws Exception exception when operate hdfs
     */
    Map<String, Object> deleteUserById(User loginUser, int id) throws IOException;

    /**
     * grant project
     *
     * @param loginUser login user
     * @param userId user id
     * @param projectIds project id array
     * @return grant result code
     */
    Map<String, Object> grantProject(User loginUser, int userId, String projectIds);


    /**
     * grant resource
     *
     * @param loginUser login user
     * @param userId user id
     * @param resourceIds resource id array
     * @return grant result code
     */
    Map<String, Object> grantResources(User loginUser, int userId, String resourceIds);


    /**
     * grant udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @param udfIds udf id array
     * @return grant result code
     */
    Map<String, Object> grantUDFFunction(User loginUser, int userId, String udfIds);


    /**
     * grant datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @param datasourceIds data source id array
     * @return grant result code
     */
    Map<String, Object> grantDataSource(User loginUser, int userId, String datasourceIds);

    /**
     * query user info
     *
     * @param loginUser login user
     * @return user info
     */
    Map<String, Object> getUserInfo(User loginUser);

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    Map<String, Object> queryAllGeneralUsers(User loginUser);


    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    Map<String, Object> queryUserList(User loginUser);

    /**
     * verify user name exists
     *
     * @param userName user name
     * @return true if user name not exists, otherwise return false
     */
    Result<Object> verifyUserName(String userName);


    /**
     * unauthorized user
     *
     * @param loginUser login user
     * @param alertgroupId alert group id
     * @return unauthorize result code
     */
    Map<String, Object> unauthorizedUser(User loginUser, Integer alertgroupId);


    /**
     * authorized user
     *
     * @param loginUser login user
     * @param alertgroupId alert group id
     * @return authorized result code
     */
    Map<String, Object> authorizedUser(User loginUser, Integer alertgroupId);

    /**
     * register user, default state is 0, default tenant_id is 1, no phone, no queue
     *
     * @param userName user name
     * @param userPassword user password
     * @param repeatPassword repeat password
     * @param email email
     * @return register result code
     * @throws Exception exception
     */
    Map<String, Object> registerUser(String userName, String userPassword, String repeatPassword, String email);

    /**
     * activate user, only system admin have permission, change user state code 0 to 1
     *
     * @param loginUser login user
     * @param userName user name
     * @return create result code
     */
    Map<String, Object> activateUser(User loginUser, String userName);

    /**
     * activate user, only system admin have permission, change users state code 0 to 1
     *
     * @param loginUser login user
     * @param userNames user name
     * @return create result code
     */
    Map<String, Object> batchActivateUser(User loginUser, List<String> userNames);
}
