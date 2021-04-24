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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.model.WorkerServerModel;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.ZookeeperRecord;

import java.util.Collection;
import java.util.List;

/**
 * monitor service
 */
public interface MonitorService {

    /**
     * query database state
     *
     * @param loginUser login user
     * @return data base state
     */
    Result<List<MonitorRecord>> queryDatabaseState(User loginUser);

    /**
     * query master list
     *
     * @param loginUser login user
     * @return master information list
     */
    Result<List<Server>> queryMaster(User loginUser);

    /**
     * query zookeeper state
     *
     * @param loginUser login user
     * @return zookeeper information list
     */
    Result<List<ZookeeperRecord>> queryZookeeperState(User loginUser);

    /**
     * query worker list
     *
     * @param loginUser login user
     * @return worker information list
     */
    Result<Collection<WorkerServerModel>> queryWorker(User loginUser);

    List<Server> getServerListFromZK(boolean isMaster);
}
