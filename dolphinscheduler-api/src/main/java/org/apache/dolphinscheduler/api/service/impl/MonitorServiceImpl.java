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

import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.ZookeeperMonitor;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerServerModel;
import org.apache.dolphinscheduler.dao.MonitorDBDao;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.ZookeeperRecord;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

/**
 * monitor service impl
 */
@Service
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService {

    @Autowired
    private ZookeeperMonitor zookeeperMonitor;

    @Autowired
    private MonitorDBDao monitorDBDao;

    /**
     * query database state
     *
     * @param loginUser login user
     * @return data base state
     */
    @Override
    public Result<List<MonitorRecord>> queryDatabaseState(User loginUser) {

        List<MonitorRecord> monitorRecordList = monitorDBDao.queryDatabaseState();

        return Result.success(monitorRecordList);

    }

    /**
     * query master list
     *
     * @param loginUser login user
     * @return master information list
     */
    @Override
    public Result<List<Server>> queryMaster(User loginUser) {

        List<Server> masterServers = getServerListFromZK(true);

        return Result.success(masterServers);
    }

    /**
     * query zookeeper state
     *
     * @param loginUser login user
     * @return zookeeper information list
     */
    @Override
    public Result<List<ZookeeperRecord>> queryZookeeperState(User loginUser) {

        List<ZookeeperRecord> zookeeperRecordList = zookeeperMonitor.zookeeperInfoList();

        return Result.success(zookeeperRecordList);

    }

    /**
     * query worker list
     *
     * @param loginUser login user
     * @return worker information list
     */
    @Override
    public Result<Collection<WorkerServerModel>> queryWorker(User loginUser) {

        List<WorkerServerModel> workerServers = getServerListFromZK(false)
                .stream()
                .map((Server server) -> {
                    WorkerServerModel model = new WorkerServerModel();
                    model.setId(server.getId());
                    model.setHost(server.getHost());
                    model.setPort(server.getPort());
                    model.setZkDirectories(Sets.newHashSet(server.getZkDirectory()));
                    model.setResInfo(server.getResInfo());
                    model.setCreateTime(server.getCreateTime());
                    model.setLastHeartbeatTime(server.getLastHeartbeatTime());
                    return model;
                })
                .collect(Collectors.toList());

        Map<String, WorkerServerModel> workerHostPortServerMapping = workerServers
                .stream()
                .collect(Collectors.toMap(
                    (WorkerServerModel worker) -> {
                        String[] s = worker.getZkDirectories().iterator().next().split("/");
                        return s[s.length - 1];
                    }
                    , Function.identity()
                    , (WorkerServerModel oldOne, WorkerServerModel newOne) -> {
                        oldOne.getZkDirectories().addAll(newOne.getZkDirectories());
                        return oldOne;
                    }));

        return Result.success(workerHostPortServerMapping.values());
    }

    @Override
    public List<Server> getServerListFromZK(boolean isMaster) {

        checkNotNull(zookeeperMonitor);
        ZKNodeType zkNodeType = isMaster ? ZKNodeType.MASTER : ZKNodeType.WORKER;
        return zookeeperMonitor.getServerList(zkNodeType);
    }

}
