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

package org.apache.dolphinscheduler.common.model;

import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author wang
 */
public class TaskNodeTest {

    @Test
    public void testGetTaskTimeoutParameter_1() {
        TaskNode taskNode = new TaskNode();
        taskNode.setTimeout("{\"strategy\":\"FAILED\",\"interval\":3,\"enable\":true,\"checkInterval\":1}");
        TaskTimeoutParameter taskTimeoutParameter = taskNode.getTaskTimeoutParameter();
        Assert.assertTrue(taskTimeoutParameter.getEnable());
    }

    @Test
    public void testGetTaskTimeoutParameter_2() {
        TaskNode taskNode = new TaskNode();
        taskNode.setTimeout(null);
        TaskTimeoutParameter taskTimeoutParameter = taskNode.getTaskTimeoutParameter();
        Assert.assertFalse(taskTimeoutParameter.getEnable());
    }

}