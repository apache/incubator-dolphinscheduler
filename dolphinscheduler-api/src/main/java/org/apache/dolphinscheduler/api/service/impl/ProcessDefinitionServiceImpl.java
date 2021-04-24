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

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.dto.ProcessMeta;
import org.apache.dolphinscheduler.api.dto.treeview.Instance;
import org.apache.dolphinscheduler.api.dto.treeview.TreeViewDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionVersionService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.SchedulerService;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.exportprocess.ProcessAddTaskParam;
import org.apache.dolphinscheduler.api.utils.exportprocess.TaskNodeParamFactory;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StreamUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * process definition service impl
 */
@Service
public class ProcessDefinitionServiceImpl extends BaseServiceImpl implements ProcessDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionServiceImpl.class);

    private static final String PROCESSDEFINITIONID = "processDefinitionId";

    private static final String RELEASESTATE = "releaseState";

    private static final String TASKS = "tasks";

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionVersionService processDefinitionVersionService;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private SchedulerService schedulerService;

    /**
     * create process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return create result code
     */
    @Override
    public Result<Integer> createProcessDefinition(User loginUser,
                                                   String projectName,
                                                   String name,
                                                   String processDefinitionJson,
                                                   String desc,
                                                   String locations,
                                                   String connects) {

        Project project = projectMapper.queryByName(projectName);
        // check project auth
        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult.getStatus());
        }

        ProcessDefinition processDefine = new ProcessDefinition();
        Date now = new Date();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        CheckParamResult checkProcessJsonResult = checkProcessNodeList(processData, processDefinitionJson);
        if (!Status.SUCCESS.equals(checkProcessJsonResult.getStatus())) {
            return Result.error(checkProcessJsonResult.getStatus());
        }

        processDefine.setName(name);
        processDefine.setReleaseState(ReleaseState.OFFLINE);
        processDefine.setProjectId(project.getId());
        processDefine.setUserId(loginUser.getId());
        processDefine.setProcessDefinitionJson(processDefinitionJson);
        processDefine.setDescription(desc);
        processDefine.setLocations(locations);
        processDefine.setConnects(connects);
        processDefine.setTimeout(processData.getTimeout());
        processDefine.setTenantId(processData.getTenantId());
        processDefine.setModifyBy(loginUser.getUserName());
        processDefine.setResourceIds(getResourceIds(processData));

        //custom global params
        List<Property> globalParamsList = processData.getGlobalParams();
        if (CollectionUtils.isNotEmpty(globalParamsList)) {
            Set<Property> globalParamsSet = new HashSet<>(globalParamsList);
            globalParamsList = new ArrayList<>(globalParamsSet);
            processDefine.setGlobalParamList(globalParamsList);
        }
        processDefine.setCreateTime(now);
        processDefine.setUpdateTime(now);
        processDefine.setFlag(Flag.YES);

        // save the new process definition
        processDefineMapper.insert(processDefine);

        // add process definition version
        long version = processDefinitionVersionService.addProcessDefinitionVersion(processDefine);

        processDefine.setVersion(version);

        processDefineMapper.updateVersionByProcessDefinitionId(processDefine.getId(), version);

        // return processDefinition object with ID
        return Result.success(processDefine.getId());
    }

    /**
     * get resource ids
     *
     * @param processData process data
     * @return resource ids
     */
    private String getResourceIds(ProcessData processData) {
        List<TaskNode> tasks = processData.getTasks();
        Set<Integer> resourceIds = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(tasks)) {
            return sb.toString();
        }
        for (TaskNode taskNode : tasks) {
            String taskParameter = taskNode.getParams();
            AbstractParameters params = TaskParametersUtils.getParameters(taskNode.getType(), taskParameter);
            if (params == null) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
                Set<Integer> tempSet = params.getResourceFilesList().
                        stream()
                        .filter(t -> t.getId() != 0)
                        .map(ResourceInfo::getId)
                        .collect(Collectors.toSet());
                resourceIds.addAll(tempSet);
            }
        }

        for (int i : resourceIds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    /**
     * query process definition list
     *
     * @param loginUser login user
     * @param projectName project name
     * @return definition list
     */
    @Override
    public Result<List<ProcessDefinition>> queryProcessDefinitionList(User loginUser, String projectName) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult.getStatus());
        }

        List<ProcessDefinition> resourceList = processDefineMapper.queryAllDefinitionList(project.getId());
        return Result.success(resourceList);
    }

    /**
     * query process definition list paging
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @param userId user id
     * @return process definition page
     */
    @Override
    public Result<PageListVO<ProcessDefinition>> queryProcessDefinitionListPaging(
            User loginUser, String projectName, String searchVal, Integer pageNo, Integer pageSize, Integer userId) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkParamResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return new Result<>(checkParamResult.getStatus().getCode(), checkParamResult.getMsg());
        }

        Page<ProcessDefinition> page = new Page<>(pageNo, pageSize);
        IPage<ProcessDefinition> processDefinitionIPage = processDefineMapper.queryDefineListPaging(
                page, searchVal, userId, project.getId(), isAdmin(loginUser));

        PageInfo<ProcessDefinition> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) processDefinitionIPage.getTotal());
        pageInfo.setLists(processDefinitionIPage.getRecords());

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * query datail of process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processId process definition id
     * @return process definition detail
     */
    @Override
    public Result<ProcessDefinition> queryProcessDefinitionById(User loginUser, String projectName, Integer processId) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkAuthResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkAuthResult.getStatus())) {
            return Result.error(checkAuthResult);
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, processId);
        } else {
            return Result.success(processDefinition);
        }
    }

    @Override
    public Result<ProcessDefinition> queryProcessDefinitionByName(User loginUser, String projectName, String processDefinitionName) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkAuthResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkAuthResult.getStatus())) {
            return Result.error(checkAuthResult.getStatus());
        }

        ProcessDefinition processDefinition = processDefineMapper.queryByDefineName(project.getId(), processDefinitionName);
        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionName);
        } else {
            return Result.success(processDefinition);
        }
    }

    /**
     * update  process definition
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name process definition name
     * @param id process definition id
     * @param processDefinitionJson process definition json
     * @param desc description
     * @param locations locations for nodes
     * @param connects connects for nodes
     * @return update result code
     */
    @Override
    public Result<ProcessDefinition> updateProcessDefinition(User loginUser,
                                                             String projectName,
                                                             int id,
                                                             String name,
                                                             String processDefinitionJson,
                                                             String desc,
                                                             String locations,
                                                             String connects) {

        Project project = projectMapper.queryByName(projectName);
        CheckParamResult checkAuthResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkAuthResult.getStatus())) {
            return Result.error(checkAuthResult.getStatus());
        }

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
        checkAuthResult = checkProcessNodeList(processData, processDefinitionJson);
        if (!Status.SUCCESS.equals(checkAuthResult.getStatus())) {
            return Result.error(checkAuthResult);
        }
        ProcessDefinition processDefine = processService.findProcessDefineById(id);
        // check process definition exists
        if (processDefine == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, id);
        }
        if (processDefine.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_ALLOWED_EDIT, processDefine.getName());
        }

        if (!name.equals(processDefine.getName())) {
            // check whether the new process define name exist
            ProcessDefinition definition = processDefineMapper.verifyByDefineName(project.getId(), name);
            if (definition != null) {
                return Result.errorWithArgs(Status.PROCESS_DEFINITION_NAME_EXIST, name);
            }
        }
        // get the processdefinitionjson before saving,and then save the name and taskid
        String oldJson = processDefine.getProcessDefinitionJson();
        processDefinitionJson = processService.changeJson(processData,oldJson);
        Date now = new Date();

        processDefine.setId(id);
        processDefine.setName(name);
        processDefine.setReleaseState(ReleaseState.OFFLINE);
        processDefine.setProjectId(project.getId());
        processDefine.setProcessDefinitionJson(processDefinitionJson);
        processDefine.setDescription(desc);
        processDefine.setLocations(locations);
        processDefine.setConnects(connects);
        processDefine.setTimeout(processData.getTimeout());
        processDefine.setTenantId(processData.getTenantId());
        processDefine.setModifyBy(loginUser.getUserName());
        processDefine.setResourceIds(getResourceIds(processData));

        //custom global params
        List<Property> globalParamsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processData.getGlobalParams())) {
            Set<Property> userDefParamsSet = new HashSet<>(processData.getGlobalParams());
            globalParamsList = new ArrayList<>(userDefParamsSet);
        }
        processDefine.setGlobalParamList(globalParamsList);
        processDefine.setUpdateTime(now);
        processDefine.setFlag(Flag.YES);

        // add process definition version
        long version = processDefinitionVersionService.addProcessDefinitionVersion(processDefine);
        processDefine.setVersion(version);

        if (processDefineMapper.updateById(processDefine) > 0) {
            return Result.success(processDefineMapper.queryByDefineId(id));
        } else {
            return Result.error(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
    }

    /**
     * verify process definition name unique
     *
     * @param loginUser login user
     * @param projectName project name
     * @param name name
     * @return true if process definition name not exists, otherwise false
     */
    @Override
    public CheckParamResult verifyProcessDefinitionName(User loginUser, String projectName, String name) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return checkResult;
        }
        ProcessDefinition processDefinition = processDefineMapper.verifyByDefineName(project.getId(), name);
        if (processDefinition == null) {
            putMsg(checkResult, Status.SUCCESS);
        } else {
            putMsg(checkResult, Status.PROCESS_DEFINITION_NAME_EXIST, name);
        }
        return checkResult;
    }

    /**
     * delete process definition by id
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> deleteProcessDefinitionById(User loginUser, String projectName, Integer processDefinitionId) {

        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(processDefinitionId);

        if (processDefinition == null) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_NOT_EXIST, processDefinitionId);
        }

        // Determine if the login user is the owner of the process definition
        if (loginUser.getId() != processDefinition.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        // check process definition is already online
        if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
            return Result.errorWithArgs(Status.PROCESS_DEFINE_STATE_ONLINE, processDefinitionId);
        }
        // check process instances is already running
        List<ProcessInstance> processInstances = processInstanceService.queryByProcessDefineIdAndStatus(processDefinitionId, Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            return Result.errorWithArgs(Status.DELETE_PROCESS_DEFINITION_BY_ID_FAIL, processInstances.size());
        }

        // get the timing according to the process definition
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinitionId);
        if (!schedules.isEmpty() && schedules.size() > 1) {
            logger.warn("scheduler num is {},Greater than 1", schedules.size());
            return Result.error(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
        } else if (schedules.size() == 1) {
            Schedule schedule = schedules.get(0);
            if (schedule.getReleaseState() == ReleaseState.OFFLINE) {
                scheduleMapper.deleteById(schedule.getId());
            } else if (schedule.getReleaseState() == ReleaseState.ONLINE) {
                return Result.errorWithArgs(Status.SCHEDULE_CRON_STATE_ONLINE, schedule.getId());
            }
        }

        int delete = processDefineMapper.deleteById(processDefinitionId);

        if (delete > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.DELETE_PROCESS_DEFINE_BY_ID_ERROR);
        }
    }

    /**
     * release process definition: online / offline
     *
     * @param loginUser login user
     * @param projectName project name
     * @param id process definition id
     * @param releaseState release state
     * @return release result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<ProcessDefinition> releaseProcessDefinition(User loginUser, String projectName, int id, ReleaseState releaseState) {
        Project project = projectMapper.queryByName(projectName);

        CheckParamResult checkAuthResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkAuthResult.getStatus())) {
            return Result.error(checkAuthResult);
        }

        // check state
        if (null == releaseState) {
            putMsg(checkAuthResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
            return Result.error(checkAuthResult);
        }

        ProcessDefinition processDefinition = processDefineMapper.selectById(id);

        switch (releaseState) {
            case ONLINE:
                // To check resources whether they are already cancel authorized or deleted
                String resourceIds = processDefinition.getResourceIds();
                if (StringUtils.isNotBlank(resourceIds)) {
                    Integer[] resourceIdArray = Arrays.stream(resourceIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                    PermissionCheck<Integer> permissionCheck = new PermissionCheck<>(AuthorizationType.RESOURCE_FILE_ID, processService, resourceIdArray, loginUser.getId(), logger);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        putMsg(checkAuthResult, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION, RELEASESTATE);
                        return Result.error(checkAuthResult);
                    }
                }

                processDefinition.setReleaseState(releaseState);
                processDefineMapper.updateById(processDefinition);
                break;
            case OFFLINE:
                processDefinition.setReleaseState(releaseState);
                processDefineMapper.updateById(processDefinition);
                List<Schedule> scheduleList = scheduleMapper.selectAllByProcessDefineArray(
                        new int[]{processDefinition.getId()}
                );

                for (Schedule schedule : scheduleList) {
                    logger.info("set schedule offline, project id: {}, schedule id: {}, process definition id: {}", project.getId(), schedule.getId(), id);
                    // set status
                    schedule.setReleaseState(ReleaseState.OFFLINE);
                    scheduleMapper.updateById(schedule);
                    schedulerService.deleteSchedule(project.getId(), schedule.getId());
                }
                break;
            default:
                putMsg(checkAuthResult, Status.REQUEST_PARAMS_NOT_VALID_ERROR, RELEASESTATE);
                return Result.error(checkAuthResult);
        }

        return Result.success(processDefinition);
    }

    /**
     * batch export process definition by ids
     */
    @Override
    public void batchExportProcessDefinitionByIds(User loginUser, String projectName, String processDefinitionIds, HttpServletResponse response) {

        if (StringUtils.isEmpty(processDefinitionIds)) {
            return;
        }

        //export project info
        Project project = projectMapper.queryByName(projectName);

        //check user access for project
        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return;
        }

        List<ProcessMeta> processDefinitionList =
                getProcessDefinitionList(processDefinitionIds);

        if (CollectionUtils.isNotEmpty(processDefinitionList)) {
            downloadProcessDefinitionFile(response, processDefinitionList);
        }
    }

    /**
     * get process definition list by ids
     */
    private List<ProcessMeta> getProcessDefinitionList(String processDefinitionIds) {
        List<ProcessMeta> processDefinitionList = new ArrayList<>();
        String[] processDefinitionIdArray = processDefinitionIds.split(",");
        for (String strProcessDefinitionId : processDefinitionIdArray) {
            //get workflow info
            int processDefinitionId = Integer.parseInt(strProcessDefinitionId);
            ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(processDefinitionId);
            if (null != processDefinition) {
                processDefinitionList.add(exportProcessMetaData(processDefinitionId, processDefinition));
            }
        }

        return processDefinitionList;
    }

    /**
     * download the process definition file
     */
    private void downloadProcessDefinitionFile(HttpServletResponse response, List<ProcessMeta> processDefinitionList) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSONUtils.toJsonString(processDefinitionList).getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            logger.warn("export process fail", e);
        } finally {
            if (null != buff) {
                try {
                    buff.close();
                } catch (Exception e) {
                    logger.warn("export process buffer not close", e);
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.warn("export process output stream not close", e);
                }
            }
        }
    }

    /**
     * get export process metadata string
     *
     * @param processDefinitionId process definition id
     * @param processDefinition process definition
     * @return export process metadata string
     */
    public String exportProcessMetaDataStr(Integer processDefinitionId, ProcessDefinition processDefinition) {
        //create workflow json file
        return JSONUtils.toJsonString(exportProcessMetaData(processDefinitionId, processDefinition));
    }

    /**
     * get export process metadata string
     *
     * @param processDefinitionId process definition id
     * @param processDefinition process definition
     * @return export process metadata string
     */
    public ProcessMeta exportProcessMetaData(Integer processDefinitionId, ProcessDefinition processDefinition) {
        //correct task param which has data source or dependent param
        String correctProcessDefinitionJson = addExportTaskNodeSpecialParam(processDefinition.getProcessDefinitionJson());
        processDefinition.setProcessDefinitionJson(correctProcessDefinitionJson);

        //export process metadata
        ProcessMeta exportProcessMeta = new ProcessMeta();
        exportProcessMeta.setProjectName(processDefinition.getProjectName());
        exportProcessMeta.setProcessDefinitionName(processDefinition.getName());
        exportProcessMeta.setProcessDefinitionJson(processDefinition.getProcessDefinitionJson());
        exportProcessMeta.setProcessDefinitionDescription(processDefinition.getDescription());
        exportProcessMeta.setProcessDefinitionLocations(processDefinition.getLocations());
        exportProcessMeta.setProcessDefinitionConnects(processDefinition.getConnects());

        //schedule info
        List<Schedule> schedules = scheduleMapper.queryByProcessDefinitionId(processDefinitionId);
        if (!schedules.isEmpty()) {
            Schedule schedule = schedules.get(0);
            exportProcessMeta.setScheduleWarningType(schedule.getWarningType().toString());
            exportProcessMeta.setScheduleWarningGroupId(schedule.getWarningGroupId());
            exportProcessMeta.setScheduleStartTime(DateUtils.dateToString(schedule.getStartTime()));
            exportProcessMeta.setScheduleEndTime(DateUtils.dateToString(schedule.getEndTime()));
            exportProcessMeta.setScheduleCrontab(schedule.getCrontab());
            exportProcessMeta.setScheduleFailureStrategy(String.valueOf(schedule.getFailureStrategy()));
            exportProcessMeta.setScheduleReleaseState(String.valueOf(ReleaseState.OFFLINE));
            exportProcessMeta.setScheduleProcessInstancePriority(String.valueOf(schedule.getProcessInstancePriority()));
            exportProcessMeta.setScheduleWorkerGroupName(schedule.getWorkerGroup());
        }
        //create workflow json file
        return exportProcessMeta;
    }

    /**
     * correct task param which has datasource or dependent
     *
     * @param processDefinitionJson processDefinitionJson
     * @return correct processDefinitionJson
     */
    private String addExportTaskNodeSpecialParam(String processDefinitionJson) {
        ObjectNode jsonObject = JSONUtils.parseObject(processDefinitionJson);
        ArrayNode jsonArray = (ArrayNode) jsonObject.path(TASKS);

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode taskNode = jsonArray.path(i);
            if (StringUtils.isNotEmpty(taskNode.path("type").asText())) {
                String taskType = taskNode.path("type").asText();

                ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
                if (null != addTaskParam) {
                    addTaskParam.addExportSpecialParam(taskNode);
                }
            }
        }
        jsonObject.set(TASKS, jsonArray);
        return jsonObject.toString();
    }

    /**
     * check task if has sub process
     *
     * @param taskType task type
     * @return if task has sub process return true else false
     */
    private boolean checkTaskHasSubProcess(String taskType) {
        return taskType.equals(TaskType.SUB_PROCESS.name());
    }

    /**
     * import process definition
     *
     * @param loginUser login user
     * @param file process metadata json file
     * @param currentProjectName current project name
     * @return import process
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Void> importProcessDefinition(User loginUser, MultipartFile file, String currentProjectName) {
        String processMetaJson = FileUtils.file2String(file);
        List<ProcessMeta> processMetaList = JSONUtils.toList(processMetaJson, ProcessMeta.class);

        //check file content
        CheckParamResult checkResult = new CheckParamResult();
        if (CollectionUtils.isEmpty(processMetaList)) {
            putMsg(checkResult, Status.DATA_IS_NULL, "fileContent");
            return Result.error(checkResult);
        }

        for (ProcessMeta processMeta : processMetaList) {
            Result<Void> result = checkAndImportProcessDefinition(loginUser, currentProjectName, processMeta);
            if (Status.SUCCESS.getCode() != result.getCode()) {
                return result;
            }
        }

        return Result.success(null);
    }

    /**
     * check and import process definition
     */
    private Result<Void> checkAndImportProcessDefinition(User loginUser, String currentProjectName, ProcessMeta processMeta) {
        CheckParamResult checkParamResult = checkImportanceParams(processMeta);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        //deal with process name
        String processDefinitionName = processMeta.getProcessDefinitionName();
        //use currentProjectName to query
        Project targetProject = projectMapper.queryByName(currentProjectName);
        if (null != targetProject) {
            processDefinitionName = recursionProcessDefinitionName(targetProject.getId(),
                    processDefinitionName, 1);
        }

        //unique check
        checkParamResult = verifyProcessDefinitionName(loginUser, currentProjectName, processDefinitionName);
        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        // get create process result
        Result<Integer> createProcessResult =
                getCreateProcessResult(loginUser,
                        currentProjectName,
                        processMeta,
                        processDefinitionName,
                        addImportTaskNodeParam(loginUser, processMeta.getProcessDefinitionJson(), targetProject));

        if (!Status.SUCCESS.equals(checkParamResult.getStatus())) {
            return Result.error(checkParamResult);
        }

        //create process definition
        Integer processDefinitionId =
                Objects.isNull(createProcessResult.getData()) ? null : createProcessResult.getData();

        //scheduler param
        return getImportProcessScheduleResult(loginUser,
                currentProjectName,
                processMeta,
                processDefinitionName,
                processDefinitionId);

    }

    /**
     * get create process result
     */
    private Result<Integer> getCreateProcessResult(User loginUser,
                                                   String currentProjectName,
                                                   ProcessMeta processMeta,
                                                   String processDefinitionName,
                                                   String importProcessParam) {
        Result<Integer> createProcessResult = null;
        try {
            createProcessResult = createProcessDefinition(loginUser
                    , currentProjectName,
                    processDefinitionName + "_import_" + DateUtils.getCurrentTimeStamp(),
                    importProcessParam,
                    processMeta.getProcessDefinitionDescription(),
                    processMeta.getProcessDefinitionLocations(),
                    processMeta.getProcessDefinitionConnects());
        } catch (Exception e) {
            logger.error("import process meta json data: {}", e.getMessage(), e);
            createProcessResult = Result.error(Status.IMPORT_PROCESS_DEFINE_ERROR);
        }

        return createProcessResult;
    }

    /**
     * get import process schedule result
     */
    private Result<Void> getImportProcessScheduleResult(User loginUser,
                                                        String currentProjectName,
                                                        ProcessMeta processMeta,
                                                        String processDefinitionName,
                                                        Integer processDefinitionId) {
        if (null != processMeta.getScheduleCrontab() && null != processDefinitionId) {
            int scheduleInsert = importProcessSchedule(loginUser,
                    currentProjectName,
                    processMeta,
                    processDefinitionName,
                    processDefinitionId);

            if (0 == scheduleInsert) {
                return Result.error(Status.IMPORT_PROCESS_DEFINE_ERROR);
            }
        }
        return Result.success(null);
    }

    /**
     * check importance params
     */
    private CheckParamResult checkImportanceParams(ProcessMeta processMeta) {
        CheckParamResult checkParamResult = new CheckParamResult(Status.SUCCESS);
        if (StringUtils.isEmpty(processMeta.getProjectName())) {
            putMsg(checkParamResult, Status.DATA_IS_NULL, "projectName");
            return checkParamResult;
        }
        if (StringUtils.isEmpty(processMeta.getProcessDefinitionName())) {
            putMsg(checkParamResult, Status.DATA_IS_NULL, "processDefinitionName");
            return checkParamResult;
        }
        if (StringUtils.isEmpty(processMeta.getProcessDefinitionJson())) {
            putMsg(checkParamResult, Status.DATA_IS_NULL, "processDefinitionJson");
            return checkParamResult;
        }

        return checkParamResult;
    }

    /**
     * import process add special task param
     *
     * @param loginUser login user
     * @param processDefinitionJson process definition json
     * @param targetProject target project
     * @return import process param
     */
    private String addImportTaskNodeParam(User loginUser, String processDefinitionJson, Project targetProject) {
        ObjectNode jsonObject = JSONUtils.parseObject(processDefinitionJson);
        ArrayNode jsonArray = (ArrayNode) jsonObject.get(TASKS);
        //add sql and dependent param
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode taskNode = jsonArray.path(i);
            String taskType = taskNode.path("type").asText();
            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
            if (null != addTaskParam) {
                addTaskParam.addImportSpecialParam(taskNode);
            }
        }

        //recursive sub-process parameter correction map key for old process id value for new process id
        Map<Integer, Integer> subProcessIdMap = new HashMap<>();

        List<Object> subProcessList = StreamUtils.asStream(jsonArray.elements())
                .filter(elem -> checkTaskHasSubProcess(JSONUtils.parseObject(elem.toString()).path("type").asText()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(subProcessList)) {
            importSubProcess(loginUser, targetProject, jsonArray, subProcessIdMap);
        }

        jsonObject.set(TASKS, jsonArray);
        return jsonObject.toString();
    }

    /**
     * import process schedule
     *
     * @param loginUser login user
     * @param currentProjectName current project name
     * @param processMeta process meta data
     * @param processDefinitionName process definition name
     * @param processDefinitionId process definition id
     * @return insert schedule flag
     */
    public int importProcessSchedule(User loginUser, String currentProjectName, ProcessMeta processMeta,
                                     String processDefinitionName, Integer processDefinitionId) {
        Date now = new Date();
        Schedule scheduleObj = new Schedule();
        scheduleObj.setProjectName(currentProjectName);
        scheduleObj.setProcessDefinitionId(processDefinitionId);
        scheduleObj.setProcessDefinitionName(processDefinitionName);
        scheduleObj.setCreateTime(now);
        scheduleObj.setUpdateTime(now);
        scheduleObj.setUserId(loginUser.getId());
        scheduleObj.setUserName(loginUser.getUserName());

        scheduleObj.setCrontab(processMeta.getScheduleCrontab());

        if (null != processMeta.getScheduleStartTime()) {
            scheduleObj.setStartTime(DateUtils.stringToDate(processMeta.getScheduleStartTime()));
        }
        if (null != processMeta.getScheduleEndTime()) {
            scheduleObj.setEndTime(DateUtils.stringToDate(processMeta.getScheduleEndTime()));
        }
        if (null != processMeta.getScheduleWarningType()) {
            scheduleObj.setWarningType(WarningType.valueOf(processMeta.getScheduleWarningType()));
        }
        if (null != processMeta.getScheduleWarningGroupId()) {
            scheduleObj.setWarningGroupId(processMeta.getScheduleWarningGroupId());
        }
        if (null != processMeta.getScheduleFailureStrategy()) {
            scheduleObj.setFailureStrategy(FailureStrategy.valueOf(processMeta.getScheduleFailureStrategy()));
        }
        if (null != processMeta.getScheduleReleaseState()) {
            scheduleObj.setReleaseState(ReleaseState.valueOf(processMeta.getScheduleReleaseState()));
        }
        if (null != processMeta.getScheduleProcessInstancePriority()) {
            scheduleObj.setProcessInstancePriority(Priority.valueOf(processMeta.getScheduleProcessInstancePriority()));
        }

        if (null != processMeta.getScheduleWorkerGroupName()) {
            scheduleObj.setWorkerGroup(processMeta.getScheduleWorkerGroupName());
        }

        return scheduleMapper.insert(scheduleObj);
    }

    /**
     * check import process has sub process
     * recursion create sub process
     *
     * @param loginUser login user
     * @param targetProject target project
     * @param jsonArray process task array
     * @param subProcessIdMap correct sub process id map
     */
    private void importSubProcess(User loginUser, Project targetProject, ArrayNode jsonArray, Map<Integer, Integer> subProcessIdMap) {
        for (int i = 0; i < jsonArray.size(); i++) {
            ObjectNode taskNode = (ObjectNode) jsonArray.path(i);
            String taskType = taskNode.path("type").asText();

            if (!checkTaskHasSubProcess(taskType)) {
                continue;
            }
            //get sub process info
            ObjectNode subParams = (ObjectNode) taskNode.path("params");
            Integer subProcessId = subParams.path(PROCESSDEFINITIONID).asInt();
            ProcessDefinition subProcess = processDefineMapper.queryByDefineId(subProcessId);
            //check is sub process exist in db
            if (null == subProcess) {
                continue;
            }
            String subProcessJson = subProcess.getProcessDefinitionJson();
            //check current project has sub process
            ProcessDefinition currentProjectSubProcess = processDefineMapper.queryByDefineName(targetProject.getId(), subProcess.getName());

            if (null == currentProjectSubProcess) {
                ArrayNode subJsonArray = (ArrayNode) JSONUtils.parseObject(subProcess.getProcessDefinitionJson()).get(TASKS);

                List<Object> subProcessList = StreamUtils.asStream(subJsonArray.elements())
                        .filter(item -> checkTaskHasSubProcess(JSONUtils.parseObject(item.toString()).path("type").asText()))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(subProcessList)) {
                    importSubProcess(loginUser, targetProject, subJsonArray, subProcessIdMap);
                    //sub process processId correct
                    if (!subProcessIdMap.isEmpty()) {

                        for (Map.Entry<Integer, Integer> entry : subProcessIdMap.entrySet()) {
                            String oldSubProcessId = "\"processDefinitionId\":" + entry.getKey();
                            String newSubProcessId = "\"processDefinitionId\":" + entry.getValue();
                            subProcessJson = subProcessJson.replaceAll(oldSubProcessId, newSubProcessId);
                        }

                        subProcessIdMap.clear();
                    }
                }

                //if sub-process recursion
                Date now = new Date();
                //create sub process in target project
                ProcessDefinition processDefine = new ProcessDefinition();
                processDefine.setName(subProcess.getName());
                processDefine.setVersion(subProcess.getVersion());
                processDefine.setReleaseState(subProcess.getReleaseState());
                processDefine.setProjectId(targetProject.getId());
                processDefine.setUserId(loginUser.getId());
                processDefine.setProcessDefinitionJson(subProcessJson);
                processDefine.setDescription(subProcess.getDescription());
                processDefine.setLocations(subProcess.getLocations());
                processDefine.setConnects(subProcess.getConnects());
                processDefine.setTimeout(subProcess.getTimeout());
                processDefine.setTenantId(subProcess.getTenantId());
                processDefine.setGlobalParams(subProcess.getGlobalParams());
                processDefine.setCreateTime(now);
                processDefine.setUpdateTime(now);
                processDefine.setFlag(subProcess.getFlag());
                processDefine.setWarningGroupId(subProcess.getWarningGroupId());
                processDefineMapper.insert(processDefine);

                logger.info("create sub process, project: {}, process name: {}", targetProject.getName(), processDefine.getName());

                //modify task node
                ProcessDefinition newSubProcessDefine = processDefineMapper.queryByDefineName(processDefine.getProjectId(), processDefine.getName());

                if (null != newSubProcessDefine) {
                    subProcessIdMap.put(subProcessId, newSubProcessDefine.getId());
                    subParams.put(PROCESSDEFINITIONID, newSubProcessDefine.getId());
                    taskNode.set("params", subParams);
                }
            }
        }
    }

    /**
     * check the process definition node meets the specifications
     *
     * @param processData process data
     * @param processDefinitionJson process definition json
     * @return check result code
     */
    @Override
    public CheckParamResult checkProcessNodeList(ProcessData processData, String processDefinitionJson) {

        CheckParamResult result = new CheckParamResult();
        try {
            if (processData == null) {
                logger.error("process data is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, processDefinitionJson);
                return result;
            }

            // Check whether the task node is normal
            List<TaskNode> taskNodes = processData.getTasks();

            if (CollectionUtils.isEmpty(taskNodes)) {
                logger.error("process node info is empty");
                putMsg(result, Status.PROCESS_DAG_IS_EMPTY);
                return result;
            }

            // check has cycle
            if (graphHasCycle(taskNodes)) {
                logger.error("process DAG has cycle");
                putMsg(result, Status.PROCESS_NODE_HAS_CYCLE);
                return result;
            }

            // check whether the process definition json is normal
            for (TaskNode taskNode : taskNodes) {
                if (!CheckUtils.checkTaskNodeParameters(taskNode.getParams(), taskNode.getType())) {
                    logger.error("task node {} parameter invalid", taskNode.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
                    return result;
                }

                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            result.setStatus(Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.setMsg(e.getMessage());
        }
        return result;
    }

    /**
     * get task node details based on process definition
     *
     * @param defineId define id
     * @return task node list
     */
    @Override
    public Result<List<TaskNode>> getTaskNodeListByDefinitionId(Integer defineId) {

        ProcessDefinition processDefinition = processDefineMapper.selectById(defineId);
        if (processDefinition == null) {
            logger.info("process define not exists");
            CheckParamResult result = new CheckParamResult();
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, defineId);
            return Result.error(result);
        }

        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        //process data check
        if (null == processData) {
            logger.error("process data is null");
            CheckParamResult result = new CheckParamResult();
            putMsg(result, Status.DATA_IS_NOT_VALID, processDefinitionJson);
            return Result.error(result);
        }

        List<TaskNode> taskNodeList = (processData.getTasks() == null) ? new ArrayList<>() : processData.getTasks();

        return Result.success(taskNodeList);

    }

    /**
     * get task node details based on process definition
     *
     * @param defineIdList define id list
     * @return task node list
     */
    @Override
    public Result<Map<Integer, List<TaskNode>>> getTaskNodeListByDefinitionIdList(String defineIdList) {

        Map<Integer, List<TaskNode>> taskNodeMap = new HashMap<>();
        String[] idList = defineIdList.split(",");
        List<Integer> idIntList = new ArrayList<>();
        for (String definitionId : idList) {
            idIntList.add(Integer.parseInt(definitionId));
        }
        Integer[] idArray = idIntList.toArray(new Integer[0]);
        List<ProcessDefinition> processDefinitionList = processDefineMapper.queryDefinitionListByIdList(idArray);
        if (CollectionUtils.isEmpty(processDefinitionList)) {
            logger.info("process definition not exists");
            CheckParamResult chechResult = new CheckParamResult();
            putMsg(chechResult, Status.PROCESS_DEFINE_NOT_EXIST, defineIdList);
            return Result.error(chechResult);
        }

        for (ProcessDefinition processDefinition : processDefinitionList) {
            String processDefinitionJson = processDefinition.getProcessDefinitionJson();
            ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);
            List<TaskNode> taskNodeList = (processData.getTasks() == null) ? new ArrayList<>() : processData.getTasks();
            taskNodeMap.put(processDefinition.getId(), taskNodeList);
        }

        return Result.success(taskNodeMap);

    }

    /**
     * query process definition all by project id
     *
     * @param projectId project id
     * @return process definitions in the project
     */
    @Override
    public Result<List<ProcessDefinition>> queryProcessDefinitionAllByProjectId(Integer projectId) {

        List<ProcessDefinition> resourceList = processDefineMapper.queryAllDefinitionList(projectId);
        return Result.success(resourceList);
    }

    /**
     * Encapsulates the TreeView structure
     *
     * @param processId process definition id
     * @param limit limit
     * @return tree view json data
     * @throws Exception exception
     */
    @Override
    public Result<TreeViewDto> viewTree(Integer processId, Integer limit) throws Exception {

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (null == processDefinition) {
            logger.info("process define not exists");
            CheckParamResult checkResult = new CheckParamResult();
            putMsg(checkResult, Status.PROCESS_DEFINE_NOT_EXIST, processDefinition);
            return Result.error(checkResult);
        }
        DAG<String, TaskNode, TaskNodeRelation> dag = genDagGraph(processDefinition);
        /**
         * nodes that is running
         */
        Map<String, List<TreeViewDto>> runningNodeMap = new ConcurrentHashMap<>();

        /**
         * nodes that is waiting torun
         */
        Map<String, List<TreeViewDto>> waitingRunningNodeMap = new ConcurrentHashMap<>();

        /**
         * List of process instances
         */
        List<ProcessInstance> processInstanceList = processInstanceService.queryByProcessDefineId(processId, limit);

        for (ProcessInstance processInstance : processInstanceList) {
            processInstance.setDuration(DateUtils.format2Duration(processInstance.getStartTime(), processInstance.getEndTime()));
        }

        if (limit > processInstanceList.size()) {
            limit = processInstanceList.size();
        }

        TreeViewDto parentTreeViewDto = new TreeViewDto();
        parentTreeViewDto.setName("DAG");
        parentTreeViewDto.setType("");
        // Specify the process definition, because it is a TreeView for a process definition

        for (int i = limit - 1; i >= 0; i--) {
            ProcessInstance processInstance = processInstanceList.get(i);

            Date endTime = processInstance.getEndTime() == null ? new Date() : processInstance.getEndTime();
            parentTreeViewDto.getInstances().add(new Instance(processInstance.getId(), processInstance.getName(), "", processInstance.getState().toString()
                    , processInstance.getStartTime(), endTime, processInstance.getHost(), DateUtils.format2Readable(endTime.getTime() - processInstance.getStartTime().getTime())));
        }

        List<TreeViewDto> parentTreeViewDtoList = new ArrayList<>();
        parentTreeViewDtoList.add(parentTreeViewDto);
        // Here is the encapsulation task instance
        for (String startNode : dag.getBeginNode()) {
            runningNodeMap.put(startNode, parentTreeViewDtoList);
        }

        while (Stopper.isRunning()) {
            Set<String> postNodeList = null;
            Iterator<Map.Entry<String, List<TreeViewDto>>> iter = runningNodeMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, List<TreeViewDto>> en = iter.next();
                String nodeName = en.getKey();
                parentTreeViewDtoList = en.getValue();

                TreeViewDto treeViewDto = new TreeViewDto();
                treeViewDto.setName(nodeName);
                TaskNode taskNode = dag.getNode(nodeName);
                treeViewDto.setType(taskNode.getType());

                //set treeViewDto instances
                for (int i = limit - 1; i >= 0; i--) {
                    ProcessInstance processInstance = processInstanceList.get(i);
                    TaskInstance taskInstance = taskInstanceMapper.queryByInstanceIdAndName(processInstance.getId(), nodeName);
                    if (taskInstance == null) {
                        treeViewDto.getInstances().add(new Instance(-1, "not running", "null"));
                    } else {
                        Date startTime = taskInstance.getStartTime() == null ? new Date() : taskInstance.getStartTime();
                        Date endTime = taskInstance.getEndTime() == null ? new Date() : taskInstance.getEndTime();

                        int subProcessId = 0;
                        /**
                         * if process is sub process, the return sub id, or sub id=0
                         */
                        if (taskInstance.getTaskType().equals(TaskType.SUB_PROCESS.name())) {
                            String taskJson = taskInstance.getTaskJson();
                            taskNode = JSONUtils.parseObject(taskJson, TaskNode.class);
                            subProcessId = Integer.parseInt(JSONUtils.parseObject(
                                    taskNode.getParams()).path(CMD_PARAM_SUB_PROCESS_DEFINE_ID).asText());
                        }
                        treeViewDto.getInstances().add(new Instance(taskInstance.getId(), taskInstance.getName(), taskInstance.getTaskType(), taskInstance.getState().toString()
                                , taskInstance.getStartTime(), taskInstance.getEndTime(), taskInstance.getHost(), DateUtils.format2Readable(endTime.getTime() - startTime.getTime()), subProcessId));
                    }
                }
                for (TreeViewDto pTreeViewDto : parentTreeViewDtoList) {
                    pTreeViewDto.getChildren().add(treeViewDto);
                }
                postNodeList = dag.getSubsequentNodes(nodeName);
                if (CollectionUtils.isNotEmpty(postNodeList)) {
                    for (String nextNodeName : postNodeList) {
                        List<TreeViewDto> treeViewDtoList = waitingRunningNodeMap.get(nextNodeName);
                        if (CollectionUtils.isEmpty(treeViewDtoList)) {
                            treeViewDtoList = new ArrayList<>();
                        }
                        treeViewDtoList.add(treeViewDto);
                        waitingRunningNodeMap.put(nextNodeName, treeViewDtoList);
                    }
                }
                runningNodeMap.remove(nodeName);
            }
            if (waitingRunningNodeMap == null || waitingRunningNodeMap.size() == 0) {
                break;
            } else {
                runningNodeMap.putAll(waitingRunningNodeMap);
                waitingRunningNodeMap.clear();
            }
        }
        return Result.success(parentTreeViewDto);
    }

    /**
     * Generate the DAG Graph based on the process definition id
     *
     * @param processDefinition process definition
     * @return dag graph
     */
    private DAG<String, TaskNode, TaskNodeRelation> genDagGraph(ProcessDefinition processDefinition) {

        String processDefinitionJson = processDefinition.getProcessDefinitionJson();

        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        //check process data
        if (null != processData) {
            List<TaskNode> taskNodeList = processData.getTasks();
            processDefinition.setGlobalParamList(processData.getGlobalParams());
            ProcessDag processDag = DagHelper.getProcessDag(taskNodeList);

            // Generate concrete Dag to be executed
            return DagHelper.buildDagGraph(processDag);
        }

        return new DAG<>();
    }

    /**
     * whether the graph has a ring
     *
     * @param taskNodeResponseList task node response list
     * @return if graph has cycle flag
     */
    private boolean graphHasCycle(List<TaskNode> taskNodeResponseList) {
        DAG<String, TaskNode, String> graph = new DAG<>();

        // Fill the vertices
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            graph.addNode(taskNodeResponse.getName(), taskNodeResponse);
        }

        // Fill edge relations
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            taskNodeResponse.getPreTasks();
            List<String> preTasks = JSONUtils.toList(taskNodeResponse.getPreTasks(), String.class);
            if (CollectionUtils.isNotEmpty(preTasks)) {
                for (String preTask : preTasks) {
                    if (!graph.addEdge(preTask, taskNodeResponse.getName())) {
                        return true;
                    }
                }
            }
        }

        return graph.hasCycle();
    }

    private String recursionProcessDefinitionName(Integer projectId, String processDefinitionName, int num) {
        ProcessDefinition processDefinition = processDefineMapper.queryByDefineName(projectId, processDefinitionName);
        if (processDefinition != null) {
            if (num > 1) {
                String str = processDefinitionName.substring(0, processDefinitionName.length() - 3);
                processDefinitionName = str + "(" + num + ")";
            } else {
                processDefinitionName = processDefinition.getName() + "(" + num + ")";
            }
        } else {
            return processDefinitionName;
        }
        return recursionProcessDefinitionName(projectId, processDefinitionName, num + 1);
    }

    private Result<Integer> copyProcessDefinition(User loginUser,
                                                  Integer processId,
                                                  Project targetProject) throws JsonProcessingException {

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            CheckParamResult checkResult = new CheckParamResult();
            putMsg(checkResult, Status.PROCESS_DEFINE_NOT_EXIST, processId);
            return Result.error(checkResult);
        } else {
            return createProcessDefinition(
                    loginUser,
                    targetProject.getName(),
                    processDefinition.getName() + "_copy_" + DateUtils.getCurrentTimeStamp(),
                    processDefinition.getProcessDefinitionJson(),
                    processDefinition.getDescription(),
                    processDefinition.getLocations(),
                    processDefinition.getConnects());

        }
    }

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param projectName projectName
     * @param processDefinitionIds processDefinitionIds
     * @param targetProjectId targetProjectId
     */
    @Override
    public Result<Void> batchCopyProcessDefinition(User loginUser,
                                                   String projectName,
                                                   String processDefinitionIds,
                                                   int targetProjectId) {
        List<String> failedProcessList = new ArrayList<>();

        if (StringUtils.isEmpty(processDefinitionIds)) {
            return Result.errorWithArgs(Status.PROCESS_DEFINITION_IDS_IS_EMPTY, processDefinitionIds);
        }

        //check src project auth
        CheckParamResult checkResult = checkProjectAndAuth(loginUser, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        Project targetProject = projectMapper.queryDetailById(targetProjectId);
        if (targetProject == null) {
            return Result.errorWithArgs(Status.PROJECT_NOT_FOUNT, targetProjectId);
        }

        if (!(targetProject.getName()).equals(projectName)) {
            checkResult = checkProjectAndAuth(loginUser, targetProject.getName());
            if (!Status.SUCCESS.equals(checkResult.getStatus())) {
                return Result.error(checkResult);
            }
        }

        String[] processDefinitionIdList = processDefinitionIds.split(Constants.COMMA);
        doBatchCopyProcessDefinition(loginUser, targetProject, failedProcessList, processDefinitionIdList);

        checkResult = checkBatchOperateResult(projectName, targetProject.getName(), failedProcessList, true);
        if (Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.success(null);
        } else {
            return Result.error(checkResult);
        }
    }

    /**
     * batch move process definition
     *
     * @param loginUser loginUser
     * @param projectName projectName
     * @param processDefinitionIds processDefinitionIds
     * @param targetProjectId targetProjectId
     */
    @Override
    public Result<Void> batchMoveProcessDefinition(User loginUser,
                                                   String projectName,
                                                   String processDefinitionIds,
                                                   int targetProjectId) {
        List<String> failedProcessList = new ArrayList<>();

        //check src project auth
        CheckParamResult checkResult = checkProjectAndAuth(loginUser, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        if (StringUtils.isEmpty(processDefinitionIds)) {
            return Result.errorWithArgs(Status.PROCESS_DEFINITION_IDS_IS_EMPTY, processDefinitionIds);
        }

        Project targetProject = projectMapper.queryDetailById(targetProjectId);
        if (targetProject == null) {
            return Result.errorWithArgs(Status.PROJECT_NOT_FOUNT, targetProjectId);
        }

        if (!(targetProject.getName()).equals(projectName)) {
            checkResult = checkProjectAndAuth(loginUser, targetProject.getName());
            if (Status.SUCCESS.equals(checkResult.getStatus())) {
                return Result.error(checkResult);
            }
        }

        String[] processDefinitionIdList = processDefinitionIds.split(Constants.COMMA);
        doBatchMoveProcessDefinition(targetProject, failedProcessList, processDefinitionIdList);

        checkResult = checkBatchOperateResult(projectName, targetProject.getName(), failedProcessList, false);
        if (Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.success(null);
        } else {
            return Result.error(checkResult);
        }

    }

    /**
     * switch the defined process definition verison
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param version the version user want to switch
     * @return switch process definition version result code
     */
    @Override
    public Result<Void> switchProcessDefinitionVersion(User loginUser, String projectName
            , int processDefinitionId, long version) {

        Project project = projectMapper.queryByName(projectName);
        // check project auth
        CheckParamResult checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        if (!Status.SUCCESS.equals(checkResult.getStatus())) {
            return Result.error(checkResult);
        }

        ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(processDefinitionId);
        if (Objects.isNull(processDefinition)) {
            putMsg(checkResult
                    , Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR
                    , processDefinitionId);
            return Result.error(checkResult);
        }

        ProcessDefinitionVersion processDefinitionVersion = processDefinitionVersionService
                .queryByProcessDefinitionIdAndVersion(processDefinitionId, version);
        if (Objects.isNull(processDefinitionVersion)) {
            putMsg(checkResult
                    , Status.SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR
                    , processDefinitionId
                    , version);
            return Result.error(checkResult);
        }

        processDefinition.setVersion(processDefinitionVersion.getVersion());
        processDefinition.setProcessDefinitionJson(processDefinitionVersion.getProcessDefinitionJson());
        processDefinition.setDescription(processDefinitionVersion.getDescription());
        processDefinition.setLocations(processDefinitionVersion.getLocations());
        processDefinition.setConnects(processDefinitionVersion.getConnects());
        processDefinition.setTimeout(processDefinitionVersion.getTimeout());
        processDefinition.setGlobalParams(processDefinitionVersion.getGlobalParams());
        processDefinition.setUpdateTime(new Date());
        processDefinition.setWarningGroupId(processDefinitionVersion.getWarningGroupId());
        processDefinition.setResourceIds(processDefinitionVersion.getResourceIds());

        if (processDefineMapper.updateById(processDefinition) > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.SWITCH_PROCESS_DEFINITION_VERSION_ERROR);
        }
    }

    /**
     * do batch move process definition
     *
     * @param targetProject targetProject
     * @param failedProcessList failedProcessList
     * @param processDefinitionIdList processDefinitionIdList
     */
    private void doBatchMoveProcessDefinition(Project targetProject, List<String> failedProcessList, String[] processDefinitionIdList) {
        for (String processDefinitionId : processDefinitionIdList) {
            try {
                Result<Void> moveProcessDefinitionResult = moveProcessDefinition(Integer.valueOf(processDefinitionId), targetProject);
                if (Status.SUCCESS.getCode() != moveProcessDefinitionResult.getCode()) {
                    setFailedProcessList(failedProcessList, processDefinitionId);
                    logger.error(moveProcessDefinitionResult.getMsg());
                }
            } catch (Exception e) {
                setFailedProcessList(failedProcessList, processDefinitionId);
            }
        }
    }

    /**
     * batch copy process definition
     *
     * @param loginUser loginUser
     * @param targetProject targetProject
     * @param failedProcessList failedProcessList
     * @param processDefinitionIdList processDefinitionIdList
     */
    private void doBatchCopyProcessDefinition(User loginUser, Project targetProject, List<String> failedProcessList, String[] processDefinitionIdList) {
        for (String processDefinitionId : processDefinitionIdList) {
            try {
                Result<Integer> copyProcessDefinitionResult =
                        copyProcessDefinition(loginUser, Integer.valueOf(processDefinitionId), targetProject);
                if (Status.SUCCESS.getCode() != copyProcessDefinitionResult.getCode()) {
                    setFailedProcessList(failedProcessList, processDefinitionId);
                    logger.error(copyProcessDefinitionResult.getMsg());
                }
            } catch (Exception e) {
                setFailedProcessList(failedProcessList, processDefinitionId);
            }
        }
    }

    /**
     * set failed processList
     *
     * @param failedProcessList failedProcessList
     * @param processDefinitionId processDefinitionId
     */
    private void setFailedProcessList(List<String> failedProcessList, String processDefinitionId) {
        ProcessDefinition processDefinition = processDefineMapper.queryByDefineId(Integer.valueOf(processDefinitionId));
        if (processDefinition != null) {
            failedProcessList.add(processDefinitionId + "[" + processDefinition.getName() + "]");
        } else {
            failedProcessList.add(processDefinitionId + "[null]");
        }
    }

    /**
     * check project and auth
     *
     * @param loginUser loginUser
     * @param projectName projectName
     */
    private CheckParamResult checkProjectAndAuth(User loginUser, String projectName) {
        Project project = projectMapper.queryByName(projectName);

        //check user access for project
        return projectService.checkProjectAndAuth(loginUser, project, projectName);
    }

    /**
     * move process definition
     *
     * @param processId processId
     * @param targetProject targetProject
     * @return move result code
     */
    private Result<Void> moveProcessDefinition(Integer processId,
                                               Project targetProject) {

        ProcessDefinition processDefinition = processDefineMapper.selectById(processId);
        if (processDefinition == null) {
            CheckParamResult checkResult = new CheckParamResult();
            putMsg(checkResult, Status.PROCESS_DEFINE_NOT_EXIST, processId);
            return Result.error(checkResult);
        }

        processDefinition.setProjectId(targetProject.getId());
        processDefinition.setUpdateTime(new Date());
        if (processDefineMapper.updateById(processDefinition) > 0) {
            return Result.success(null);
        } else {
            return Result.error(Status.UPDATE_PROCESS_DEFINITION_ERROR);
        }
    }

    /**
     * check batch operate result
     *
     * @param srcProjectName srcProjectName
     * @param targetProjectName targetProjectName
     * @param failedProcessList failedProcessList
     * @param isCopy isCopy
     */
    private CheckParamResult checkBatchOperateResult(String srcProjectName, String targetProjectName,
                                                     List<String> failedProcessList, boolean isCopy) {
        CheckParamResult checkResult = new CheckParamResult();
        if (!failedProcessList.isEmpty()) {
            if (isCopy) {
                putMsg(checkResult, Status.COPY_PROCESS_DEFINITION_ERROR, srcProjectName, targetProjectName, String.join(",", failedProcessList));
            } else {
                putMsg(checkResult, Status.MOVE_PROCESS_DEFINITION_ERROR, srcProjectName, targetProjectName, String.join(",", failedProcessList));
            }
        } else {
            putMsg(checkResult, Status.SUCCESS);
        }
        return checkResult;
    }

    /**
     * check has associated process definition
     *
     * @param processDefinitionId process definition id
     * @param version version
     * @return The query result has a specific process definition return true
     */
    @Override
    public boolean checkHasAssociatedProcessDefinition(int processDefinitionId, long version) {
        Integer hasAssociatedDefinitionId = processDefineMapper.queryHasAssociatedDefinitionByIdAndVersion(processDefinitionId, version);
        return Objects.nonNull(hasAssociatedDefinitionId);
    }

}

