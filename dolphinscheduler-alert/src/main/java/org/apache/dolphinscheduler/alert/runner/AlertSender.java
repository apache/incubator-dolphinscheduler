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
package org.apache.dolphinscheduler.alert.runner;

import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.plugin.PluginManager;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertData;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * alert sender
 */
public class AlertSender {

    private static final Logger logger = LoggerFactory.getLogger(AlertSender.class);

    private List<Alert> alertList;
    private AlertDao alertDao;
    private PluginManager pluginManager;

    public AlertSender() {
    }

    public AlertSender(List<Alert> alertList, AlertDao alertDao, PluginManager pluginManager) {
        super();
        this.alertList = alertList;
        this.alertDao = alertDao;
        this.pluginManager = pluginManager;
    }

    public void run() {
        List<User> users;
        for (Alert alert : alertList) {
            users = alertDao.listUserByAlertgroupId(alert.getAlertGroupId());

            // receiving group list
            List<String> receviersList = new ArrayList<>();
            for (User user : users) {
                receviersList.add(user.getEmail());
            }

            AlertData alertData = new AlertData();
            alertData.setId(alert.getId())
                    .setAlertGroupId(alert.getAlertGroupId())
                    .setContent(alert.getContent())
                    .setLog(alert.getLog())
                    .setReceivers(alert.getReceivers())
                    .setReceiversCc(alert.getReceiversCc())
                    .setShowType(alert.getShowType().getDescp())
                    .setAlertType(alert.getAlertType().getDescp())
                    .setTitle(alert.getTitle());

            AlertInfo alertInfo = new AlertInfo();
            alertInfo.setAlertData(alertData);

            alertInfo.addProp("receivers", receviersList);

            AlertPlugin emailPlugin = pluginManager.findOne(Constants.PLUGIN_DEFAULT_EMAIL_ID);
            Result ret = emailPlugin.process(alertInfo);

            if (ret == null) {
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, "alert send error", alert.getId());
                logger.info("alert send error : return value is null");
            } else if (!ret.getIsSuccess()) {
                alertDao.updateAlert(AlertStatus.EXECUTION_FAILURE, ret.getMessage(), alert.getId());
                logger.info("alert send error : {}", ret.getMessage());
            } else {
                alertDao.updateAlert(AlertStatus.EXECUTION_SUCCESS, ret.getMessage(), alert.getId());
                logger.info("alert send success");
            }
        }

    }

}
