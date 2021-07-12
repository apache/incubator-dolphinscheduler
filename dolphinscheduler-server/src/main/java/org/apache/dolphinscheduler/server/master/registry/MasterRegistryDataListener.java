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
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.spi.register.DataChangeEvent;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterRegistryDataListener implements SubscribeListener {

    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryDataListener.class);

    private final MasterRegistryClient masterRegistryClient;

    private final int order;

    public MasterRegistryDataListener(MasterRegistryClient masterRegistryClient) {
        this(masterRegistryClient, 0);
    }

    public MasterRegistryDataListener(MasterRegistryClient masterRegistryClient, int order) {
        this.masterRegistryClient = masterRegistryClient;
        this.order = order;
    }

    @Override
    public void notify(String path, DataChangeEvent event) {
        //monitor master
        if (path.startsWith(REGISTRY_DOLPHINSCHEDULER_MASTERS + Constants.SINGLE_SLASH)) {
            handleMasterEvent(event, path);
        } else if (path.startsWith(REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH)) {
            //monitor worker
            handleWorkerEvent(event, path);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * monitor master
     *
     * @param event event
     * @param path path
     */
    public void handleMasterEvent(DataChangeEvent event, String path) {
        switch (event) {
            case ADD:
                logger.info("master node added : {}", path);
                break;
            case REMOVE:
                masterRegistryClient.removeNodePath(path, NodeType.MASTER, true);
                break;
            default:
                break;
        }
    }

    /**
     * monitor worker
     *
     * @param event event
     * @param path path
     */
    public void handleWorkerEvent(DataChangeEvent event, String path) {
        switch (event) {
            case ADD:
                logger.info("worker node added : {}", path);
                break;
            case REMOVE:
                logger.info("worker node deleted : {}", path);
                masterRegistryClient.removeNodePath(path, NodeType.WORKER, true);
                break;
            default:
                break;
        }
    }

}
