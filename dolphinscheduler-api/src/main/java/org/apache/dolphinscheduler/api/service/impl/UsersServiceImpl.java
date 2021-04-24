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

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.dto.resources.visitor.ResourceTreeVisitor;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.EncryptionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DatasourceUser;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.ResourcesUser;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UDFUser;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UDFUserMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.utils.ResourceProcessDefinitionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * users service impl
 */
@Service
public class UsersServiceImpl extends BaseServiceImpl implements UsersService {

    private static final Logger logger = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectUserMapper projectUserMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    @Autowired
    private UDFUserMapper udfUserMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;


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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createUser(User loginUser,
                                   String userName,
                                   String userPassword,
                                   String email,
                                   int tenantId,
                                   String phone,
                                   String queue,
                                   int state) throws IOException {
        //check all user params
        String msg = this.checkUserParams(userName, userPassword, email, phone);
        CheckParamResult checkResult = new CheckParamResult();
        if (!StringUtils.isEmpty(msg)) {
            putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
            return Result.error(checkResult);
        }
        if (!isAdmin(loginUser)) {
            putMsg(checkResult, Status.USER_NO_OPERATION_PERM);
            return Result.error(checkResult);
        }

        if (!checkTenantExists(tenantId)) {
            putMsg(checkResult, Status.TENANT_NOT_EXIST);
            return Result.error(checkResult);
        }

        User user = createUser(userName, userPassword, email, tenantId, phone, queue, state);

        Tenant tenant = tenantMapper.queryById(tenantId);
        // resource upload startup
        if (PropertyUtils.getResUploadStartupState()) {
            // if tenant not exists
            if (!HadoopUtils.getInstance().exists(HadoopUtils.getHdfsTenantDir(tenant.getTenantCode()))) {
                createTenantDirIfNotExists(tenant.getTenantCode());
            }
            String userPath = HadoopUtils.getHdfsUserDir(tenant.getTenantCode(), user.getId());
            HadoopUtils.getInstance().mkdir(userPath);
        }

        return Result.success(null);

    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public User createUser(String userName,
                           String userPassword,
                           String email,
                           int tenantId,
                           String phone,
                           String queue,
                           int state) {
        User user = new User();
        Date now = new Date();

        user.setUserName(userName);
        user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        user.setEmail(email);
        user.setTenantId(tenantId);
        user.setPhone(phone);
        user.setState(state);
        // create general users, administrator users are currently built-in
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        if (StringUtils.isEmpty(queue)) {
            queue = "";
        }
        user.setQueue(queue);

        // save user
        userMapper.insert(user);
        return user;
    }

    /***
     * create User for ldap login
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(UserType userType, String userId, String email) {
        User user = new User();
        Date now = new Date();

        user.setUserName(userId);
        user.setEmail(email);
        // create general users, administrator users are currently built-in
        user.setUserType(userType);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setQueue("");

        // save user
        userMapper.insert(user);
        return user;
    }

    /**
     * get user by user name
     *
     * @param userName user name
     * @return exist user or null
     */
    @Override
    public User getUserByUserName(String userName) {
        return userMapper.queryByUserNameAccurately(userName);
    }

    /**
     * query user by id
     *
     * @param id id
     * @return user info
     */
    @Override
    public User queryUser(int id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> queryUser(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return userMapper.selectByIds(ids);
    }

    /**
     * query user
     *
     * @param name name
     * @return user info
     */
    @Override
    public User queryUser(String name) {
        return userMapper.queryByUserNameAccurately(name);
    }

    /**
     * query user
     *
     * @param name name
     * @param password password
     * @return user info
     */
    @Override
    public User queryUser(String name, String password) {
        String md5 = EncryptionUtils.getMd5(password);
        return userMapper.queryUserByNamePassword(name, md5);
    }

    /**
     * get user id by user name
     *
     * @param name user name
     * @return if name empty 0, user not exists -1, user exist user id
     */
    @Override
    public int getUserIdByName(String name) {
        //executor name query
        int executorId = 0;
        if (StringUtils.isNotEmpty(name)) {
            User executor = queryUser(name);
            if (null != executor) {
                executorId = executor.getId();
            } else {
                executorId = -1;
            }
        }

        return executorId;
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search avlue
     * @param pageSize page size
     * @return user list page
     */
    @Override
    public Result<PageListVO<User>> queryUserList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Page<User> page = new Page<>(pageNo, pageSize);

        IPage<User> scheduleList = userMapper.queryUserPaging(page, searchVal);

        PageInfo<User> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) scheduleList.getTotal());
        pageInfo.setLists(scheduleList.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * updateProcessInstance user
     *
     *
     * @param loginUser
     * @param userId user id
     * @param userName user name
     * @param userPassword user password
     * @param email email
     * @param tenantId tennat id
     * @param phone phone
     * @param queue queue
     * @return update result code
     * @throws Exception exception
     */
    @Override
    public Result<Void> updateUser(User loginUser, int userId,
                                   String userName,
                                   String userPassword,
                                   String email,
                                   int tenantId,
                                   String phone,
                                   String queue,
                                   int state) throws IOException {

        if (!hasPerm(loginUser, userId)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userId);
        }
        if (StringUtils.isNotEmpty(userName)) {

            if (!CheckUtils.checkUserName(userName)) {
                return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
            }

            User tempUser = userMapper.queryByUserNameAccurately(userName);
            if (tempUser != null && tempUser.getId() != userId) {
                return Result.error(Status.USER_NAME_EXIST);
            }
            user.setUserName(userName);
        }

        if (StringUtils.isNotEmpty(userPassword)) {
            if (!CheckUtils.checkPassword(userPassword)) {
                return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userPassword);
            }
            user.setUserPassword(EncryptionUtils.getMd5(userPassword));
        }

        if (StringUtils.isNotEmpty(email)) {
            if (!CheckUtils.checkEmail(email)) {
                return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, email);
            }
            user.setEmail(email);
        }

