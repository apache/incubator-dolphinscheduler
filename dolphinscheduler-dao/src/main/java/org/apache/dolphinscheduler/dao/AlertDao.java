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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessAlertContent;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ServerAlertContent;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Override
    protected void init() {
        alertMapper = ConnectionFactory.getInstance().getMapper(AlertMapper.class);
        alertPluginInstanceMapper = ConnectionFactory.getInstance().getMapper(AlertPluginInstanceMapper.class);
        alertGroupMapper = ConnectionFactory.getInstance().getMapper(AlertGroupMapper.class);
    }

    /**
     * insert alert
     *
     * @param alert alert
     * @return add alert result
     */
    public int addAlert(Alert alert) {
        return alertMapper.insert(alert);
    }

    /**
     * update alert
     *
     * @param alertStatus alertStatus
     * @param log log
     * @param id id
     * @return update alert result
     */
    public int updateAlert(AlertStatus alertStatus, String log, int id) {
        Alert alert = alertMapper.selectById(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * MasterServer or WorkerServer stopped
     *
     * @param alertGroupId alertGroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendServerStoppedAlert(int alertGroupId, String host, String serverType) {
        Alert alert = new Alert();
        List<ServerAlertContent> serverAlertContents = new ArrayList<>(1);
        ServerAlertContent serverStopAlertContent = ServerAlertContent.newBuilder().
                type(serverType).host(host).event(AlertEvent.SERVER_DOWN).warningLevel(AlertWarnLevel.SERIOUS).
                build();
        serverAlertContents.add(serverStopAlertContent);
        String content = JSONUtils.toJsonString(serverAlertContents);
        alert.setTitle("Fault tolerance warning");
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    /**
     * process time out alert
     *
     * @param processInstance processInstance
     * @param processDefinition processDefinition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        int alertGroupId = processInstance.getWarningGroupId();
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .processId(processInstance.getId())
                .processName(processInstance.getName())
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Process Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    private void saveTaskTimeoutAlert(Alert alert, String content, int alertGroupId) {
        alert.setAlertGroupId(alertGroupId);
        alert.setContent(content);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alertMapper.insert(alert);
    }

    /**
     * task timeout warn
     *
     * @param alertGroupId alertGroupId
     * @param processInstanceId processInstanceId
     * @param processInstanceName processInstanceName
     * @param taskId taskId
     * @param taskName taskName
     */
    public void sendTaskTimeoutAlert(int alertGroupId, int processInstanceId,
                                     String processInstanceName, int taskId, String taskName) {
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .processId(processInstanceId)
                .processName(processInstanceName)
                .taskId(taskId)
                .taskName(taskName)
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Task Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    /**
     * list the alert information of waiting to be executed
     *
     * @return alert list
     */
    public List<Alert> listWaitExecutionAlert() {
        return alertMapper.listAlertByStatus(AlertStatus.WAIT_EXECUTION);
    }

    /**
     * for test
     *
     * @return AlertMapper
     */
    public AlertMapper getAlertMapper() {
        return alertMapper;
    }

    /**
     * list all alert plugin instance by alert group id
     *
     * @param alertGroupId alert group id
     * @return AlertPluginInstance list
     */
    public List<AlertPluginInstance> listInstanceByAlertGroupId(int alertGroupId) {
        String alertInstanceIdsParam = alertGroupMapper.queryAlertGroupInstanceIdsById(alertGroupId);
        if (StringUtils.isNotBlank(alertInstanceIdsParam)) {
            String[] idsArray = alertInstanceIdsParam.split(",");
            List<Integer> ids = Arrays.stream(idsArray)
                    .map(s -> Integer.parseInt(s.trim()))
                    .collect(Collectors.toList());
            return alertPluginInstanceMapper.queryByIds(ids);
        }
        return null;
    }

    public AlertPluginInstanceMapper getAlertPluginInstanceMapper() {
        return alertPluginInstanceMapper;
    }

    public void setAlertPluginInstanceMapper(AlertPluginInstanceMapper alertPluginInstanceMapper) {
        this.alertPluginInstanceMapper = alertPluginInstanceMapper;
    }

    public AlertGroupMapper getAlertGroupMapper() {
        return alertGroupMapper;
    }

    public void setAlertGroupMapper(AlertGroupMapper alertGroupMapper) {
        this.alertGroupMapper = alertGroupMapper;
    }
}
