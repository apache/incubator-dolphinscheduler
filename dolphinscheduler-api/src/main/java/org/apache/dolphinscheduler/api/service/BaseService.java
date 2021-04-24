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
import org.apache.dolphinscheduler.api.dto.CheckParamResultWithInfo;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * base service
 */
public interface BaseService {

    /**
     * check admin
     *
     * @param user input user
     * @return ture if administrator, otherwise return false
     */
    boolean isAdmin(User user);

    /**
     * isNotAdmin
     *
     * @param loginUser login user
     * @return true if not administrator, otherwise false
     */
    boolean isNotAdmin(User loginUser);

    /**
     * put message to map
     *
     * @param checkParamResult result code
     * @param status status
     * @param statusParams status message
     */
    void putMsg(CheckParamResult checkParamResult, Status status, Object... statusParams);

    /**
     * put message to result object
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    void putMsg(Result<Object> result, Status status, Object... statusParams);

    /**
     * check
     *
     * @param result result
     * @param bool bool
     * @param userNoOperationPerm status
     * @return check result
     */
    boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm);

    /**
     * create tenant dir if not exists
     *
     * @param tenantCode tenant code
     * @throws IOException if hdfs operation exception
     */
    void createTenantDirIfNotExists(String tenantCode) throws IOException;

    /**
     * has perm
     *
     * @param operateUser operate user
     * @param createUserId create user id
     */
    boolean hasPerm(User operateUser, int createUserId);

    /**
     * check and parse date parameters
     *
     * @param startDateStr start date string
     * @param endDateStr end date string
     * @return map<status, startDate, endDate>
     */
    CheckParamResultWithInfo<Map<String, Date>> checkAndParseDateParameters(String startDateStr, String endDateStr);
}
