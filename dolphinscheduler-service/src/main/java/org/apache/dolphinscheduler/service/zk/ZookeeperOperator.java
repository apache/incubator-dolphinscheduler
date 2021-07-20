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

package org.apache.dolphinscheduler.service.zk;

import static org.apache.dolphinscheduler.common.utils.Preconditions.checkNotNull;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zk base operator
 */
@Component
public class ZookeeperOperator implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperOperator.class);

    @Autowired
    private ZookeeperConfig zookeeperConfig;

    protected CuratorFramework zkClient;

    @Override
    public void afterPropertiesSet() {
        this.zkClient = buildClient();
        initStateListener();
        treeCacheStart();
    }

    /**
     * this method is for sub class,
     */
    protected void registerListener(){}

    protected void treeCacheStart(){}

    public void initStateListener() {
        checkNotNull(zkClient);

        zkClient.getConnectionStateListenable().addListener((client, newState) -> {
            if (newState == ConnectionState.LOST) {
                logger.error("connection lost from zookeeper");
            } else if (newState == ConnectionState.RECONNECTED) {
                logger.info("reconnected to zookeeper");
            } else if (newState == ConnectionState.SUSPENDED) {
                logger.warn("connection SUSPENDED to zookeeper");
            }
        });
    }

    private CuratorFramework buildClient() {
        logger.info("zookeeper registry center init, server lists is: {}.", zookeeperConfig.getServerList());

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().ensembleProvider(new DefaultEnsembleProvider(checkNotNull(zookeeperConfig.getServerList(),"zookeeper quorum can't be null")))
                .retryPolicy(new ExponentialBackoffRetry(zookeeperConfig.getBaseSleepTimeMs(), zookeeperConfig.getMaxRetries(), zookeeperConfig.getMaxSleepMs()));

        //these has default value
        if (0 != zookeeperConfig.getSessionTimeoutMs()) {
            builder.sessionTimeoutMs(zookeeperConfig.getSessionTimeoutMs());
        }
        if (0 != zookeeperConfig.getConnectionTimeoutMs()) {
            builder.connectionTimeoutMs(zookeeperConfig.getConnectionTimeoutMs());
        }
        if (StringUtils.isNotBlank(zookeeperConfig.getDigest())) {
            builder.authorization("digest", zookeeperConfig.getDigest().getBytes(StandardCharsets.UTF_8)).aclProvider(new ACLProvider() {

                @Override
                public List<ACL> getDefaultAcl() {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }

                @Override
                public List<ACL> getAclForPath(final String path) {
                    return ZooDefs.Ids.CREATOR_ALL_ACL;
                }
            });
        }
        zkClient = builder.build();
        zkClient.start();
        try {
            zkClient.blockUntilConnected();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
        return zkClient;
    }

    public String get(final String key) {
        try {
            return new String(zkClient.getData().forPath(key), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("get key : {}", key, ex);
        }
        return null;
    }

    public List<String> getChildrenKeys(final String key) {
        try {
            return zkClient.getChildren().forPath(key);
        } catch (NoNodeException ex) {
            return new ArrayList<>();
        } catch (InterruptedException ex) {
            logger.error("getChildrenKeys key : {} InterruptedException", key);
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            logger.error("getChildrenKeys key : {}", key, ex);
            throw new RuntimeException(ex);
        }
    }

    public boolean hasChildren(final String key) {
        Stat stat;
        try {
            stat = zkClient.checkExists().forPath(key);
            return stat.getNumChildren() >= 1;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean isExisted(final String key) {
        try {
            return zkClient.checkExists().forPath(key) != null;
        } catch (Exception ex) {
            logger.error("isExisted key : {}", key, ex);
        }
        return false;
    }

    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(StandardCharsets.UTF_8));
            } else {
                update(key, value);
            }
        } catch (Exception ex) {
            logger.error("persist key : {} , value : {}", key, value, ex);
        }
    }

    public void update(final String key, final String value) {
        try {
            zkClient.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(StandardCharsets.UTF_8)).and().commit();
        } catch (Exception ex) {
            logger.error("update key : {} , value : {}", key, value, ex);
        }
    }

    public void persistEphemeral(final String path, final String value) {
        try {
            // If the ephemeral node exist and the data is not equals to the given value
            // delete the old node
            if (isExisted(path) && !value.equals(get(path))) {
                try {
                    zkClient.delete().deletingChildrenIfNeeded().forPath(path);
                } catch (NoNodeException ignore) {
                    //NOP
                }
            }
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            logger.error("persistEphemeral path : {} , value : {}", path, value, ex);
        }
    }

    public void persistEphemeral(String key, String value, boolean overwrite) {
        try {
            if (overwrite) {
                persistEphemeral(key, value);
            } else {
                if (!isExisted(key)) {
                    zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (final Exception ex) {
            logger.error("persistEphemeral key : {} , value : {}, overwrite : {}", key, value, overwrite, ex);
        }
    }

    public void persistEphemeralSequential(final String key, String value) {
        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (final Exception ex) {
            logger.error("persistEphemeralSequential key : {}", key, ex);
        }
    }

    public void remove(final String key) {
        try {
            if (isExisted(key)) {
                zkClient.delete().deletingChildrenIfNeeded().forPath(key);
            }
        } catch (NoNodeException ignore) {
            //NOP
        } catch (final Exception ex) {
            logger.error("remove key : {}", key, ex);
        }
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }

    public ZookeeperConfig getZookeeperConfig() {
        return zookeeperConfig;
    }

    public void close() {
        CloseableUtils.closeQuietly(zkClient);
    }
}