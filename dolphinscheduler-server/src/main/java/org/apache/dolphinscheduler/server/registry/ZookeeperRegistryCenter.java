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

package org.apache.dolphinscheduler.server.registry;

import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  zookeeper register center
 */
@Service
public class ZookeeperRegistryCenter implements InitializingBean {

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * namespace
     */
    public static final String NAMESPACE = "/dolphinscheduler";

    /**
     * nodes namespace
     */
    public static final String NODES = NAMESPACE + "/nodes";

    /**
     * master path
     */
    public static final String MASTER_PATH = NODES + "/master";

    /**
     * worker path
     */
    public static final String WORKER_PATH = NODES + "/worker";

    public static final String EMPTY = "";

    @Autowired
    protected ZookeeperCachedOperator zookeeperCachedOperator;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    /**
     * init node persist
     */
    public void init() {
        if (isStarted.compareAndSet(false, true)) {
            initNodes();
        }
    }

    /**
     * init nodes
     */
    private void initNodes() {
        zookeeperCachedOperator.persist(MASTER_PATH, EMPTY);
        zookeeperCachedOperator.persist(WORKER_PATH, EMPTY);
    }

    /**
     * close
     */
    public void close() {
        if (isStarted.compareAndSet(true, false)) {
            if (zookeeperCachedOperator != null) {
                zookeeperCachedOperator.close();
            }
        }
    }

    /**
     * get master path
     * @return master path
     */
    public String getMasterPath() {
        return MASTER_PATH;
    }

    /**
     * get worker path
     * @return worker path
     */
    public String getWorkerPath() {
        return WORKER_PATH;
    }

    /**
     *  get master nodes directly
     * @return master nodes
     */
    public Set<String> getMasterNodesDirectly() {
        List<String> masters = getChildrenKeys(MASTER_PATH);
        return new HashSet<>(masters);
    }

    /**
     *  get worker nodes directly
     * @return master nodes
     */
    public Set<String> getWorkerNodesDirectly() {
        List<String> workers = getChildrenKeys(WORKER_PATH);
        return new HashSet<>(workers);
    }

    /**
     * whether worker path
     * @param path path
     * @return result
     */
    public boolean isWorkerPath(String path) {
        return path != null && path.contains(WORKER_PATH);
    }

    /**
     * whether master path
     * @param path path
     * @return result
     */
    public boolean isMasterPath(String path) {
        return path != null && path.contains(MASTER_PATH);
    }

    /**
     * get children nodes
     * @param key key
     * @return children nodes
     */
    public List<String> getChildrenKeys(final String key) {
        return zookeeperCachedOperator.getChildrenKeys(key);
    }

    /**
     * get zookeeperCachedOperator
     * @return zookeeperCachedOperator
     */
    public ZookeeperCachedOperator getZookeeperCachedOperator() {
        return zookeeperCachedOperator;
    }

}
