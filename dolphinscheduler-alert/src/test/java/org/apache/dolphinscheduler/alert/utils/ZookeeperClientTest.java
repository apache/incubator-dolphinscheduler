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

import org.apache.dolphinscheduler.service.zk.ZKServer;

import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*", "javax.security.*", "javax.crypto.*"})
public class ZookeeperClientTest {

    private static ZKServer zkServer;

    private static boolean flag;

    Properties properties;

    @Before
    public void before() throws IOException {

        properties = PowerMockito.mock(Properties.class);
        new Thread(() -> {
            if (zkServer == null) {
                zkServer = new ZKServer();
            }
            zkServer.startLocalZkServer(2181);
        }).start();
    }

    @Test
    public void testConcurrentOperation() {
        try {
            flag = false;
            ZookeeperClient.concurrentOperation(new ZookeeperClient.LockCallBall() {
                @Override
                public void handle() {
                    flag = true;
                }
            }, "");
            Assert.assertTrue(flag);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testConcurrentOperationLock() {
        try {
            flag = false;
            ZookeeperClient.concurrentOperation(new ZookeeperClient.LockCallBall() {
                @Override
                public void handle() {
                    flag = true;
                }
            }, "127.0.0.1:2181");
            Assert.assertTrue(flag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetZookeeperProperties() {
        Properties properties = ZookeeperClient.getZookeeperProperties();
        Assert.assertNotNull(properties);
    }

    @After
    public void after() {
        if (zkServer != null) {
            zkServer.stop();
        }
    }
}
