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

import static org.mockito.ArgumentMatchers.anyString;

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTask;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTaskTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * shell task return test.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ShellTask.class})
public class ShellTaskReturnTest {
    private static final Logger logger = LoggerFactory.getLogger(ShellTaskTest.class);

    private ShellTask shellTask;
    private ShellCommandExecutor shellCommandExecutor;
    private TaskExecutionContext taskExecutionContext;
    private CommandExecuteResult commandExecuteResult;

    @Before
    public void before() throws Exception {
        System.setProperty("log4j2.disable.jmx", Boolean.TRUE.toString());
        shellCommandExecutor = PowerMockito.mock(ShellCommandExecutor.class);
        PowerMockito.whenNew(ShellCommandExecutor.class).withAnyArguments().thenReturn(shellCommandExecutor);
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setTaskName("kris test");
        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        taskExecutionContext.setHost("127.0.0.1:1234");
        taskExecutionContext.setExecutePath("/tmp");
        taskExecutionContext.setLogPath("/log");
        taskExecutionContext.setTaskJson(
                "{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],"
                        + "\\\"failedNode\\\":[\\\"\\\"]}\",\"conditionsTask\":false,"
                        + "\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\""
                        + "tasks-16849\",\"maxRetryTimes\":0,\"name\":\"shell test 001\","
                        + "\"params\":\"{\\\"rawScript\\\":\\\"#!/bin/sh\\\\necho $[yyyy-MM-dd HH:mm:ss +3]\\\\necho \\\\\\\" ?? "
                        + "${time1} \\\\\\\"\\\\necho \\\\\\\" ????? ${time2}\\\\\\\"\\\\n\\\","
                        + "\\\"localParams\\\":[{\\\"prop\\\":\\\"time1\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":"
                        + "\\\"VARCHAR\\\",\\\"value\\\":\\\"$[yyyy-MM-dd HH:mm:ss]\\\"},"
                        + "{\\\"prop\\\":\\\"time2\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${time_gb}\\\"}"
                        + "],\\\"resourceList\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,"
                        + "\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":"
                        + "{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\","
                        + "\"type\":\"SHELL\",\"workerGroup\":\"default\"}");
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setGlobalParams("[{\"direct\":\"IN\",\"prop\":\"time_gb\",\"type\":\"VARCHAR\",\"value\":\"2020-12-16 17:18:33\"}]");
        taskExecutionContext.setExecutorId(1);
        taskExecutionContext.setCmdTypeIfComplement(5);
        taskExecutionContext.setTenantCode("roo");
        taskExecutionContext.setScheduleTime(new Date());
        taskExecutionContext.setQueue("default");
        taskExecutionContext.setTaskParams(
                "{\"rawScript\":\"#!/bin/sh\\necho $[yyyy-MM-dd HH:mm:ss +3]\\necho \\\" ?? ${time1} \\\"\\necho \\\" ????? ${time2}\\\"\\n\",\"localParams\":"
                        +
                        "[{\"prop\":\"time1\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"$[yyyy-MM-dd HH:mm:ss]\"},{\"prop\":\"time2\",\"direct\":\"IN\",\"type\":\"VARCHAR"
                        + "\",\"value\":\"${time_gb}\"}],\"resourceList\":[]}");
        Map<String, String> definedParams = new HashMap<>();
        definedParams.put("time_gb", "2020-12-16 00:00:00");
        taskExecutionContext.setDefinedParams(definedParams);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.when(Files.exists(Paths.get(anyString()))).thenReturn(true);
        commandExecuteResult = new CommandExecuteResult();

        commandExecuteResult.setExitStatusCode(0);
        commandExecuteResult.setProcessId(1);
    }

    @Test
    public void testShellReturnString() {
        shellTask = new ShellTask(taskExecutionContext, logger);
        shellTask.init();
        try {
            PowerMockito.when(shellCommandExecutor.run(anyString())).thenReturn(commandExecuteResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