        if (StringUtils.isNotEmpty(phone) && !CheckUtils.checkPhone(phone)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, phone);
        }
        user.setPhone(phone);
        user.setQueue(queue);
        user.setState(state);
        Date now = new Date();
        user.setUpdateTime(now);

        //if user switches the tenant, the user's resources need to be copied to the new tenant
        if (user.getTenantId() != tenantId) {
            Tenant oldTenant = tenantMapper.queryById(user.getTenantId());
            //query tenant
            Tenant newTenant = tenantMapper.queryById(tenantId);
            if (newTenant != null) {
                // if hdfs startup
                if (PropertyUtils.getResUploadStartupState() && oldTenant != null) {
                    String newTenantCode = newTenant.getTenantCode();
                    String oldResourcePath = HadoopUtils.getHdfsResDir(oldTenant.getTenantCode());
                    String oldUdfsPath = HadoopUtils.getHdfsUdfDir(oldTenant.getTenantCode());

                    // if old tenant dir exists
                    if (HadoopUtils.getInstance().exists(oldResourcePath)) {
                        String newResourcePath = HadoopUtils.getHdfsResDir(newTenantCode);
                        String newUdfsPath = HadoopUtils.getHdfsUdfDir(newTenantCode);

                        //file resources list
                        List<Resource> fileResourcesList = resourceMapper.queryResourceList(
                                null, userId, ResourceType.FILE.ordinal());
                        if (CollectionUtils.isNotEmpty(fileResourcesList)) {
                            ResourceTreeVisitor resourceTreeVisitor = new ResourceTreeVisitor(fileResourcesList);
                            ResourceComponent resourceComponent = resourceTreeVisitor.visit();
                            copyResourceFiles(resourceComponent, oldResourcePath, newResourcePath);
                        }

                        //udf resources
                        List<Resource> udfResourceList = resourceMapper.queryResourceList(
                                null, userId, ResourceType.UDF.ordinal());
                        if (CollectionUtils.isNotEmpty(udfResourceList)) {
                            ResourceTreeVisitor resourceTreeVisitor = new ResourceTreeVisitor(udfResourceList);
                            ResourceComponent resourceComponent = resourceTreeVisitor.visit();
                            copyResourceFiles(resourceComponent, oldUdfsPath, newUdfsPath);
                        }

                        //Delete the user from the old tenant directory
                        String oldUserPath = HadoopUtils.getHdfsUserDir(oldTenant.getTenantCode(), userId);
                        HadoopUtils.getInstance().delete(oldUserPath, true);
                    } else {
                        // if old tenant dir not exists , create
                        createTenantDirIfNotExists(oldTenant.getTenantCode());
                    }

                    if (HadoopUtils.getInstance().exists(HadoopUtils.getHdfsTenantDir(newTenant.getTenantCode()))) {
                        //create user in the new tenant directory
                        String newUserPath = HadoopUtils.getHdfsUserDir(newTenant.getTenantCode(), user.getId());
                        HadoopUtils.getInstance().mkdir(newUserPath);
                    } else {
                        // if new tenant dir not exists , create
                        createTenantDirIfNotExists(newTenant.getTenantCode());
                    }

                }
            }
            user.setTenantId(tenantId);
        }

        // updateProcessInstance user
        userMapper.updateById(user);
        return Result.success(null);
    }

    /**
     * delete user
     *
     * @param loginUser login user
     * @param id user id
     * @return delete result code
     * @throws Exception exception when operate hdfs
     */
    @Override
    public Result<Void> deleteUserById(User loginUser, int id) throws IOException {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.errorWithArgs(Status.USER_NO_OPERATION_PERM, id);
        }
        //check exist
        User tempUser = userMapper.selectById(id);
        if (tempUser == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, id);
        }
        // delete user
        User user = userMapper.queryTenantCodeByUserId(id);

        if (user != null) {
            if (PropertyUtils.getResUploadStartupState()) {
                String userPath = HadoopUtils.getHdfsUserDir(user.getTenantCode(), id);
                if (HadoopUtils.getInstance().exists(userPath)) {
                    HadoopUtils.getInstance().delete(userPath, true);
                }
            }
        }

        userMapper.deleteById(id);

        return Result.success(null);
    }

    /**
     * grant project
     *
     * @param loginUser login user
     * @param userId user id
     * @param projectIds project id array
     * @return grant result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> grantProject(User loginUser, int userId, String projectIds) {

        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        //check exist
        User tempUser = userMapper.selectById(userId);
        if (tempUser == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userId);
        }
        //if the selected projectIds are empty, delete all items associated with the user
        projectUserMapper.deleteProjectRelation(0, userId);

        if (StringUtils.isEmpty(projectIds)) {
            return Result.success(null);
        }

        String[] projectIdArr = projectIds.split(",");

        for (String projectId : projectIdArr) {
            Date now = new Date();
            ProjectUser projectUser = new ProjectUser();
            projectUser.setUserId(userId);
            projectUser.setProjectId(Integer.parseInt(projectId));
            projectUser.setPerm(7);
            projectUser.setCreateTime(now);
            projectUser.setUpdateTime(now);
            projectUserMapper.insert(projectUser);
        }

        return Result.success(null);
    }

    /**
     * grant resource
     *
     * @param loginUser login user
     * @param userId user id
     * @param resourceIds resource id array
     * @return grant result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> grantResources(User loginUser, int userId, String resourceIds) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userId);
        }

        Set<Integer> needAuthorizeResIds = new HashSet<>();
        if (StringUtils.isNotBlank(resourceIds)) {
            String[] resourceFullIdArr = resourceIds.split(",");
            // need authorize resource id set
            for (String resourceFullId : resourceFullIdArr) {
                String[] resourceIdArr = resourceFullId.split("-");
                for (int i = 0; i <= resourceIdArr.length - 1; i++) {
                    int resourceIdValue = Integer.parseInt(resourceIdArr[i]);
                    needAuthorizeResIds.add(resourceIdValue);
                }
            }
        }

        //get the authorized resource id list by user id
        List<Integer> resIds = resourceUserMapper.queryResourcesIdListByUserIdAndPerm(userId, Constants.AUTHORIZE_WRITABLE_PERM);
        List<Resource> oldAuthorizedRes = CollectionUtils.isEmpty(resIds) ? new ArrayList<>() : resourceMapper.queryResourceListById(resIds);
        //if resource type is UDF,need check whether it is bound by UDF function
        Set<Integer> oldAuthorizedResIds = oldAuthorizedRes.stream().map(Resource::getId).collect(Collectors.toSet());

        //get the unauthorized resource id list
        oldAuthorizedResIds.removeAll(needAuthorizeResIds);

        if (CollectionUtils.isNotEmpty(oldAuthorizedResIds)) {

            // get all resource id of process definitions those is released
            List<Map<String, Object>> list = processDefinitionMapper.listResourcesByUser(userId);
            Map<Integer, Set<Integer>> resourceProcessMap = ResourceProcessDefinitionUtils.getResourceProcessDefinitionMap(list);
            Set<Integer> resourceIdSet = resourceProcessMap.keySet();

            resourceIdSet.retainAll(oldAuthorizedResIds);
            if (CollectionUtils.isNotEmpty(resourceIdSet)) {
                logger.error("can't be deleted,because it is used of process definition");
                for (Integer resId : resourceIdSet) {
                    logger.error("resource id:{} is used of process definition {}", resId, resourceProcessMap.get(resId));
                }
                return Result.error(Status.RESOURCE_IS_USED);
            }

        }

        resourceUserMapper.deleteResourceUser(userId, 0);

        if (StringUtils.isEmpty(resourceIds)) {
            return Result.success(null);
        }

        for (int resourceIdValue : needAuthorizeResIds) {
            Resource resource = resourceMapper.selectById(resourceIdValue);
            if (resource == null) {
                return Result.error(Status.RESOURCE_NOT_EXIST);
            }

            Date now = new Date();
            ResourcesUser resourcesUser = new ResourcesUser();
            resourcesUser.setUserId(userId);
            resourcesUser.setResourcesId(resourceIdValue);
            if (resource.isDirectory()) {
                resourcesUser.setPerm(Constants.AUTHORIZE_READABLE_PERM);
            } else {
                resourcesUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
            }

            resourcesUser.setCreateTime(now);
            resourcesUser.setUpdateTime(now);
            resourceUserMapper.insert(resourcesUser);

        }

        return Result.success(null);
    }

    /**
     * grant udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @param udfIds udf id array
     * @return grant result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> grantUDFFunction(User loginUser, int userId, String udfIds) {

        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userId);
        }

        udfUserMapper.deleteByUserId(userId);

        if (StringUtils.isEmpty(udfIds)) {
            return Result.success(null);
        }

        String[] resourcesIdArr = udfIds.split(",");

        for (String udfId : resourcesIdArr) {
            Date now = new Date();
            UDFUser udfUser = new UDFUser();
            udfUser.setUserId(userId);
            udfUser.setUdfId(Integer.parseInt(udfId));
            udfUser.setPerm(7);
            udfUser.setCreateTime(now);
            udfUser.setUpdateTime(now);
            udfUserMapper.insert(udfUser);
        }

        return Result.success(null);
    }

    /**
     * grant datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @param datasourceIds data source id array
     * @return grant result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> grantDataSource(User loginUser, int userId, String datasourceIds) {

        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userId);
        }

        datasourceUserMapper.deleteByUserId(userId);

        if (StringUtils.isEmpty(datasourceIds)) {
            return Result.success(null);
        }

        String[] datasourceIdArr = datasourceIds.split(",");

        for (String datasourceId : datasourceIdArr) {
            Date now = new Date();

            DatasourceUser datasourceUser = new DatasourceUser();
            datasourceUser.setUserId(userId);
            datasourceUser.setDatasourceId(Integer.parseInt(datasourceId));
            datasourceUser.setPerm(7);
            datasourceUser.setCreateTime(now);
            datasourceUser.setUpdateTime(now);
            datasourceUserMapper.insert(datasourceUser);
        }

        return Result.success(null);
    }

    /**
     * query user info
     *
     * @param loginUser login user
     * @return user info
     */
    @Override
    public Result<User> getUserInfo(User loginUser) {

        User user = null;
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            user = loginUser;
        } else {
            user = userMapper.queryDetailsById(loginUser.getId());

            List<AlertGroup> alertGroups = alertGroupMapper.queryByUserId(loginUser.getId());

            StringBuilder sb = new StringBuilder();

            if (alertGroups != null && !alertGroups.isEmpty()) {
                for (int i = 0; i < alertGroups.size() - 1; i++) {
                    sb.append(alertGroups.get(i).getGroupName() + ",");
                }
                sb.append(alertGroups.get(alertGroups.size() - 1));
                user.setAlertGroup(sb.toString());
            }
        }

        return Result.success(user);
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public Result<List<User>> queryAllGeneralUsers(User loginUser) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<User> userList = userMapper.queryAllGeneralUser();

        return Result.success(userList);
    }

    /**
     * query user list
     *
     * @param loginUser login user
     * @return user list
     */
    @Override
    public Result<List<User>> queryUserList(User loginUser) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<User> userList = userMapper.selectList(null);

        return Result.success(userList);
    }

    /**
     * verify user name exists
     *
     * @param userName user name
     * @return true if user name not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyUserName(String userName) {

        Result<Object> result = new Result<>();
        User user = userMapper.queryByUserNameAccurately(userName);
        if (user != null) {
            putMsg(result, Status.USER_NAME_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * unauthorized user
     *
     * @param loginUser login user
     * @param alertgroupId alert group id
     * @return unauthorize result code
     */
    @Override
    public Result<List<User>> unauthorizedUser(User loginUser, Integer alertgroupId) {

        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<User> userList = userMapper.selectList(null);
        List<User> resultUsers = new ArrayList<>();
        Set<User> userSet = null;
        if (userList != null && !userList.isEmpty()) {
            userSet = new HashSet<>(userList);

            List<User> authedUserList = userMapper.queryUserListByAlertGroupId(alertgroupId);

            Set<User> authedUserSet = null;
            if (authedUserList != null && !authedUserList.isEmpty()) {
                authedUserSet = new HashSet<>(authedUserList);
                userSet.removeAll(authedUserSet);
            }
            resultUsers = new ArrayList<>(userSet);
        }

        return Result.success(resultUsers);
    }

    /**
     * authorized user
     *
     * @param loginUser login user
     * @param alertgroupId alert group id
     * @return authorized result code
     */
    @Override
    public Result<List<User>> authorizedUser(User loginUser, Integer alertgroupId) {
        //only admin can operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        List<User> userList = userMapper.queryUserListByAlertGroupId(alertgroupId);

        return Result.success(userList);
    }

    /**
     * @param tenantId tenant id
     * @return true if tenant exists, otherwise return false
     */
    private boolean checkTenantExists(int tenantId) {
        return tenantMapper.queryById(tenantId) != null;
    }

    /**
     * @return if check failed return the field, otherwise return null
     */
    private String checkUserParams(String userName, String password, String email, String phone) {

        String msg = null;
        if (!CheckUtils.checkUserName(userName)) {

            msg = userName;
        } else if (!CheckUtils.checkPassword(password)) {

            msg = password;
        } else if (!CheckUtils.checkEmail(email)) {

            msg = email;
        } else if (!CheckUtils.checkPhone(phone)) {

            msg = phone;
        }

        return msg;
    }

    /**
     * copy resource files
     *
     * @param resourceComponent resource component
     * @param srcBasePath src base path
     * @param dstBasePath dst base path
     * @throws IOException io exception
     */
    private void copyResourceFiles(ResourceComponent resourceComponent, String srcBasePath, String dstBasePath) throws IOException {
        List<ResourceComponent> components = resourceComponent.getChildren();

        if (CollectionUtils.isNotEmpty(components)) {
            for (ResourceComponent component : components) {
                // verify whether exist
                if (!HadoopUtils.getInstance().exists(String.format("%s/%s", srcBasePath, component.getFullName()))) {
                    logger.error("resource file: {} not exist,copy error", component.getFullName());
                    throw new ServiceException(Status.RESOURCE_NOT_EXIST);
                }

                if (!component.isDirctory()) {
                    // copy it to dst
                    HadoopUtils.getInstance().copy(String.format("%s/%s", srcBasePath, component.getFullName()), String.format("%s/%s", dstBasePath, component.getFullName()), false, true);
                    continue;
                }

                if (CollectionUtils.isEmpty(component.getChildren())) {
                    // if not exist,need create it
                    if (!HadoopUtils.getInstance().exists(String.format("%s/%s", dstBasePath, component.getFullName()))) {
                        HadoopUtils.getInstance().mkdir(String.format("%s/%s", dstBasePath, component.getFullName()));
                    }
                } else {
                    copyResourceFiles(component, srcBasePath, dstBasePath);
                }
            }
        }
    }

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
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<User> registerUser(String userName, String userPassword, String repeatPassword, String email) {

        //check user params
        String msg = this.checkUserParams(userName, userPassword, email, "");

        if (!StringUtils.isEmpty(msg)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, msg);
        }

        if (!userPassword.equals(repeatPassword)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, "two passwords are not same");
        }
        User user = createUser(userName, userPassword, email, 1, "", "", Flag.NO.ordinal());
        return Result.success(user);
    }

    /**
     * activate user, only system admin have permission, change user state code 0 to 1
     *
     * @param loginUser login user
     * @param userName user name
     * @return create result code
     */
    @Override
    public Result<User> activateUser(User loginUser, String userName) {

        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        if (!CheckUtils.checkUserName(userName)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
        }

        User user = userMapper.queryByUserNameAccurately(userName);

        if (user == null) {
            return Result.errorWithArgs(Status.USER_NOT_EXIST, userName);
        }

        if (user.getState() != Flag.NO.ordinal()) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, userName);
        }

        user.setState(Flag.YES.ordinal());
        Date now = new Date();
        user.setUpdateTime(now);
        userMapper.updateById(user);
        User responseUser = userMapper.queryByUserNameAccurately(userName);
        return Result.success(responseUser);
    }

    /**
     * activate user, only system admin have permission, change users state code 0 to 1
     *
     * @param loginUser login user
     * @param userNames user name
     * @return create result code
     */
    @Override
    public Result<Map<String, Object>> batchActivateUser(User loginUser, List<String> userNames) {

        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        int totalSuccess = 0;
        List<String> successUserNames = new ArrayList<>();
        Map<String, Object> successRes = new HashMap<>();
        int totalFailed = 0;
        List<Map<String, String>> failedInfo = new ArrayList<>();
        Map<String, Object> failedRes = new HashMap<>();
        for (String userName : userNames) {
            Result<User> tmpResult = activateUser(loginUser, userName);
            if (tmpResult.getCode() != Status.SUCCESS.getCode()) {
                totalFailed++;
                Map<String, String> failedBody = new HashMap<>();
                failedBody.put("userName", userName);
                failedBody.put("msg", tmpResult.getMsg());
                failedInfo.add(failedBody);
            } else {
                totalSuccess++;
                successUserNames.add(userName);
            }
        }
        successRes.put("sum", totalSuccess);
        successRes.put("userName", successUserNames);
        failedRes.put("sum", totalFailed);
        failedRes.put("info", failedInfo);
        Map<String, Object> res = new HashMap<>();
        res.put("success", successRes);
        res.put("failed", failedRes);
        return Result.success(res);
    }
}
