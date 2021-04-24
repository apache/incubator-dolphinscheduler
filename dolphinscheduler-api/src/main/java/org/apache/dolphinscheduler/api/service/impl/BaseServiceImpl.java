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
import org.apache.dolphinscheduler.api.dto.CheckParamResultWithInfo;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * base service impl
 */
public class BaseServiceImpl implements BaseService {

    /**
     * check admin
     *
     * @param user input user
     * @return ture if administrator, otherwise return false
     */
    @Override
    public boolean isAdmin(User user) {
        return user.getUserType() == UserType.ADMIN_USER;
    }

    /**
     * isNotAdmin
     *
     * @param loginUser login user
     * @param result result code
     * @return true if not administrator, otherwise false
     */
    @Override
    public boolean isNotAdmin(User loginUser) {
        //only admin can operate
        return !isAdmin(loginUser);
    }

    /**
     * put message to map
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    @Override
    public void putMsg(CheckParamResult checkParamResult, Status status, Object... statusParams) {
        checkParamResult.setStatus(status);
        if (statusParams != null && statusParams.length > 0) {
            checkParamResult.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            checkParamResult.setMsg(status.getMsg());
        }
    }

    /**
     * put message to result object
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    @Override
    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    /**
     * check
     *
     * @param result result
     * @param bool bool
     * @param userNoOperationPerm status
     * @return check result
     */
    @Override
    public boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm) {
        // only admin can operate
        if (bool) {
            result.put(Constants.STATUS, userNoOperationPerm);
            result.put(Constants.MSG, userNoOperationPerm.getMsg());
            return true;
        }
        return false;
    }

    /**
     * create tenant dir if not exists
     *
     * @param tenantCode tenant code
     * @throws IOException if hdfs operation exception
     */
    @Override
    public void createTenantDirIfNotExists(String tenantCode) throws IOException {
        String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
        String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
        // init resource path and udf path
        HadoopUtils.getInstance().mkdir(resourcePath);
        HadoopUtils.getInstance().mkdir(udfsPath);
    }

    /**
     * has perm
     *
     * @param operateUser operate user
     * @param createUserId create user id
     */
    @Override
    public boolean hasPerm(User operateUser, int createUserId) {
        return operateUser.getId() == createUserId || isAdmin(operateUser);
    }

    /**
     * check and parse date parameters
     *
     * @param startDateStr start date string
     * @param endDateStr end date string
     * @return map<status, startDate, endDate>
     */
    @Override
    public CheckParamResultWithInfo<Map<String, Date>> checkAndParseDateParameters(String startDateStr, String endDateStr) {
        CheckParamResultWithInfo<Map<String, Date>> checkResult = new CheckParamResultWithInfo<>();
        Date start = null;
        if (StringUtils.isNotEmpty(startDateStr)) {
            start = DateUtils.getScheduleDate(startDateStr);
            if (Objects.isNull(start)) {
                putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return checkResult;
            }
        }

        Date end = null;
        if (StringUtils.isNotEmpty(endDateStr)) {
            end = DateUtils.getScheduleDate(endDateStr);
            if (Objects.isNull(end)) {
                putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return checkResult;
            }
        }

        putMsg(checkResult, Status.SUCCESS);
        // todo improve
        Map<String, Date> timeParseMap = new HashMap<>();
        timeParseMap.put(Constants.START_TIME, start);
        timeParseMap.put(Constants.END_TIME, end);
        checkResult.setInfo(timeParseMap);
        return checkResult;
    }

}
