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
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * tenant service
 */
public interface TenantService {

    /**
     * create tenant
     *
     * @param loginUser login user
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param desc description
     * @return create result code
     * @throws Exception exception
     */
    Map<String, Object> createTenant(User loginUser,
                                     String tenantCode,
                                     int queueId,
                                     String desc) throws Exception;

    /**
     * query tenant list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return tenant list page
     */
    Map<String, Object> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * updateProcessInstance tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param desc description
     * @return update result code
     * @throws Exception exception
     */
    Map<String, Object> updateTenant(User loginUser, int id, String tenantCode, int queueId,
            String desc) throws Exception;

    /**
     * delete tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @return delete result code
     * @throws Exception exception
     */
    Map<String, Object> deleteTenantById(User loginUser, int id) throws Exception;

    /**
     * query tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    Map<String, Object> queryTenantList(User loginUser);

    /**
     * verify tenant code
     *
     * @param tenantCode tenant code
     * @return true if tenant code can user, otherwise return false
     */
    Result verifyTenantCode(String tenantCode);
}
