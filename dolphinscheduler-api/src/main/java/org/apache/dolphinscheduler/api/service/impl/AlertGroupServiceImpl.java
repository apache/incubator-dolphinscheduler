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
import org.apache.dolphinscheduler.api.service.AlertGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.utils.BooleanUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert group service impl
 */
@Service
public class AlertGroupServiceImpl extends BaseServiceImpl implements AlertGroupService {

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * query alert group list
     *
     * @return alert group list
     */
    @Override
    public Result<List<AlertGroup>> queryAlertgroup() {

        List<AlertGroup> alertGroups = alertGroupMapper.queryAllGroupList();

        return Result.success(alertGroups);
    }

    /**
     * paging query alarm group list
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return alert group list page
     */
    @Override
    public Result<PageListVO<AlertGroup>> listPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Page<AlertGroup> page = new Page<>(pageNo, pageSize);
        IPage<AlertGroup> alertGroupIPage = alertGroupMapper.queryAlertGroupPage(
                page, searchVal);
        PageInfo<AlertGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) alertGroupIPage.getTotal());
        pageInfo.setLists(alertGroupIPage.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * create alert group
     *
     * @param loginUser login user
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return create result code
     */
    @Override
    public Result<Void> createAlertgroup(User loginUser, String groupName, String desc, String alertInstanceIds) {
        //only admin can operate
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        AlertGroup alertGroup = new AlertGroup();
        Date now = new Date();

        alertGroup.setGroupName(groupName);
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        alertGroup.setDescription(desc);
        alertGroup.setCreateTime(now);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());

        // insert
        int insert = alertGroupMapper.insert(alertGroup);

        if (insert > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.CREATE_ALERT_GROUP_ERROR);
        }
    }

    /**
     * updateProcessInstance alert group
     *
     * @param loginUser login user
     * @param id alert group id
     * @param groupName group name
     * @param desc description
     * @param alertInstanceIds alertInstanceIds
     * @return update result code
     */
    @Override
    public Result<Void> updateAlertgroup(User loginUser, int id, String groupName, String desc, String alertInstanceIds) {

        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        AlertGroup alertGroup = alertGroupMapper.selectById(id);

        if (alertGroup == null) {
            return Result.error(Status.ALERT_GROUP_NOT_EXIST);

        }

        Date now = new Date();

        if (StringUtils.isNotEmpty(groupName)) {
            alertGroup.setGroupName(groupName);
        }
        alertGroup.setDescription(desc);
        alertGroup.setUpdateTime(now);
        alertGroup.setCreateUserId(loginUser.getId());
        alertGroup.setAlertInstanceIds(alertInstanceIds);
        alertGroupMapper.updateById(alertGroup);
        return Result.success(null);
    }

    /**
     * delete alert group by id
     *
     * @param loginUser login user
     * @param id alert group id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> delAlertgroupById(User loginUser, int id) {

        //only admin can operate
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }
        //check exist
        AlertGroup alertGroup = alertGroupMapper.selectById(id);
        if (alertGroup == null) {
            return Result.error(Status.ALERT_GROUP_NOT_EXIST);
        }
        alertGroupMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * verify group name exists
     *
     * @param groupName group name
     * @return check result code
     */
    @Override
    public boolean existGroupName(String groupName) {
        return BooleanUtils.isTrue(alertGroupMapper.existGroupName(groupName));
    }
}
