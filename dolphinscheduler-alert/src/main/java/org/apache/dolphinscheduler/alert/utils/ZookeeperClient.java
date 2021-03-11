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

package org.apache.dolphinscheduler.alert.utils;

import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_ABNORMAL_TOLERATING_NUMBER;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_PROPERTIES_PATH;
import static org.apache.dolphinscheduler.alert.utils.Constants.ZOOKEEPER_ROOT;

import org.apache.dolphinscheduler.common.utils.IOUtils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.RetryOneTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ZookeeperClient {


    private static final Properties properties = new Properties();

    public static void concurrentOperation(LockCallBall callBall,String zookeeperConnectorStr) throws Exception {

        RetryPolicy retryPolicy = new RetryOneTime(1000);
        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectorStr, retryPolicy);
        zkClient.start();

        String alertLockPath = PropertyUtils.getString(ZOOKEEPER_ROOT, "/dolphinscheduler") + ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERTS;

        InterProcessMutex mutex = new InterProcessMutex(zkClient, alertLockPath);
        if (zkClient.getState() == CuratorFrameworkState.STARTED) {
            if (mutex.acquire(3, TimeUnit.SECONDS)) {
                callBall.handle();
                mutex.release();
            }

        }
        zkClient.close();

    }

    public static int checkZkStateAbnormalToleratingNumber() {
        return PropertyUtils.getInt(ZOOKEEPER_ABNORMAL_TOLERATING_NUMBER, 3);
    }

    public static  Properties getZookeeperProperties () {
        /**
         * init properties
         */
        String[] propertyFiles = new String[]{ZOOKEEPER_PROPERTIES_PATH};

        for (String fileName : propertyFiles) {
            InputStream fis = null;
            try {
                fis = ZookeeperClient.class.getResourceAsStream(fileName);
                properties.load(fis);

            } catch (IOException e) {
                if (fis != null) {
                    IOUtils.closeQuietly(fis);
                }
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return properties;
    }

    public abstract static class LockCallBall {
        public abstract void handle();
    }
}
