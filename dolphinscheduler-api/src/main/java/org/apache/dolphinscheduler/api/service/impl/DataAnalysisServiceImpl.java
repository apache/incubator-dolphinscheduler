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
import org.apache.dolphinscheduler.api.dto.CommandStateCount;
import org.apache.dolphinscheduler.api.dto.DefineUserDto;
import org.apache.dolphinscheduler.api.dto.TaskCountDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DataAnalysisService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TriFunction;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;
import org.apache.dolphinscheduler.dao.entity.ExecuteStatusCount;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * data analysis service impl
 */
@Service
public class DataAnalysisServiceImpl extends BaseServiceImpl implements DataAnalysisService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ProcessService processService;

    /**
     * statistical task instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return task state count data
     */
    @Override
    public Result<TaskCountDto> countTaskStateByProject(User loginUser, int projectId, String startDate, String endDate) {

        return countStateByProject(
                loginUser,
                projectId,
                startDate,
                endDate,
            (start, end, projectIds) -> this.taskInstanceMapper.countTaskInstanceStateByUser(start, end, projectIds));
    }

    /**
     * statistical process instance status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return process instance state count data
     */
    @Override
    public Result<TaskCountDto> countProcessInstanceStateByProject(User loginUser, int projectId, String startDate, String endDate) {
        Result<TaskCountDto> result = this.countStateByProject(
                loginUser,
                projectId,
                startDate,
                endDate, (start, end, projectIds) -> this.processInstanceMapper.countInstanceStateByUser(start, end, projectIds));
        // process state count needs to remove state of forced success
        if (null != result.getData()) {
            result.getData().removeStateFromCountList(ExecutionStatus.FORCED_SUCCESS);
        }
        return result;
    }

    private Result<TaskCountDto> countStateByProject(User loginUser, int projectId, String startDate, String endDate
            , TriFunction<Date, Date, Integer[], List<ExecuteStatusCount>> instanceStateCounter) {
        CheckParamResult checkResult = checkProject(loginUser, projectId);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        Date start = null;
        Date end = null;
        if (StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)) {
            start = DateUtils.getScheduleDate(startDate);
            end = DateUtils.getScheduleDate(endDate);
            if (Objects.isNull(start) || Objects.isNull(end)) {
                putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return Result.error(checkResult);
            }
        }

        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        List<ExecuteStatusCount> processInstanceStateCounts =
                instanceStateCounter.apply(start, end, projectIdArray);

        if (processInstanceStateCounts != null) {
            TaskCountDto taskCountResult = new TaskCountDto(processInstanceStateCounts);
            return Result.success(taskCountResult);
        }
        return Result.success(null);
    }


    /**
     * statistics the process definition quantities of certain person
     *
     * @param loginUser login user
     * @param projectId project id
     * @return definition count data
     */
    @Override
    public Result<DefineUserDto> countDefinitionByUser(User loginUser, int projectId) {

        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        List<DefinitionGroupByUser> defineGroupByUsers = processDefinitionMapper.countDefinitionGroupByUser(
                loginUser.getId(), projectIdArray, isAdmin(loginUser));

        DefineUserDto dto = new DefineUserDto(defineGroupByUsers);
        return Result.success(dto);
    }


    /**
     * statistical command status data
     *
     * @param loginUser login user
     * @param projectId project id
     * @param startDate start date
     * @param endDate end date
     * @return command state count data
     */
    @Override
    public Result<List<CommandStateCount>> countCommandState(User loginUser, int projectId, String startDate, String endDate) {

        CheckParamResult checkResult = checkProject(loginUser, projectId);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        /**
         * find all the task lists in the project under the user
         * statistics based on task status execution, failure, completion, wait, total
         */
        Date start = null;
        if (StringUtils.isNotEmpty(startDate)) {
            start = DateUtils.getScheduleDate(startDate);
            if (Objects.isNull(start)) {
                putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return Result.error(checkResult);
            }
        }
        Date end = null;
        if (StringUtils.isNotEmpty(endDate)) {
            end = DateUtils.getScheduleDate(endDate);
            if (Objects.isNull(end)) {
                putMsg(checkResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
                return Result.error(checkResult);
            }
        }

        Integer[] projectIdArray = getProjectIdsArrays(loginUser, projectId);
        // count normal command state
        Map<CommandType, Integer> normalCountCommandCounts = commandMapper.countCommandState(loginUser.getId(), start, end, projectIdArray)
                .stream()
                .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        // count error command state
        Map<CommandType, Integer> errorCommandCounts = errorCommandMapper.countCommandState(start, end, projectIdArray)
                .stream()
                .collect(Collectors.toMap(CommandCount::getCommandType, CommandCount::getCount));

        List<CommandStateCount> list = Arrays.stream(CommandType.values())
                .map(commandType -> new CommandStateCount(
                        errorCommandCounts.getOrDefault(commandType, 0),
                        normalCountCommandCounts.getOrDefault(commandType, 0),
                        commandType)
                ).collect(Collectors.toList());

        return Result.success(list);
    }

    private Integer[] getProjectIdsArrays(User loginUser, int projectId) {
        List<Integer> projectIds = new ArrayList<>();
        if (projectId != 0) {
            projectIds.add(projectId);
        } else if (loginUser.getUserType() == UserType.GENERAL_USER) {
            projectIds = processService.getProjectIdListHavePerm(loginUser.getId());
            if (projectIds.isEmpty()) {
                projectIds.add(0);
            }
        }
        return projectIds.toArray(new Integer[0]);
    }

    /**
     * count queue state
     *
     * @param loginUser login user
     * @param projectId project id
     * @return queue state count data
     */
    @Override
    public Result<Map<String, Integer>> countQueueState(User loginUser, int projectId) {

        CheckParamResult checkResult = checkProject(loginUser, projectId);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        //TODO need to add detail data info, and use a specific class to replace the map
        Map<String, Integer> dataMap = new HashMap<>();
        dataMap.put("taskQueue", 0);
        dataMap.put("taskKill", 0);
        return Result.success(dataMap);
    }

    private CheckParamResult checkProject(User loginUser, int projectId) {
        if (projectId != 0) {
            Project project = projectMapper.selectById(projectId);
            return projectService.hasProjectAndPerm(loginUser, project);
        }
        return new CheckParamResult(Status.SUCCESS);
    }

}
