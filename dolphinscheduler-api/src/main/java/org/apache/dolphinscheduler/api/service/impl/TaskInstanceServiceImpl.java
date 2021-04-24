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
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * task instance service impl
 */
@Service
public class TaskInstanceServiceImpl extends BaseServiceImpl implements TaskInstanceService {

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProcessService processService;

    @Autowired
    TaskInstanceMapper taskInstanceMapper;

    @Autowired
    ProcessInstanceService processInstanceService;

    @Autowired
    UsersService usersService;

    /**
     * query task list by project, process instance, task name, task start time, task end time, task status, keyword paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param searchVal search value
     * @param taskName task name
     * @param stateType state type
     * @param host host
     * @param startDate start time
     * @param endDate end time
     * @param pageNo page number
     * @param pageSize page size
     * @return task list page
     */
    @Override
    public Result<PageListVO<Map<String, Object>>> queryTaskListPaging(User loginUser, String projectName,
                                                                Integer processInstanceId, String processInstanceName, String taskName, String executorName, String startDate,
                                                                String endDate, String searchVal, ExecutionStatus stateType, String host,
                                                                Integer pageNo, Integer pageSize) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        int[] statusArray = null;
        if (stateType != null) {
            statusArray = new int[]{stateType.ordinal()};
        }

        CheckParamResultWithInfo<Map<String, Date>> checkParamResultWithInfo = checkAndParseDateParameters(startDate, endDate);
        if (!Status.SUCCESS.equals(checkParamResultWithInfo.getStatus())) {
            return Result.error(checkParamResultWithInfo);
        }
        Date start = checkParamResultWithInfo.getInfo().get(Constants.START_TIME);
        Date end = checkParamResultWithInfo.getInfo().get(Constants.END_TIME);

        Page<TaskInstance> page = new Page<>(pageNo, pageSize);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(pageNo, pageSize);
        int executorId = usersService.getUserIdByName(executorName);

        IPage<TaskInstance> taskInstanceIPage = taskInstanceMapper.queryTaskInstanceListPaging(
                page, project.getId(), processInstanceId, processInstanceName, searchVal, taskName, executorId, statusArray, host, start, end
        );
        Set<String> exclusionSet = new HashSet<>();
        exclusionSet.add(Constants.CLASS);
        exclusionSet.add("taskJson");
        List<TaskInstance> taskInstanceList = taskInstanceIPage.getRecords();

        for (TaskInstance taskInstance : taskInstanceList) {
            taskInstance.setDuration(DateUtils.format2Duration(taskInstance.getStartTime(), taskInstance.getEndTime()));
            User executor = usersService.queryUser(taskInstance.getExecutorId());
            if (null != executor) {
                taskInstance.setExecutorName(executor.getUserName());
            }
        }
        pageInfo.setTotalCount((int) taskInstanceIPage.getTotal());
        pageInfo.setLists(CollectionUtils.getListByExclusion(taskInstanceIPage.getRecords(), exclusionSet));

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * change one task instance's state from failure to forced success
     *
     * @param loginUser login user
     * @param projectName project name
     * @param taskInstanceId task instance id
     * @return the result code and msg
     */
    @Override
    public Result<Void> forceTaskSuccess(User loginUser, String projectName, Integer taskInstanceId) {
        Project project = projectMapper.queryByName(projectName);

        // check user auth
        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        // check whether the task instance can be found
        TaskInstance task = taskInstanceMapper.selectById(taskInstanceId);
        if (task == null) {
            return Result.error(Status.TASK_INSTANCE_NOT_FOUND);
        }

        // check whether the task instance state type is failure or cancel
        if (!task.getState().typeIsFailure() && !task.getState().typeIsCancel()) {
            return Result.errorWithArgs(Status.TASK_INSTANCE_STATE_OPERATION_ERROR, taskInstanceId, task.getState().toString());
        }

        // change the state of the task instance
        task.setState(ExecutionStatus.FORCED_SUCCESS);
        int changedNum = taskInstanceMapper.updateById(task);
        if (changedNum > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.FORCE_TASK_SUCCESS_ERROR);
        }
    }
}
