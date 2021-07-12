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

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_NODE;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.apache.dolphinscheduler.spi.register.RegistryConnectListener;
import org.apache.dolphinscheduler.spi.register.RegistryConnectState;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * zookeeper master client
 * <p>
 * single instance
 */
@Component
public class MasterRegistryClient {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryClient.class);

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;
    @Autowired
    private RegistryClient registryClient;

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

    private String localNodePath;

    public void start() {
        String nodeLock = registryClient.getMasterStartUpLockPath();
        try {
            // create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/startup-masters

            registryClient.getLock(nodeLock);
            // master registry
            registry();
            String registryPath = getMasterPath();
            registryClient.handleDeadServer(registryPath, NodeType.MASTER, Constants.DELETE_OP);

            // init system node

            while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.MASTER)) {
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            }

            // self tolerant
            if (registryClient.getActiveMasterNum() == 1) {
                removeNodePath(null, NodeType.MASTER, true);
                removeNodePath(null, NodeType.WORKER, true);
            }
            registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_NODE, new MasterRegistryDataListener(this));
        } catch (Exception e) {
            logger.error("master start up exception", e);
        } finally {
            registryClient.releaseLock(nodeLock);
        }
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    public void closeRegistry() {
        unRegistry();
    }

    /**
     * remove zookeeper node path
     *
     * @param path zookeeper node path
     * @param nodeType zookeeper node type
     * @param failover is failover
     */
    public void removeNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);
        String failoverPath = getFailoverLockPath(nodeType);
        try {
            registryClient.getLock(failoverPath);

            String serverHost = null;
            if (StringUtils.isNotEmpty(path)) {
                serverHost = registryClient.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    logger.error("server down error: unknown path: {}", path);
                    return;
                }
                // handle dead server
                registryClient.handleDeadServer(path, nodeType, Constants.ADD_OP);
            }
            //failover server
            if (failover) {
                failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed.", nodeType);
            logger.error("failover exception ", e);
        } finally {
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * failover server when server down
     *
     * @param serverHost server host
     * @param nodeType zookeeper node type
     */
    private void failoverServerWhenDown(String serverHost, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                failoverMaster(serverHost);
                break;
            case WORKER:
                failoverWorker(serverHost, true);
                break;
            default:
                break;
        }
    }

    /**
     * get failover lock path
     *
     * @param nodeType zookeeper node type
     * @return fail over lock path
     */
    private String getFailoverLockPath(NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                return registryClient.getMasterFailoverLockPath();
            case WORKER:
                return registryClient.getWorkerFailoverLockPath();
            default:
                return "";
        }
    }

    /**
     * task needs failover if task start before worker starts
     *
     * @param taskInstance task instance
     * @return true if task instance need fail over
     */
    private boolean checkTaskInstanceNeedFailover(TaskInstance taskInstance) {

        boolean taskNeedFailover = true;

        //now no host will execute this task instance,so no need to failover the task
        if (taskInstance.getHost() == null) {
            return false;
        }

        // if the worker node exists in zookeeper, we must check the task starts after the worker
        if (registryClient.checkNodeExists(taskInstance.getHost(), NodeType.WORKER)) {
            //if task start after worker starts, there is no need to failover the task.
            if (checkTaskAfterWorkerStart(taskInstance)) {
                taskNeedFailover = false;
            }
        }
        return taskNeedFailover;
    }

    /**
     * check task start after the worker server starts.
     *
     * @param taskInstance task instance
     * @return true if task instance start time after worker server start date
     */
    private boolean checkTaskAfterWorkerStart(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return false;
        }
        Date workerServerStartDate = null;
        List<Server> workerServers = registryClient.getServerList(NodeType.WORKER);
        for (Server workerServer : workerServers) {
            if (taskInstance.getHost().equals(workerServer.getHost() + Constants.COLON + workerServer.getPort())) {
                workerServerStartDate = workerServer.getCreateTime();
                break;
            }
        }
        if (workerServerStartDate != null) {
            return taskInstance.getStartTime().after(workerServerStartDate);
        }
        return false;
    }

    /**
     * failover worker tasks
     * <p>
     * 1. kill yarn job if there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. failover all tasks when workerHost is null
     *
     * @param workerHost worker host
     * @param needCheckWorkerAlive need check worker alive
     */
    private void failoverWorker(String workerHost, boolean needCheckWorkerAlive) {
        logger.info("start worker[{}] failover ...", workerHost);
        List<TaskInstance> needFailoverTaskInstanceList = processService.queryNeedFailoverTaskInstances(workerHost);
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            if (needCheckWorkerAlive) {
                if (!checkTaskInstanceNeedFailover(taskInstance)) {
                    continue;
                }
            }

            ProcessInstance processInstance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
            if (processInstance != null) {
                taskInstance.setProcessInstance(processInstance);
            }

            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                    .buildTaskInstanceRelatedInfo(taskInstance)
                    .buildProcessInstanceRelatedInfo(processInstance)
                    .create();
            // only kill yarn job if exists , the local thread has exited
            ProcessUtils.killYarnJob(taskExecutionContext);

            taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
            processService.saveTaskInstance(taskInstance);
        }
        logger.info("end worker[{}] failover ...", workerHost);
    }

    /**
     * failover master tasks
     *
     * @param masterHost master host
     */
    private void failoverMaster(String masterHost) {
        logger.info("start master failover ...");

        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(masterHost);

        logger.info("failover process list size:{} ", needFailoverProcessInstanceList.size());
        //updateProcessInstance host is null and insert into command
        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            logger.info("failover process instance id: {} host:{}", processInstance.getId(), processInstance.getHost());
            if (Constants.NULL.equals(processInstance.getHost())) {
                continue;
            }
            processService.processNeedFailoverProcessInstances(processInstance);
        }

        logger.info("master failover end");
    }

    public void blockAcquireMutex() {
        registryClient.getLock(registryClient.getMasterLockPath());
    }

    public void releaseLock() {
        registryClient.releaseLock(registryClient.getMasterLockPath());
    }

    @PostConstruct
    public void init() {
        this.startTime = DateUtils.dateToString(new Date());
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
        registryClient.init();
    }

    /**
     * registry
     */
    public void registry() {
        String address = NetUtils.getAddr(masterConfig.getListenPort());
        localNodePath = getMasterPath();
        registryClient.persistEphemeral(localNodePath, "");
        registryClient.addConnectionStateListener(new MasterRegistryConnectStateListener());
        int masterHeartbeatInterval = masterConfig.getMasterHeartbeatInterval();
        HeartBeatTask heartBeatTask = new HeartBeatTask(startTime,
                masterConfig.getMasterMaxCpuloadAvg(),
                masterConfig.getMasterReservedMemory(),
                Sets.newHashSet(getMasterPath()),
                Constants.MASTER_TYPE,
                registryClient);

        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, masterHeartbeatInterval, masterHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("master node : {} registry to ZK successfully with heartBeatInterval : {}s", address, masterHeartbeatInterval);

    }

    class MasterRegistryConnectStateListener implements RegistryConnectListener {

        @Override
        public void notify(RegistryConnectState newState) {
            if (RegistryConnectState.RECONNECTED == newState) {
                registryClient.persistEphemeral(localNodePath, "");
            }
            if (RegistryConnectState.SUSPENDED == newState) {
                registryClient.persistEphemeral(localNodePath, "");
            }
        }
    }

    /**
     * remove registry info
     */
    public void unRegistry() {
        String address = getLocalAddress();
        String localNodePath = getMasterPath();
        registryClient.remove(localNodePath);
        logger.info("master node : {} unRegistry to register center.", address);
        heartBeatExecutor.shutdown();
        logger.info("heartbeat executor shutdown");
        registryClient.close();
    }

    /**
     * get master path
     */
    public String getMasterPath() {
        String address = getLocalAddress();
        return REGISTRY_DOLPHINSCHEDULER_MASTERS + "/" + address;
    }

    /**
     * get local address
     */
    private String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }

}
