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

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.IP_IS_EMPTY;
import static org.apache.dolphinscheduler.api.enums.Status.SIGN_OUT_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.USER_LOGIN_FAILURE;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.security.Authenticator;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.utils.AuthUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.commons.httpclient.HttpStatus;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * login controller
 */
@Api(tags = "LOGIN_TAG")
@RestController
@RequestMapping("")
public class LoginController extends BaseController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private Authenticator authenticator;


    /**
     * login
     *
     * @param userName user name
     * @param userPassword user password
     * @param request request
     * @param response response
     * @return login result
     */
    @ApiOperation(value = "login", notes = "LOGIN_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "USER_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "userPassword", value = "USER_PASSWORD", required = true, dataType = "String")
    })
    @PostMapping(value = "/login")
    @ApiException(USER_LOGIN_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = {"userPassword", "request", "response"})
    public Result login(@RequestParam(value = "userName") String userName,
                        @RequestParam(value = "userPassword") String userPassword,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        //user name check
        if (StringUtils.isEmpty(userName)) {
            return error(Status.USER_NAME_NULL.getCode(),
                    Status.USER_NAME_NULL.getMsg());
        }

        // user ip check
        String ip = getClientIpAddress(request);
        if (StringUtils.isEmpty(ip)) {
            return error(IP_IS_EMPTY.getCode(), IP_IS_EMPTY.getMsg());
        }

        // verify username and password
        Result<Map<String, String>> result = authenticator.authenticate(userName, userPassword, ip);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            return result;
        }

        response.setStatus(HttpStatus.SC_OK);
        Map<String, String> cookieMap = result.getData();
        for (Map.Entry<String, String> cookieEntry : cookieMap.entrySet()) {
            Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        return result;
    }

    /**
     * sign out
     *
     * @param request request
     * @return sign out result
     */
    @ApiOperation(value = "signOut", notes = "SIGNOUT_NOTES")
    @PostMapping(value = "/signOut")
    @ApiException(SIGN_OUT_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = {"request"})
    public Result signOut(HttpServletRequest request) {
        String ip = getClientIpAddress(request);
        sessionService.signOut(ip, AuthUtils.getLoginUser());
        //clear session
        request.removeAttribute(Constants.SESSION_USER);
        return success();
    }
}
