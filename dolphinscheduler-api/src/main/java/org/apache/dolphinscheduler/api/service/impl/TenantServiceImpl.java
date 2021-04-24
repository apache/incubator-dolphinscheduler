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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.BooleanUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * tenant service impl
 */
@Service
public class TenantServiceImpl extends BaseServiceImpl implements TenantService {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * create tenant
     *
     * @param loginUser  login user
     * @param tenantCode tenant code
     * @param queueId    queue id
     * @param desc       description
     * @return create result code
     * @throws Exception exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createTenant(User loginUser,
                                     String tenantCode,
                                     int queueId,
                                     String desc) throws Exception {

        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        if (!RegexUtils.isValidLinuxUserName(tenantCode)) {
            return Result.error(Status.CHECK_OS_TENANT_CODE_ERROR);
        }

        if (checkTenantExists(tenantCode)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, tenantCode);
        }

        Tenant tenant = new Tenant();
        Date now = new Date();
        tenant.setTenantCode(tenantCode);
        tenant.setQueueId(queueId);
        tenant.setDescription(desc);
        tenant.setCreateTime(now);
        tenant.setUpdateTime(now);

        // save
        tenantMapper.insert(tenant);

        // if hdfs startup
        if (PropertyUtils.getResUploadStartupState()) {
            createTenantDirIfNotExists(tenantCode);
        }

        return Result.success(null);
    }

    /**
     * query tenant list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return tenant list page
     */
    @Override
    public Result<PageListVO<Tenant>> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Page<Tenant> page = new Page<>(pageNo, pageSize);
        IPage<Tenant> tenantIPage = tenantMapper.queryTenantPaging(page, searchVal);
        PageInfo<Tenant> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) tenantIPage.getTotal());
        pageInfo.setLists(tenantIPage.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * updateProcessInstance tenant
     *
     * @param loginUser  login user
     * @param id         tennat id
     * @param tenantCode tennat code
     * @param queueId    queue id
     * @param desc       description
     * @return update result code
     * @throws Exception exception
     */
    @Override
    public Result<Void> updateTenant(User loginUser, int id, String tenantCode, int queueId,
                                     String desc) throws Exception {

        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Tenant tenant = tenantMapper.queryById(id);

        if (tenant == null) {
            return Result.error(Status.TENANT_NOT_EXIST);
        }

        // updateProcessInstance tenant
        /**
         * if the tenant code is modified, the original resource needs to be copied to the new tenant.
         */
        if (!tenant.getTenantCode().equals(tenantCode)) {
            if (checkTenantExists(tenantCode)) {
                // if hdfs startup
                if (PropertyUtils.getResUploadStartupState()) {
                    String resourcePath = HadoopUtils.getHdfsDataBasePath() + "/" + tenantCode + "/resources";
                    String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
                    //init hdfs resource
                    HadoopUtils.getInstance().mkdir(resourcePath);
                    HadoopUtils.getInstance().mkdir(udfsPath);
                }
            } else {
                return Result.error(Status.OS_TENANT_CODE_HAS_ALREADY_EXISTS);
            }
        }

        Date now = new Date();

        if (StringUtils.isNotEmpty(tenantCode)) {
            tenant.setTenantCode(tenantCode);
        }

        if (queueId != 0) {
            tenant.setQueueId(queueId);
        }
        tenant.setDescription(desc);
        tenant.setUpdateTime(now);
        tenantMapper.updateById(tenant);

        return Result.success(null);
    }

    /**
     * delete tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @return delete result code
     * @throws Exception exception
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteTenantById(User loginUser, int id) throws Exception {

        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Tenant tenant = tenantMapper.queryById(id);
        if (tenant == null) {
            return Result.error(Status.TENANT_NOT_EXIST);
        }

        List<ProcessInstance> processInstances = getProcessInstancesByTenant(tenant);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            return Result.errorWithArgs(Status.DELETE_TENANT_BY_ID_FAIL, processInstances.size());
        }

        List<ProcessDefinition> processDefinitions =
                processDefinitionMapper.queryDefinitionListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(processDefinitions)) {
            return Result.errorWithArgs(Status.DELETE_TENANT_BY_ID_FAIL_DEFINES, processDefinitions.size());
        }

        List<User> userList = userMapper.queryUserListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(userList)) {
            return Result.errorWithArgs(Status.DELETE_TENANT_BY_ID_FAIL_USERS, userList.size());
        }

        // if resource upload startup
        if (PropertyUtils.getResUploadStartupState()) {
            String tenantPath = HadoopUtils.getHdfsDataBasePath() + "/" + tenant.getTenantCode();

            if (HadoopUtils.getInstance().exists(tenantPath)) {
                HadoopUtils.getInstance().delete(tenantPath, true);
            }
        }

        tenantMapper.deleteById(id);
        processInstanceMapper.updateProcessInstanceByTenantId(id, -1);
        return Result.success(null);
    }

    private List<ProcessInstance> getProcessInstancesByTenant(Tenant tenant) {
        return processInstanceMapper.queryByTenantIdAndStatus(tenant.getId(), Constants.NOT_TERMINATED_STATES);
    }

    /**
     * query tenant list
     *
     * @param tenantCode tenant code
     * @return tenant list
     */
    public Result<List<Tenant>> queryTenantList(String tenantCode) {

        List<Tenant> resourceList = tenantMapper.queryByTenantCode(tenantCode);
        if (CollectionUtils.isNotEmpty(resourceList)) {
            return Result.success(resourceList);
        } else {
            return Result.error(Status.TENANT_NOT_EXIST);
        }
    }

    /**
     * query tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    @Override
    public Result<List<Tenant>> queryTenantList(User loginUser) {

        List<Tenant> resourceList = tenantMapper.selectList(null);

        return Result.success(resourceList);
    }

    /**
     * verify tenant code
     *
     * @param tenantCode tenant code
     * @return true if tenant code can user, otherwise return false
     */
    @Override
    public Result<Void> verifyTenantCode(String tenantCode) {
        if (checkTenantExists(tenantCode)) {
            return Result.errorWithArgs(Status.OS_TENANT_CODE_EXIST, tenantCode);
        }
        return Result.success(null);
    }

    /**
     * check tenant exists
     *
     * @param tenantCode tenant code
     * @return ture if the tenant code exists, otherwise return false
     */
    private boolean checkTenantExists(String tenantCode) {
        Boolean existTenant = tenantMapper.existTenant(tenantCode);
        return BooleanUtils.isTrue(existTenant);
    }
}
