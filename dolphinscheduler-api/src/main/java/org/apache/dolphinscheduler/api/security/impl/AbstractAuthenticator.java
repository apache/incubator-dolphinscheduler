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

package org.apache.dolphinscheduler.api.security.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.dao.entity.Session;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAuthenticator implements Authenticator {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticator.class);

    @Autowired
    private UsersService userService;
    @Autowired
    private SessionService sessionService;

    /**
     * user login and return user in db
     *
     * @param userId user identity field
     * @param password user login password
     * @param extra extra user login field
     * @return user object in database
     */
    public abstract User login(String userId, String password, String extra);

    @Override
    public Result<Map<String, String>> authenticate(String userId, String password, String extra) {
        Result<Map<String, String>> result = new Result<>();
        User user = login(userId, password, extra);
        if (user == null) {
            result.setCode(Status.USER_NAME_PASSWD_ERROR.getCode());
            result.setMsg(Status.USER_NAME_PASSWD_ERROR.getMsg());
            return result;
        }

        // check user state
        if (user.getState() == Flag.NO.ordinal()) {
            result.setCode(Status.USER_DISABLED.getCode());
            result.setMsg(Status.USER_DISABLED.getMsg());
            return result;
        }

        // create session
        String sessionId = sessionService.createSession(user, extra);
        if (sessionId == null) {
            result.setCode(Status.LOGIN_SESSION_FAILED.getCode());
            result.setMsg(Status.LOGIN_SESSION_FAILED.getMsg());
            return result;
        }
        logger.info("sessionId : {}", sessionId);
        result.setData(Collections.singletonMap(Constants.SESSION_ID, sessionId));
        result.setCode(Status.SUCCESS.getCode());
        result.setMsg(Status.LOGIN_SUCCESS.getMsg());
        return result;
    }

    @Override
    public User getAuthUser(HttpServletRequest request) {
        Session session = sessionService.getSession(request);
        if (session == null) {
            logger.info("session info is null ");
            return null;
        }
        //get user object from session
        return userService.queryUser(session.getUserId());
    }

}
