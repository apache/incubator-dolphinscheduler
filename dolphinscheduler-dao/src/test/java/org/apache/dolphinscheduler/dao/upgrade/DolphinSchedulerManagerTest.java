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

package org.apache.dolphinscheduler.dao.upgrade;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.SchemaUtils;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * dolphinshceduler manager test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class, SchemaUtils.class })
@PowerMockIgnore({"javax.management.*"})
public class DolphinSchedulerManagerTest {

    @Test
    public void testUpgradeDolphinScheduler() throws Exception {
        String rootDir = System.getProperty("user.dir") + "/../";
        PowerMockito.mockStatic(FileUtils.class);
        File[] files = new File[10];

        files[0] = new File(rootDir + "sql/upgrade/1.0.1_schema");
        files[1] = new File(rootDir + "sql/upgrade/1.0.2_schema");
        files[2] = new File(rootDir + "sql/upgrade/1.1.0_schema");
        files[3] = new File(rootDir + "sql/upgrade/1.2.0_schema");
        files[4] = new File(rootDir + "sql/upgrade/1.3.0_schema");
        files[5] = new File(rootDir + "sql/upgrade/1.3.2_schema");
        files[6] = new File(rootDir + "sql/upgrade/1.3.3_schema");
        files[7] = new File(rootDir + "sql/upgrade/1.3.5_schema");
        files[8] = new File(rootDir + "sql/upgrade/1.3.6_schema");
        files[9] = new File(rootDir + "sql/upgrade/1.4.0_schema");
        PowerMockito.when(FileUtils.getAllDir("sql/upgrade")).thenReturn(files);

        PowerMockito.whenNew(File.class).withArguments("sql/soft_version").thenReturn(null);
        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(null);
        PowerMockito.when(FileUtils.readFile2Str(Mockito.any())).thenReturn("1.4.0");

        DolphinSchedulerManager dolphinSchedulerManager = new DolphinSchedulerManager();
        dolphinSchedulerManager.upgradeDolphinScheduler();
        Assert.assertNotNull(dolphinSchedulerManager.upgradeDao);
    }
}
