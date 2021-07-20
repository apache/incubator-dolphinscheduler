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

package org.apache.dolphinscheduler.server.worker.registry;

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.SLASH;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.registry.ZookeeperRegistryCenter;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;

import org.apache.curator.framework.state.ConnectionState;

import java.util.Date;
import java.util.Set;
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
 * worker registry
 */
@Service
public class WorkerRegistry {

    private final Logger logger = LoggerFactory.getLogger(WorkerRegistry.class);

    /**
     * zookeeper registry center
     */
    @Autowired
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    /**
     * worker config
     */
    @Autowired
    private WorkerConfig workerConfig;

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    /**
     * worker start time
     */
    private String startTime;


    private Set<String> workerGroups;

    @PostConstruct
    public void init() {
        this.workerGroups = workerConfig.getWorkerGroups();
        this.startTime = DateUtils.dateToString(new Date());
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    /**
     * get zookeeper registry center
     * @return ZookeeperRegistryCenter
     */
    public ZookeeperRegistryCenter getZookeeperRegistryCenter() {
        return zookeeperRegistryCenter;
    }

    /**
     * registry
     */
    public void registry() {
        String address = NetUtils.getAddr(workerConfig.getListenPort());
        Set<String> workerZkPaths = getWorkerZkPaths();
        int workerHeartbeatInterval = workerConfig.getWorkerHeartbeatInterval();

        for (String workerZKPath : workerZkPaths) {
            zookeeperRegistryCenter.getRegisterOperator().persistEphemeral(workerZKPath, "");
            zookeeperRegistryCenter.getRegisterOperator().getZkClient().getConnectionStateListenable().addListener(
                (client,newState) -> {
                    if (newState == ConnectionState.LOST) {
                        logger.error("worker : {} connection lost from zookeeper", address);
                    } else if (newState == ConnectionState.RECONNECTED) {
                        logger.info("worker : {} reconnected to zookeeper", address);
                    } else if (newState == ConnectionState.SUSPENDED) {
                        logger.warn("worker : {} connection SUSPENDED ", address);
                    }
                });
            logger.info("worker node : {} registry to ZK {} successfully", address, workerZKPath);
        }

        HeartBeatTask heartBeatTask = new HeartBeatTask(this.startTime,
                this.workerConfig.getWorkerMaxCpuloadAvg(),
                this.workerConfig.getWorkerReservedMemory(),
                workerZkPaths,
                Constants.WORKER_TYPE,
                this.zookeeperRegistryCenter);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, workerHeartbeatInterval, workerHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("worker node : {} heartbeat interval {} s", address, workerHeartbeatInterval);
    }

    /**
     * remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        Set<String> workerZkPaths = getWorkerZkPaths();
        for (String workerZkPath : workerZkPaths) {
            zookeeperRegistryCenter.getRegisterOperator().remove(workerZkPath);
            logger.info("worker node : {} unRegistry from ZK {}.", address, workerZkPath);
        }
        this.heartBeatExecutor.shutdownNow();
        logger.info("heartbeat executor shutdown");
    }

    /**
     * get worker path
     */
    public Set<String> getWorkerZkPaths() {
        Set<String> workerZkPaths = Sets.newHashSet();

        String address = getLocalAddress();
        String workerZkPathPrefix = this.zookeeperRegistryCenter.getWorkerPath();

        for (String workGroup : this.workerGroups) {
            StringBuilder workerZkPathBuilder = new StringBuilder(100);
            workerZkPathBuilder.append(workerZkPathPrefix).append(SLASH);
            if (StringUtils.isEmpty(workGroup)) {
                workGroup = DEFAULT_WORKER_GROUP;
            }
            // trim and lower case is need
            workerZkPathBuilder.append(workGroup.trim().toLowerCase()).append(SLASH);
            workerZkPathBuilder.append(address);
            workerZkPaths.add(workerZkPathBuilder.toString());
        }
        return workerZkPaths;
    }

    /**
     * get local address
     * @return local address
     */
    private String getLocalAddress() {
        return NetUtils.getAddr(workerConfig.getListenPort());
    }

}
