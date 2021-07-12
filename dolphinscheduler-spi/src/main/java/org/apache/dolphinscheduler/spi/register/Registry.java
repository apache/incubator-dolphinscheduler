package org.apache.dolphinscheduler.spi.register;/*
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

import java.util.List;
import java.util.Map;

/**
 * The final display of all registry component data must follow a tree structure.
 * Therefore, some registry may need to do a layer of internal conversion, such as Etcd
 */
public interface Registry {

    /**
     * initialize registry center.
     */
    void init(Map<String, String> registerData);

    /**
     * close registry
     */
    void close();

    /**
     * subscribe registry data change
     */
    boolean subscribe(String path, SubscribeListener subscribeListener);

    /**
     * unsubscribe
     */
    void unsubscribe(String path);

    /**
     * Registry status monitoring, globally unique. Only one is allowed to subscribe.
     */
    void addConnectionStateListener(RegistryConnectListener registryConnectListener);

    /**
     * get key
     */
    String get(String key);

    /**
     * delete
     */
    void remove(String key);

    /**
     * persist data
     */
    void persist(String key, String value);

    /**
     *persist ephemeral data
     */
    void persistEphemeral(String key, String value);

    /**
     * update data
     */
    void update(String key, String value);

    /**
     * get children keys
     */
    List<String> getChildren(String path);

    /**
     * Judge node is exist or not.
     */
    boolean isExisted(String key);

    /**
     * delete kay
     */
    boolean delete(String key);

    /**
     * Obtain a distributed lock
     * todo It is best to add expiration time, and automatically release the lock after expiration
     */
    boolean acquireLock(String key);

    /**
     * release key
     */
    boolean releaseLock(String key);
}
