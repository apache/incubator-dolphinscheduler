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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.spi.register.DataChangeEvent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MasterRegistryDataListenerTest {

    @Test
    public void testNotify() {
        MasterRegistryClient mockRegistryClient = Mockito.mock(MasterRegistryClient.class);
        MasterRegistryDataListener listener = new MasterRegistryDataListener(mockRegistryClient);
        // Master
        listener.notify(Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS + Constants.SINGLE_SLASH + "xx", DataChangeEvent.ADD);
        // Worker
        listener.notify(Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + "xx", DataChangeEvent.ADD);
        // if no exception, assert true
        Assert.assertTrue(true);
    }

    @Test
    public void getOrder() {
        MasterRegistryDataListener listener = new MasterRegistryDataListener(null);
        Assert.assertEquals(0, listener.getOrder());
    }

    @Test
    public void handleMasterEvent() {
        MasterRegistryClient mockRegistryClient = Mockito.mock(MasterRegistryClient.class);

        MasterRegistryDataListener listener = new MasterRegistryDataListener(mockRegistryClient);
        listener.handleMasterEvent(DataChangeEvent.ADD, "");
        listener.handleMasterEvent(DataChangeEvent.REMOVE, "");
        // if no exception, assert true
        Assert.assertTrue(true);
    }

    @Test
    public void handleWorkerEvent() {
        MasterRegistryClient mockRegistryClient = Mockito.mock(MasterRegistryClient.class);

        MasterRegistryDataListener listener = new MasterRegistryDataListener(mockRegistryClient);
        listener.handleWorkerEvent(DataChangeEvent.ADD, "");
        listener.handleWorkerEvent(DataChangeEvent.REMOVE, "");
        // if no exception, assert true
        Assert.assertTrue(true);
    }
}