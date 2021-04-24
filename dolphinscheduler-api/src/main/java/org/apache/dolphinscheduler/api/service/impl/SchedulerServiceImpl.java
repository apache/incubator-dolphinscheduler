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
import org.apache.dolphinscheduler.api.dto.ScheduleParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.ProcessScheduleJob;
import org.apache.dolphinscheduler.service.quartz.QuartzExecutors;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * scheduler service impl
 */
@Service
public class SchedulerServiceImpl extends BaseServiceImpl implements SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerServiceImpl.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    /**
     * save schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefineId process definition id
     * @param schedule scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @return create result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Schedule> insertSchedule(User loginUser, String projectName,
                                           Integer processDefineId,
                                           String schedule,
                                           WarningType warningType,
                                           int warningGroupId,
                                           FailureStrategy failureStrategy,
                                           Priority processInstancePriority,
                                           String workerGroup) {

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        CheckParamResult checkResult = projectService.hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check work flow define release state
        ProcessDefinition processDefinition = processService.findProcessDefineById(processDefineId);
        Result<Void> result = executorService.checkProcessDefinitionValid(processDefinition, processDefineId);
        if (result.getCode() != Status.SUCCESS.getCode()) {
            Result<Schedule> errorResult = new Result<>();
            errorResult.setCode(result.getCode());
            errorResult.setMsg(result.getMsg());
            return errorResult;
        }

        Schedule scheduleObj = new Schedule();
        Date now = new Date();

        scheduleObj.setProjectName(projectName);
        scheduleObj.setProcessDefinitionId(processDefinition.getId());
        scheduleObj.setProcessDefinitionName(processDefinition.getName());

        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
            logger.warn("The start time must not be the same as the end");
            return Result.error(Status.SCHEDULE_START_TIME_END_TIME_SAME);
        }
        scheduleObj.setStartTime(scheduleParam.getStartTime());
        scheduleObj.setEndTime(scheduleParam.getEndTime());
        if (!org.quartz.CronExpression.isValidExpression(scheduleParam.getCrontab())) {
            logger.error("{} verify failure", scheduleParam.getCrontab());

            return Result.errorWithArgs(Status.REQUEST_PARAMS_NOT_VALID_ERROR, scheduleParam.getCrontab());
        }
        scheduleObj.setCrontab(scheduleParam.getCrontab());
        scheduleObj.setWarningType(warningType);
        scheduleObj.setWarningGroupId(warningGroupId);
        scheduleObj.setFailureStrategy(failureStrategy);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());
        scheduleObj.setReleaseState(ReleaseState.OFFLINE);
        scheduleObj.setProcessInstancePriority(processInstancePriority);
        scheduleObj.setWorkerGroup(workerGroup);
        scheduleMapper.insert(scheduleObj);

        /**
         * updateProcessInstance receivers and cc by process definition id
         */
        processDefinition.setWarningGroupId(warningGroupId);
        processDefinitionMapper.updateById(processDefinition);

        // return scheduler object with ID
        return Result.success(scheduleMapper.selectById(scheduleObj.getId()));
    }

    /**
     * updateProcessInstance schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @param scheduleExpression scheduler
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param failureStrategy failure strategy
     * @param workerGroup worker group
     * @param processInstancePriority process instance priority
     * @param scheduleStatus schedule status
     * @return update result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> updateSchedule(User loginUser,
                                       String projectName,
                                       Integer id,
                                       String scheduleExpression,
                                       WarningType warningType,
                                       int warningGroupId,
                                       FailureStrategy failureStrategy,
                                       ReleaseState scheduleStatus,
                                       Priority processInstancePriority,
                                       String workerGroup) {

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        CheckParamResult checkResult = projectService.hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check schedule exists
        Schedule schedule = scheduleMapper.selectById(id);

        if (schedule == null) {
            return Result.errorWithArgs(Status.SCHEDULE_CRON_NOT_EXISTS, id);
        }

        ProcessDefinition processDefinition = processService.findProcessDefineById(schedule.getProcessDefinitionId());
        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, schedule.getProcessDefinitionId());
        }

        /**
         * scheduling on-line status forbid modification
         */
        if (schedule.getReleaseState() == ReleaseState.ONLINE) {
            return Result.error(Status.SCHEDULE_CRON_ONLINE_FORBID_UPDATE);
        }

        Date now = new Date();

        // updateProcessInstance param
        if (StringUtils.isNotEmpty(scheduleExpression)) {
            ScheduleParam scheduleParam = JSONUtils.parseObject(scheduleExpression, ScheduleParam.class);
            if (DateUtils.differSec(scheduleParam.getStartTime(), scheduleParam.getEndTime()) == 0) {
                logger.warn("The start time must not be the same as the end");
                return Result.error(Status.SCHEDULE_START_TIME_END_TIME_SAME);
            }
            schedule.setStartTime(scheduleParam.getStartTime());
            schedule.setEndTime(scheduleParam.getEndTime());
            if (!org.quartz.CronExpression.isValidExpression(scheduleParam.getCrontab())) {
                return Result.errorWithArgs(Status.SCHEDULE_CRON_CHECK_FAILED, scheduleParam.getCrontab());
            }
            schedule.setCrontab(scheduleParam.getCrontab());
        }

        if (warningType != null) {
            schedule.setWarningType(warningType);
        }

        schedule.setWarningGroupId(warningGroupId);

        if (failureStrategy != null) {
            schedule.setFailureStrategy(failureStrategy);
        }

        if (scheduleStatus != null) {
            schedule.setReleaseState(scheduleStatus);
        }
        schedule.setWorkerGroup(workerGroup);
        schedule.setUpdateTime(now);
        schedule.setProcessInstancePriority(processInstancePriority);
        scheduleMapper.updateById(schedule);

        /**
         * updateProcessInstance recipients and cc by process definition ID
         */
        processDefinition.setWarningGroupId(warningGroupId);

        processDefinitionMapper.updateById(processDefinition);

        return Result.success(null);
    }


    /**
     * set schedule online or offline
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id scheduler id
     * @param scheduleStatus schedule status
     * @return publish result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> setScheduleState(User loginUser,
                                         String projectName,
                                         Integer id,
                                         ReleaseState scheduleStatus) {

        Project project = projectMapper.queryByName(projectName);
        // check project auth
        CheckParamResult checkResult = projectService.hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        // check schedule exists
        Schedule scheduleObj = scheduleMapper.selectById(id);

        if (scheduleObj == null) {
            return Result.errorWithArgs(Status.SCHEDULE_CRON_NOT_EXISTS, id);
        }
        // check schedule release state
        if (scheduleObj.getReleaseState() == scheduleStatus) {
            logger.info("schedule release is already {},needn't to change schedule id: {} from {} to {}",
                    scheduleObj.getReleaseState(), scheduleObj.getId(), scheduleObj.getReleaseState(), scheduleStatus);
            return Result.errorWithArgs(Status.SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE, scheduleStatus);
        }
        ProcessDefinition processDefinition = processService.findProcessDefineById(scheduleObj.getProcessDefinitionId());
        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, scheduleObj.getProcessDefinitionId());
        }

        if (scheduleStatus == ReleaseState.ONLINE) {
            // check process definition release state
            if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
                logger.info("not release process definition id: {} , name : {}",
                        processDefinition.getId(), processDefinition.getName());
                return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_RELEASE, processDefinition.getName());
            }
            // check sub process definition release state
            List<Integer> subProcessDefineIds = new ArrayList<>();
            processService.recurseFindSubProcessId(scheduleObj.getProcessDefinitionId(), subProcessDefineIds);
            Integer[] idArray = subProcessDefineIds.toArray(new Integer[subProcessDefineIds.size()]);
            if (!subProcessDefineIds.isEmpty()) {
                List<ProcessDefinition> subProcessDefinitionList =
                        processDefinitionMapper.queryDefinitionListByIdList(idArray);
                if (subProcessDefinitionList != null && !subProcessDefinitionList.isEmpty()) {
                    for (ProcessDefinition subProcessDefinition : subProcessDefinitionList) {
                        /**
                         * if there is no online process, exit directly
                         */
                        if (subProcessDefinition.getReleaseState() != ReleaseState.ONLINE) {
                            logger.info("not release process definition id: {} , name : {}",
                                    subProcessDefinition.getId(), subProcessDefinition.getName());
                            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_RELEASE, subProcessDefinition.getId());
                        }
                    }
                }
            }
        }

        // check master server exists
        List<Server> masterServers = monitorService.getServerListFromZK(true);

        if (masterServers.isEmpty()) {
            return Result.error(Status.MASTER_NOT_EXISTS);
        }

        // set status
        scheduleObj.setReleaseState(scheduleStatus);

        scheduleMapper.updateById(scheduleObj);

        try {
            switch (scheduleStatus) {
                case ONLINE:
                    logger.info("Call master client set schedule online, project id: {}, flow id: {},host: {}", project.getId(), processDefinition.getId(), masterServers);
                    setSchedule(project.getId(), scheduleObj);
                    break;
                case OFFLINE:
                    logger.info("Call master client set schedule offline, project id: {}, flow id: {},host: {}", project.getId(), processDefinition.getId(), masterServers);
                    deleteSchedule(project.getId(), id);
                    break;
                default:
                    return Result.errorWithArgs(Status.SCHEDULE_STATUS_UNKNOWN, scheduleStatus.toString());
            }
        } catch (Exception e) {
            throw new ServiceException(scheduleStatus == ReleaseState.ONLINE ? "set online failure" : "set offline failure");
        }

        return Result.success(null);
    }

    /**
     * query schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefineId process definition id
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return schedule list page
     */
    @Override
    public Result<PageListVO<Schedule>> querySchedule(User loginUser, String projectName, Integer processDefineId, String searchVal, Integer pageNo, Integer pageSize) {

        Project project = projectMapper.queryByName(projectName);

        // check project auth
        CheckParamResult checkResult = projectService.hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        ProcessDefinition processDefinition = processService.findProcessDefineById(processDefineId);
        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, processDefineId);
        }
        Page<Schedule> page = new Page<>(pageNo, pageSize);
        IPage<Schedule> scheduleIPage = scheduleMapper.queryByProcessDefineIdPaging(
                page, processDefineId, searchVal
        );

        PageInfo<Schedule> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) scheduleIPage.getTotal());
        pageInfo.setLists(scheduleIPage.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * query schedule list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return schedule list
     */
    @Override
    public Result<List<Schedule>> queryScheduleList(User loginUser, String projectName) {
        Project project = projectMapper.queryByName(projectName);

        // check project auth
        CheckParamResult checkResult = projectService.hasProjectAndPerm(loginUser, project);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        List<Schedule> schedules = scheduleMapper.querySchedulerListByProjectName(projectName);

        return Result.success(schedules);
    }

    public void setSchedule(int projectId, Schedule schedule) {
        int scheduleId = schedule.getId();
        logger.info("set schedule, project id: {}, scheduleId: {}", projectId, scheduleId);

        Date startDate = schedule.getStartTime();
        Date endDate = schedule.getEndTime();

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

        Map<String, Object> dataMap = QuartzExecutors.buildDataMap(projectId, scheduleId, schedule);

        QuartzExecutors.getInstance().addJob(ProcessScheduleJob.class, jobName, jobGroupName, startDate, endDate,
                schedule.getCrontab(), dataMap);

    }

    /**
     * delete schedule
     *
     * @param projectId project id
     * @param scheduleId schedule id
     * @throws RuntimeException runtime exception
     */
    @Override
    public void deleteSchedule(int projectId, int scheduleId) {
        logger.info("delete schedules of project id:{}, schedule id:{}", projectId, scheduleId);

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

        if (!QuartzExecutors.getInstance().deleteJob(jobName, jobGroupName)) {
            logger.warn("set offline failure:projectId:{},scheduleId:{}", projectId, scheduleId);
            throw new ServiceException("set offline failure");
        }

    }

    /**
     * delete schedule by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param scheduleId scheule id
     * @return delete result code
     */
    @Override
    public Result<Void> deleteScheduleById(User loginUser, String projectName, Integer scheduleId) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        Schedule schedule = scheduleMapper.selectById(scheduleId);

        if (schedule == null) {
            return Result.errorWithArgs(Status.SCHEDULE_CRON_NOT_EXISTS, scheduleId);
        }

        // Determine if the login user is the owner of the schedule
        if (loginUser.getId() != schedule.getUserId()
                && loginUser.getUserType() != UserType.ADMIN_USER) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        // check schedule is already online
        if (schedule.getReleaseState() == ReleaseState.ONLINE) {
            return Result.errorWithArgs(Status.SCHEDULE_CRON_STATE_ONLINE, schedule.getId());
        }

        int delete = scheduleMapper.deleteById(scheduleId);

        if (delete > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.DELETE_SCHEDULE_CRON_BY_ID_ERROR);
        }
    }

    /**
     * preview schedule
     *
     * @param loginUser login user
     * @param projectName project name
     * @param schedule schedule expression
     * @return the next five fire time
     */
    @Override
    public Result<List<String>> previewSchedule(User loginUser, String projectName, String schedule) {
        CronExpression cronExpression;
        ScheduleParam scheduleParam = JSONUtils.parseObject(schedule, ScheduleParam.class);
        Date now = new Date();

        Date startTime = now.after(scheduleParam.getStartTime()) ? now : scheduleParam.getStartTime();
        Date endTime = scheduleParam.getEndTime();
        try {
            cronExpression = CronUtils.parse2CronExpression(scheduleParam.getCrontab());
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            return Result.error(Status.PARSE_TO_CRON_EXPRESSION_ERROR);
        }
        List<Date> selfFireDateList = CronUtils.getSelfFireDateList(startTime, endTime, cronExpression, Constants.PREVIEW_SCHEDULE_EXECUTE_COUNT);
        return Result.success(selfFireDateList.stream().map(DateUtils::dateToString).collect(Collectors.toList()));
    }
}
