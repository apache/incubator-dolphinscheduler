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

package org.apache.dolphinscheduler.spi.register;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerManagerTest {

    private Logger logger = LoggerFactory.getLogger(ListenerManagerTest.class);

    @Test
    public void checkHasListeners() {
        Assert.assertFalse(ListenerManager.checkHasListeners("/"));
    }

    @Test
    public void addListener() {
        ListenerManager.addListener("/", new MockListener());
        ListenerManager.addListener("/", new MockListener());
        Assert.assertTrue(true);
    }

    @Test
    public void removeListener() {
        ListenerManager.removeListener("/xxx");
    }

    @Test
    public void dataChange() {
        ListenerManager.dataChange("key", "/", DataChangeEvent.ADD);
        ListenerManager.addListener("key", new MockListener());
        ListenerManager.dataChange("key", "/", DataChangeEvent.ADD);
        Assert.assertTrue(true);
    }

    class MockListener implements SubscribeListener {

        @Override
        public void notify(String path, DataChangeEvent dataChangeEvent) {
            logger.info("notify: path: {}, event: {}", path, dataChangeEvent);
        }

    }
}