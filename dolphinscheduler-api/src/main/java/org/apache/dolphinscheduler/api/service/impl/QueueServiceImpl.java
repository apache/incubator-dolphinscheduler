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
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.BooleanUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * queue service impl
 */
@Service
public class QueueServiceImpl extends BaseServiceImpl implements QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * query queue list
     *
     * @param loginUser login user
     * @return queue list
     */
    @Override
    public Result<List<Queue>> queryList(User loginUser) {
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<Queue> queueList = queueMapper.selectList(null);

        return Result.success(queueList);
    }

    /**
     * query queue list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return queue list
     */
    @Override
    public Result<PageListVO<Queue>> queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        Page<Queue> page = new Page<>(pageNo, pageSize);

        IPage<Queue> queueList = queueMapper.queryQueuePaging(page, searchVal);

        Integer count = (int) queueList.getTotal();
        PageInfo<Queue> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount(count);
        pageInfo.setLists(queueList.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * create queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param queueName queue name
     * @return create result
     */
    @Override
    public Result<Void> createQueue(User loginUser, String queue, String queueName) {
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        if (StringUtils.isEmpty(queue)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        }

        if (StringUtils.isEmpty(queueName)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        }

        if (checkQueueNameExist(queueName)) {
            return Result.errorWithArgs(Status.QUEUE_NAME_EXIST, queueName);
        }

        if (checkQueueExist(queue)) {
            return Result.errorWithArgs(Status.QUEUE_VALUE_EXIST, queue);
        }

        Queue queueObj = new Queue();
        Date now = new Date();

        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setCreateTime(now);
        queueObj.setUpdateTime(now);

        queueMapper.insert(queueObj);

        return Result.success(null);
    }

    /**
     * update queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param id queue id
     * @param queueName queue name
     * @return update result code
     */
    @Override
    public Result<Void> updateQueue(User loginUser, int id, String queue, String queueName) {
        if (isNotAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        if (StringUtils.isEmpty(queue)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        }

        if (StringUtils.isEmpty(queueName)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        }

        Queue queueObj = queueMapper.selectById(id);
        if (queueObj == null) {
            return Result.errorWithArgs(Status.QUEUE_NOT_EXIST, id);
        }

        // whether queue value or queueName is changed
        if (queue.equals(queueObj.getQueue()) && queueName.equals(queueObj.getQueueName())) {
            return Result.error(Status.NEED_NOT_UPDATE_QUEUE);
        }

        // check queue name is exist
        if (!queueName.equals(queueObj.getQueueName())
                && checkQueueNameExist(queueName)) {
            return Result.errorWithArgs(Status.QUEUE_NAME_EXIST, queueName);
        }

        // check queue value is exist
        if (!queue.equals(queueObj.getQueue()) && checkQueueExist(queue)) {
            return Result.errorWithArgs(Status.QUEUE_VALUE_EXIST, queue);
        }

        // check old queue using by any user
        if (checkIfQueueIsInUsing(queueObj.getQueueName(), queueName)) {
            //update user related old queue
            Integer relatedUserNums = userMapper.updateUserQueue(queueObj.getQueueName(), queueName);
            logger.info("old queue have related {} user, exec update user success.", relatedUserNums);
        }

        // update queue
        Date now = new Date();
        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setUpdateTime(now);

        queueMapper.updateById(queueObj);

        return Result.success(null);
    }

    /**
     * verify queue and queueName
     *
     * @param queue queue
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    @Override
    public Result<Void> verifyQueue(String queue, String queueName) {

        if (StringUtils.isEmpty(queue)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        }

        if (StringUtils.isEmpty(queueName)) {
            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        }

        if (checkQueueNameExist(queueName)) {
            return Result.errorWithArgs(Status.QUEUE_NAME_EXIST, queueName);
        }

        if (checkQueueExist(queue)) {
            return Result.errorWithArgs(Status.QUEUE_VALUE_EXIST, queue);
        }

        return Result.success(null);
    }

    /**
     * check queue exist
     * if exists return true，not exists return false
     * check queue exist
     *
     * @param queue queue
     * @return true if the queue not exists, otherwise return false
     */
    private boolean checkQueueExist(String queue) {
        return BooleanUtils.isTrue(queueMapper.existQueue(queue, null));
    }

    /**
     * check queue name exist
     * if exists return true，not exists return false
     *
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    private boolean checkQueueNameExist(String queueName) {
        return BooleanUtils.isTrue(queueMapper.existQueue(null, queueName));
    }

    /**
     * check old queue name using by any user
     * if need to update user
     *
     * @param oldQueue old queue name
     * @param newQueue new queue name
     * @return true if need to update user
     */
    private boolean checkIfQueueIsInUsing (String oldQueue, String newQueue) {
        return !oldQueue.equals(newQueue) && BooleanUtils.isTrue(userMapper.existUser(oldQueue));
    }

}
