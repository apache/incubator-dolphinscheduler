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

package org.apache.dolphinscheduler.alert;

import static org.apache.dolphinscheduler.common.Constants.ALERT_RPC_PORT;

import org.apache.dolphinscheduler.alert.plugin.AlertPluginManager;
import org.apache.dolphinscheduler.common.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.common.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.alert.processor.AlertRequestProcessor;
import org.apache.dolphinscheduler.alert.runner.AlertSender;
import org.apache.dolphinscheduler.alert.utils.Constants;
import org.apache.dolphinscheduler.alert.utils.PropertyUtils;
import org.apache.dolphinscheduler.alert.utils.ZookeeperClient;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * alert of start
 */
public class AlertServer {
    public static final String ALERT_PLUGIN_BINDING = "alert.plugin.binding";
    public static final String ALERT_PLUGIN_DIR = "alert.plugin.dir";
    public static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";
    private static final Logger logger = LoggerFactory.getLogger(AlertServer.class);
    private static AlertServer instance;
    /**
     * Alert Dao
     */
    private AlertDao alertDao = DaoFactory.getDaoInstance(AlertDao.class);
    private AlertSender alertSender;
    private AlertPluginManager alertPluginManager;
    private DolphinPluginManagerConfig alertPluginManagerConfig;
    private int zookeeperStateAbnormalToleratingNumber = 0;
    /**
     * netty server
     */
    private NettyRemotingServer server;

    private AlertServer() {

    }

    public static final AlertServer getInstance() {
        return AlertServerHolder.INSTANCE;

    }

    public static void main(String[] args) {
        AlertServer alertServer = AlertServer.getInstance();
        alertServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                alertServer.stop();
            }
        });
    }

    private void initPlugin() {
        alertPluginManager = new AlertPluginManager();
        alertPluginManagerConfig = new DolphinPluginManagerConfig();
        alertPluginManagerConfig.setPlugins(PropertyUtils.getString(ALERT_PLUGIN_BINDING));
        if (StringUtils.isNotBlank(PropertyUtils.getString(ALERT_PLUGIN_DIR))) {
            alertPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(ALERT_PLUGIN_DIR, Constants.ALERT_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY))) {
            alertPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY).trim());
        }

        DolphinPluginLoader alertPluginLoader = new DolphinPluginLoader(alertPluginManagerConfig, ImmutableList.of(alertPluginManager));
        try {
            alertPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("load Alert Plugin Failed !", e);
        }
    }

    /**
     * init netty remoting server
     */
    private void initRemoteServer() {
        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(ALERT_RPC_PORT);
        this.server = new NettyRemotingServer(serverConfig);
        this.server.registerProcessor(CommandType.ALERT_SEND_REQUEST, new AlertRequestProcessor(alertDao, alertPluginManager));
        this.server.start();
    }

    /**
     * Cyclic alert info sending alert
     */
    private void runSender() {

        while (Stopper.isRunning()) {
            try {
                Thread.sleep(Constants.ALERT_SCAN_INTERVAL);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            if (alertPluginManager == null || alertPluginManager.getAlertChannelMap().size() == 0) {
                logger.warn("No Alert Plugin . Can not send alert info. ");
            } else {
                try {
                    Properties properties = ZookeeperClient.getZookeeperProperties();
                    ZookeeperClient.concurrentOperation(new ZookeeperClient.LockCallBall() {
                        @Override
                        public void handle() {
                            senderRunner();
                            zookeeperStateAbnormalToleratingNumber = 0;
                        }
                    }, properties.getProperty(Constants.ZOOKEEPER_LIST));
                } catch (Exception e) {
                    logger.error("alert server with error : ", e);
                }

                if (zookeeperStateAbnormalToleratingNumber > ZookeeperClient.checkZkStateAbnormalToleratingNumber()) {
                    senderRunner();
                    zookeeperStateAbnormalToleratingNumber = 0;
                } else {
                    zookeeperStateAbnormalToleratingNumber++;
                }

            }
        }
    }

    private void senderRunner() {
        List<Alert> alerts = alertDao.listWaitExecutionAlert();
        alertSender = new AlertSender(alerts, alertDao, alertPluginManager);
        alertSender.run();
    }

    /**
     * start
     */
    public void start() {
        initPlugin();
        initRemoteServer();
        logger.info("alert server ready start ");
        runSender();
    }

    /**
     * stop
     */
    public void stop() {
        this.server.close();
        logger.info("alert server shut down");
    }

    private static class AlertServerHolder {
        private static final AlertServer INSTANCE = new AlertServer();
    }

}
