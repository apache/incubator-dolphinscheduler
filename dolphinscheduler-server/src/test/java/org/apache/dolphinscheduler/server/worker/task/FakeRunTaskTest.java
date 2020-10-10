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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.TaskRecordDao;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TaskRecordDao.class)
@PowerMockIgnore({"javax.management.*"})
public class FakeRunTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(FakeRunTaskTest.class);

    @Before
    public void before() throws Exception {
        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        PowerMockito.mockStatic(TaskRecordDao.class);
        PowerMockito.when(TaskRecordDao.getTaskRecordFlag()).thenReturn(false);
    }

    /**
     * test fake-run task
     */
    @Test
    public void testFakeRunTask() throws Exception {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        FakeRunTask task = new FakeRunTask(taskExecutionContext, logger);
        task.init();
        task.handle();
        Assert.assertEquals(Constants.EXIT_CODE_SUCCESS, task.getExitStatusCode());
        task.after();
    }
}
