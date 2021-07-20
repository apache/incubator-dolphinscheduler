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

package org.apache.dolphinscheduler.server.master.registry;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;

import org.apache.curator.framework.state.ConnectionState;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * master registry
 */
@Service
public class MasterRegistry {

    private final Logger logger = LoggerFactory.getLogger(MasterRegistry.class);

    /**
     * zookeeper registry center
     */
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    /**
     * master start time
     */
    private String startTime;

    @PostConstruct
    public void init() {
        this.startTime = DateUtils.dateToString(new Date());
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     * registry
     */
    public void registry() {
        String address = NetUtils.getAddr(masterConfig.getListenPort());
        String localNodePath = getMasterPath();
        zookeeperRegistryCenter.getRegisterOperator().persistEphemeral(localNodePath, "");
        zookeeperRegistryCenter.getRegisterOperator().getZkClient().getConnectionStateListenable().addListener(
            (client, newState) -> {
                if (newState == ConnectionState.LOST) {
                    logger.error("master : {} connection lost from zookeeper", address);
                } else if (newState == ConnectionState.RECONNECTED) {
                    logger.info("master : {} reconnected to zookeeper", address);
                } else if (newState == ConnectionState.SUSPENDED) {
                    logger.warn("master : {} connection SUSPENDED ", address);
                }
            });
        int masterHeartbeatInterval = masterConfig.getMasterHeartbeatInterval();
        HeartBeatTask heartBeatTask = new HeartBeatTask(startTime,
                masterConfig.getMasterMaxCpuloadAvg(),
                masterConfig.getMasterReservedMemory(),
                Sets.newHashSet(getMasterPath()),
                Constants.MASTER_TYPE,
                zookeeperRegistryCenter);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, masterHeartbeatInterval, masterHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("master node : {} registry to ZK successfully with heartBeatInterval : {}s", address, masterHeartbeatInterval);
    }

    /**
     * remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        String localNodePath = getMasterPath();
        zookeeperRegistryCenter.getRegisterOperator().remove(localNodePath);
        logger.info("master node : {} unRegistry to ZK.", address);
        heartBeatExecutor.shutdown();
        logger.info("heartbeat executor shutdown");
    }

    /**
     * get master path
     */
    public String getMasterPath() {
        String address = getLocalAddress();
        return this.zookeeperRegistryCenter.getMasterPath() + "/" + address;
    }

    /**
     * get local address
     * @return
     */
    private String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }

    /**
     * get zookeeper registry center
     * @return ZookeeperRegistryCenter
     */
    public ZookeeperRegistryCenter getZookeeperRegistryCenter() {
        return zookeeperRegistryCenter;
    }

}
