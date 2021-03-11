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

package org.apache.dolphinscheduler.service.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.List;

import org.slf4j.Logger;

public class PermissionCheck<T> {
    /**
     * logger
     */
    private Logger logger;
    /**
     * Authorization Type
     */
    private AuthorizationType authorizationType;

    /**
     * Authorization Type
     */
    private ProcessService processService;

    /**
     * need check array
     */
    private T[] needChecks;

    /**
     * resource info
     */
    private List<ResourceInfo> resourceList;

    /**
     * user id
     */
    private int userId;

    /**
     * permission check
     *
     * @param authorizationType authorization type
     * @param processService process dao
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService) {
        this.authorizationType = authorizationType;
        this.processService = processService;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, T[] needChecks, int userId) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.needChecks = needChecks;
        this.userId = userId;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, T[] needChecks, int userId, Logger logger) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.needChecks = needChecks;
        this.userId = userId;
        this.logger = logger;
    }

    /**
     * permission check
     */
    public PermissionCheck(AuthorizationType authorizationType, ProcessService processService, List<ResourceInfo> resourceList, int userId, Logger logger) {
        this.authorizationType = authorizationType;
        this.processService = processService;
        this.resourceList = resourceList;
        this.userId = userId;
        this.logger = logger;
    }

    public AuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(AuthorizationType authorizationType) {
        this.authorizationType = authorizationType;
    }

    public ProcessService getProcessService() {
        return processService;
    }

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

    public T[] getNeedChecks() {
        return needChecks;
    }

    public void setNeedChecks(T[] needChecks) {
        this.needChecks = needChecks;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * has permission
     *
     * @return true if has permission
     */
    public boolean hasPermission() {
        try {
            checkPermission();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * check permission
     *
     * @throws ServiceException exception
     */
    public void checkPermission() throws ServiceException {
        if (this.needChecks.length > 0) {

            // get user type in order to judge whether the user is admin
            User user = processService.getUserById(userId);
            if (user == null) {
                logger.error("user id {} doesn't exist", userId);
                throw new ServiceException(String.format("user %s doesn't exist", userId));
            }
            if (user.getUserType() != UserType.ADMIN_USER) {
                List<T> unauthorizedList = processService.listUnauthorized(userId, needChecks, authorizationType);
                // if exist unauthorized resource
                if (CollectionUtils.isNotEmpty(unauthorizedList)) {
                    logger.error("user {} doesn't have permission of {}: {}", user.getUserName(), authorizationType.getDescp(), unauthorizedList);
                    throw new ServiceException(String.format("user %s doesn't have permission of %s %s", user.getUserName(), authorizationType.getDescp(), unauthorizedList.get(0)));
                }
            }
        }
    }

}
