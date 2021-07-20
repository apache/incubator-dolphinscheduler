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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggerUtilsTest {
    private Logger logger = LoggerFactory.getLogger(LoggerUtilsTest.class);

    @Test
    public void buildTaskId() {

      String taskId = LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,79,4084,15210);

      Assert.assertEquals(" - [taskAppId=TASK-79-4084-15210]", taskId);
    }

    @Test
    public void getAppIds() {
        List<String> appIdList = LoggerUtils.getAppIds("The url to track the job: http://AzaAaa123s456:8088/proxy/application_1624948206873_0693", logger);
        Assert.assertEquals("http://AzaAaa123s456:8088/proxy/application_1624948206873_0693", appIdList.get(0));

    }
}